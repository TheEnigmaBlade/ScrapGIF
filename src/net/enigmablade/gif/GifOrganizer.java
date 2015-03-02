package net.enigmablade.gif;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import com.alee.log.*;
import com.alee.managers.language.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.util.*;

public class GifOrganizer implements UIController, FileSystemAccessor
{
	private Config config;
	
	private GifOrganizerUI view;
	private ImageCache imageCache;
	private TagCache recentTags;
	
	private LibraryManager libraryManager;
	private Library currentLibrary;
	
	private SearchManager search;
	
	// Initialization
	
	public GifOrganizer()
	{
		config = SettingsLoader.getConfig("config");
		view = new GifOrganizerUI(this, config);
		search = new SearchManager();
		
		initSettings();
	}
	
	public void initSettings()
	{
		// Image cache
		imageCache = new ImageCache();
		
		// Image display
		view.setImageSize(config.getImageSize());
		
		// Recent tags
		recentTags = new TagCache(10);
		String recentTagsStr = config.getRecentTags();
		if(recentTagsStr != null)
			recentTags.addAll(Arrays.asList(recentTagsStr.split(",")));
		
		// View settings
		if(config.isWindowSizeSet())
		{
			int width = config.getWindowWidth();
			int height = config.getWindowHeight();
			if(width > 0 && height > 0)
				view.setSize(width, height);
		}
	}
	
	public void start()
	{
		Set<String> paths = config.getLibraries();
		libraryManager = LibraryManager.initInstance(paths);
		List<Library> libraries = libraryManager.getLibraries();
		view.setLibraries(libraries);
		String selectedId = config.getProperty("current_library", libraries.size() > 0 ? libraries.get(0).getId() : "");
		Log.info("Initial library: "+selectedId);
		SwingUtilities.invokeLater(() -> view.selectLibrary(libraryManager.getLibrary(selectedId)));
	}
	
	// UI interaction
	
	@Override
	public void setLibrary(Library library)
	{
		Log.info("Setting library: "+library);
		
		if(currentLibrary != null)
			currentLibrary.setLoaded(false);
		
		currentLibrary = library;
		config.setProperty("current_library", currentLibrary.getId());
		
		if(!currentLibrary.isLoaded())
		{
			new LibraryLoaderWorker(currentLibrary).execute();
			view.setLibraryLoading(true);
		}
		else
		{
			refreshCurrentLibrary();
		}
	}
	
	@Override
	public void addFilesFromDrag(List<File> files)
	{
		boolean someInvalid = false;
		
		for(File file : files)
		{
			if(ImageLoader.IMAGE_FILTER.accept(file.getParentFile(), file.getName()))
			{
				addImage(file, true, true, true);
			}
			else
			{
				someInvalid = true;
			}
		}
		
		if(someInvalid)
			view.notifyBadFile();
	}
	
	@Override
	public void addUrlFromDrag(URL url)
	{
		Log.info("Requested drag from URL: "+url.toString());
		if(!ImageLoader.IMAGE_FILTER.accept(null, url.toString()))
		{
			Log.warn("Invalid image extension");
			view.notifyBadFile();
			return;
		}
		
		view.startDownloadProgress();
		
		// Create temporary file
		String imageFileName = url.getFile();
		String ext = imageFileName.substring(imageFileName.lastIndexOf('.'));
		
		File tempFile;
		try
		{
			tempFile = File.createTempFile("giforg-temp", ext);
		}
		catch(IOException e)
		{
			Log.error("Failed to create temp file for image download", e);
			view.notifyDownloadError();
			return;
		}
		
		// Download file
		if(!IOUtil.downloadFile(url, tempFile))
		{
			Log.error("Failed to for image download");
			view.notifyDownloadError();
			return;
		}
		
		view.endProgress();
		
		// Process file
		addFilesFromDrag(Collections.singletonList(tempFile));
	}
	
	@Override
	public void tagImage(ImageData image)
	{
		String tag = view.getTagInput();
		if(tag != null && tag.length() > 0)
		{
			tag = tag.trim().toLowerCase();
			image.addTag(tag);
			saveLibrary();
			
			useTags(tag);
			refreshCurrentLibrary();
		}
	}
	
	@Override
	public void starImage(ImageData image)
	{
		image.setStarred(!image.isStarred());
		view.refreshImageMenu();
		saveLibrary();
	}
	
	@Override
	public void animateImage(ImageData image)
	{
		ImageFrame[] frames = imageCache.get(image.getId());
		if(frames == null)
			new ImageLoaderWorker(image).execute();
		else
			animateImage(image, frames);
	}
	
	@Override
	public void uploadImage(ImageData image)
	{
		// Image already uploaded
		if(image.getLinks().size() > 0)
		{
			Log.info("Image already uploaded");
			copyImageLink(image.getLinks().get(0));
			view.notifyLinkCopy();
		}
		// Upload image
		else
		{
			view.startUploadProgress();
			
			Service service = ServiceManager.getService(config.getPreferredServices(), image, this);
			if(service == null)
			{
				Log.warn("No service exists for image: type="+image.getType());
				uploadImageCallback(ServiceError.NO_SERVICE, null, null);
			}
			else
			{
				service.upload(currentLibrary.getImagePath(image).toFile(), image, this::uploadImageCallback, this::uploadProgressCallback);
			}
		}
	}
	
	public void uploadProgressCallback(int progress)
	{
		if(progress > 0)
		{
			if(progress < 100)
				view.setProgress(progress);
			else
				view.endUploadProgress();
		}
	}
	
	public void uploadImageCallback(ServiceError error, ImageData image, ServiceLink link)
	{
		view.endProgress();
		
		// Upload successful
		if(error == ServiceError.NONE)
		{
			image.addLink(link);
			copyImageLink(link);
			saveLibrary();
		}
		
		view.notifyUpload(error, config.isSoundEffectsEnabled());
	}
	
	@Override
	public void openFileSystem(ImageData image)
	{
		Log.info("Opening file system: "+image.getPath()+" in "+currentLibrary.getPath());
		if(System.getProperty("os.name").toLowerCase().contains("windows"))
		{
			ProcessBuilder b = new ProcessBuilder("explorer", "/select,\""+image.getPath()+"\"");
			b.directory(new File(currentLibrary.getPath()));
			try
			{
				b.start();
				return;
			}
			catch(Exception e)
			{
				Log.error("Failed to use Windows-specific file system open", e);
			}
		}
		
		if(Desktop.isDesktopSupported())
		{
			try
			{
				Desktop.getDesktop().open(new File(currentLibrary.getPath()));
			}
			catch(Exception e)
			{
				Log.error("Failed to open default file browser", e);
			}
		}
	}
	
	@Override
	public void setSearchFavorites(String currentQuery, boolean onlyFavs)
	{
		currentQuery = currentQuery.toLowerCase();
		new SearchWorker(currentQuery, false, true, onlyFavs, this::searchCallback).execute();
	}
	
	@Override
	public void addToSearchQuery(String newQuery, boolean onEnd, boolean onlyFavs)
	{
		newQuery = newQuery.toLowerCase();
		new SearchWorker(newQuery, onEnd, false, onlyFavs, this::searchCallback).execute();
	}
	
	@Override
	public void removedFromSearchQuery(String newQuery, boolean onEnd, boolean onlyFavs)
	{
		newQuery = newQuery.toLowerCase();
		new SearchWorker(newQuery, onEnd, true, onlyFavs, this::searchCallback).execute();
	}
	
	private void searchCallback(List<ImageData> images)
	{
		view.setImages(images);
	}
	
	@Override
	public void loadThumbnail(ImageData image)
	{
		view.setImageItemLoading(image.getId(), true);
		new ThumbnailLoaderWorker(image).execute();
	}
	
	@Override
	public void close()
	{
		Log.info("Closing");
		
		config.setWindowSize(view.getWidth(), view.getHeight());
		
		System.exit(0);
	}
	
	//// Menu bar
	
	@Override
	public void createLibrary(File dir, String name)
	{
		Log.info("Creating library: name="+name+", dir="+dir.getAbsolutePath());
		
		// Resolve library dir
		dir = dir.toPath().resolve(name).toFile();
		Log.info("Making dir structure for: "+dir.getAbsolutePath());
		if(!dir.mkdirs())
		{
			Log.error("Failed to create library directories");
			view.notifyLibraryCreateError();
			return;
		}
		
		// Create and add library
		Library library = new Library(name, dir.getAbsolutePath());
		while(!LibraryManager.getInstance().addLibrary(library))
			library.regenId();
		
		updateLibraries(library);
	}
	
	@Override
	public void createLibraryFrom(File dir, String name)
	{
		Log.info("Creating library from: name="+name+", dir="+dir.getAbsolutePath());
		Library library = new Library(name, dir.getAbsolutePath());
		
		// Add existing images to the library
		File[] files = dir.listFiles(ImageLoader.IMAGE_FILTER);
		for(File file : files)
			library.addImage(new ImageData(file.getName(), true));
		
		// Add new library to manager
		while(!LibraryManager.getInstance().addLibrary(library))
			library.regenId();
		
		updateLibraries(library);
	}
	
	@Override
	public void importLibrary(File dir)
	{
		Log.info("Creating library: dir="+dir.getAbsolutePath());
		Library library = LibraryManager.getInstance().importLibrary(dir.getAbsolutePath());
		if(library == null)
		{
			Log.warn("Library was not imported");
			view.notifyLibraryImportError();
			return;
		}
		
		updateLibraries(library);
	}
	
	@Override
	public void manageLibraries()
	{
		//TODO
	}
	
	@Override
	public void addLocalImage()
	{
		File imageFile = view.getImageFileInput();
		if(imageFile != null)
		{
			Log.info("Adding local image: "+imageFile.getAbsolutePath());
			addImage(imageFile, false, true, true);
		}
	}
	
	@Override
	public void addWebImage()
	{
		String url = view.getImageUrlInput();
		if(url != null)
		{
			Log.info("Adding web image: "+url);
			try
			{
				addUrlFromDrag(new URL(url));
			}
			catch(MalformedURLException e)
			{
				Log.error("Invalid URL given: "+url);
				view.notifyDownloadError();
			}
		}
	}
	
	@Override
	public void addImageFolder()
	{
		File dir = view.getImageFolderInput();
		if(dir != null)
		{
			Log.info("Adding image folder: "+dir.getAbsolutePath());
			if(currentLibrary.getPath().equals(dir.getAbsolutePath()))
			{
				Log.error("Library can't add its own directory!");
				return;
			}
			
			File[] files = dir.listFiles(ImageLoader.IMAGE_FILTER);
			if(files.length > 0)
			{
				for(int n = 0; n < files.length-1; n++)
					addImage(files[n], false, false, false);
				addImage(files[files.length-1], false, false, true);
			}
			else
			{
				Log.warn("No images to add");
			}
		}
	}
	
	//// Settings
	
	@Override
	public void setCheckNewImages(boolean check)
	{
		config.setCheckNewImages(check);
	}
	
	@Override
	public void setLanguage(String language)
	{
		LanguageManager.setLanguage(language);
	}
	
	// Workers
	
	private class LibraryLoaderWorker extends SwingWorker<List<File>, Void>
	{
		private Library library;
		
		public LibraryLoaderWorker(Library library)
		{
			this.library = library;
		}
		
		@Override
		protected List<File> doInBackground()
		{
			LibraryManager.loadLibrary(library);
			search.buildSearchFromLibrary(library);
			return config.isCheckNewImages() ? LibraryManager.getUnloadedImages(library) : Collections.emptyList();
		}
		
		@Override
		protected void done()
		{
			try
			{
				List<File> newImages = get();
				if(!newImages.isEmpty() && view.getNewImagesConfirmation(newImages.size()))
				{
					for(File image : newImages)
					{
						addImage(image, true, false, false);
					}
					saveLibrary();
				}
			}
			catch(InterruptedException | ExecutionException e)
			{
				Log.error(e);
			}
			
			refreshCurrentLibrary();
		}
	}
	
	private class ThumbnailLoaderWorker extends SwingWorker<BufferedImage, Void>
	{
		private ImageData image;
		
		public ThumbnailLoaderWorker(ImageData image)
		{
			this.image = image;
		}
		
		@Override
		protected BufferedImage doInBackground() throws IOException
		{
			return ImageLoaders.getThumbnail(currentLibrary, image);
		}
		
		@Override
		protected void done()
		{
			view.setImageItemLoading(image.getId(), false);
			
			try
			{
				image.setThumbnail(get());
				view.updatedThumbnail(image);
			}
			catch(InterruptedException | ExecutionException e)
			{
				Log.error(e);
			}
		}
	};
	
	private class ImageLoaderWorker extends SwingWorker<ImageFrame[], Void>
	{
		private ImageData item;
		
		public ImageLoaderWorker(ImageData item)
		{
			this.item = item;
		}
		
		@Override
		protected ImageFrame[] doInBackground() throws IOException
		{
			SwingUtilities.invokeLater(() -> view.setImageItemLoading(item.getId(), true));
			
			File file = currentLibrary.getImagePath(item).toFile();
			if(!file.exists() || !file.isFile())
			{
				Log.error("Bad file: "+file.getAbsolutePath());
				return null;
			}
			
			ImageLoader loader = ImageLoaders.getLoader(IOUtil.getFileExtension(file));
			ImageFrame[] frames = loader.loadImage(file);
			if(frames != null)
				imageCache.store(item.getId(), frames);
			return frames;
		}
		
		@Override
		protected void done()
		{
			try
			{
				animateImage(item, get());
			}
			catch(InterruptedException | ExecutionException e)
			{
				Log.error(e);
			}
		}
	};
	
	private class SearchWorker extends SwingWorker<List<ImageData>, Void>
	{
		private String diff;
		private boolean onEnd, removed, onlyFavs;
		private Consumer<List<ImageData>> callback;
		
		public SearchWorker(String diff, boolean onEnd, boolean removed, boolean onlyFavs, Consumer<List<ImageData>> callback)
		{
			this.diff = diff;
			this.onEnd = onEnd;
			this.removed = removed;
			this.onlyFavs = onlyFavs;
			this.callback = callback;
		}
		
		@Override
		protected List<ImageData> doInBackground()
		{
			if(removed)
				return search.removeFromQuery(diff, onEnd, onlyFavs);
			return search.addToQuery(diff, onEnd, onlyFavs);
		}
		
		@Override
		protected void done()
		{
			try
			{
				callback.accept(get());
			}
			catch(InterruptedException | ExecutionException e)
			{
				Log.error(e);
			}
		}
	}
	
	// Actions
	
	private void useTags(String... tags)
	{
		for(String tag : tags)
		{
			currentLibrary.addTag(tag);
			recentTags.add(tag);
		}
		config.setRecentTags(recentTags.toPropertyString());
	}
	
	private boolean addImage(File imageFile, boolean moveFile, boolean askTags, boolean update)
	{
		// Get tags
		ImageLoader imageLoader = ImageLoaders.getLoader(IOUtil.getFileExtension(imageFile));
		ImageFrame[] image = imageLoader.loadImage(imageFile);
		Set<String> tags = askTags ? view.getMultiTagInput(image, recentTags) : Collections.emptySet();
		if(tags == null)
			// Canceled add
			return false;
		
		// Create image data instance
		Path source = imageFile.toPath();
		ImageData data = new ImageData(imageFile.getName());
		while(currentLibrary.hasImage(data))
			data.regenId();
		Path target = currentLibrary.getImagePath(data);
		
		Log.info((moveFile ? "Moving" : "Copying")+" image file");
		Log.info("\tSource: "+source);
		Log.info("\tTarget: "+target);
		
		// Move image to library folder
		try
		{
			if(moveFile)
				Files.move(source, target);
			else
				Files.copy(source, target);
		}
		catch(FileAlreadyExistsException e)
		{
			Log.error("Failed to move image file: cannot overwrite", e);
			view.notifyFileRelocateError(moveFile);
			return false;
		}
		catch(Exception e)
		{
			Log.error("Failed to move image file", e);
			view.notifyFileRelocateError(moveFile);
			return false;
		}
		
		// Set new image tags
		for(String tag : tags)
		{
			tag = tag.trim().toLowerCase();
			data.addTag(tag);
		}
		useTags(tags.toArray(new String[0]));
		
		// Add image to library index
		data.setThumbnail(ImageLoaders.getThumbnail(currentLibrary, data));
		currentLibrary.addImage(data);
		view.addImage(data);
		
		if(update)
		{
			saveLibrary();
			refreshCurrentLibrary();
		}
		
		return true;
	}
	
	private void animateImage(ImageData image, ImageFrame[] frames)
	{
		view.setImageItemAnimating(image.getId(), frames);
	}
	
	private void saveLibrary()
	{
		Log.info("Saving library");
		
		// Save current library
		LibraryManager.saveLibrary(currentLibrary);
	}
	
	public void copyImageLink(ServiceLink link)
	{
		String url = ServiceManager.createUrl(link);
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(url), null);
	}
	
	// Updaters
	
	private void updateLibraries(Library toSelect)
	{
		LibraryManager man = LibraryManager.getInstance();
		
		// Save library list
		Log.info("Saving library list");
		List<Library> newLibraries = man.getLibraries();
		config.setLibraries(newLibraries.stream().map((lib) -> lib.getPath()).collect(Collectors.toSet()));
		
		// Update UI
		view.setLibraries(newLibraries);
		view.selectLibrary(toSelect);
	}
	
	private void refreshCurrentLibrary()
	{
		Log.info("Refreshing current library");
		search.buildSearchFromLibrary(currentLibrary);
		view.resetSearch();
		view.setLibraryLoading(false);
		view.setImages(currentLibrary.getImages());
		view.setLibrarySize(currentLibrary.getImages().size());
	}
	
	// Helpers
	
	@Override
	public boolean isValidLibrary(File dir)
	{
		return LibraryManager.isLibrary(dir.getAbsolutePath());
	}
	
	//// File system access
	
	@Override
	public long getImageLastModified(ImageData image)
	{
		Path p = currentLibrary.getImagePath(image);
		try
		{
			return Files.getLastModifiedTime(p).toMillis();
		}
		catch(Exception e)
		{
			Log.error("Failed to get last modified attribute for "+p.toString(), e);
			return -1;
		}
	}
	
	@Override
	public long getImageSize(ImageData image)
	{
		Path p = currentLibrary.getImagePath(image);
		try
		{
			return Files.size(p);
		}
		catch(Exception e)
		{
			Log.error("Failed to get file size attribute for "+p.toString(), e);
			return -1;
		}
	}
	
	// Accessor methods
	
	public GifOrganizerUI getView()
	{
		return view;
	}
}

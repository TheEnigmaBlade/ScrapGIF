package net.enigmablade.gif;

import java.awt.*;
import java.awt.datatransfer.*;
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
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.util.*;

public class GifOrganizer implements UIController
{
	private Config config;
	
	private GifOrganizerUI view;
	private ImageCache imageCache;
	private TagCache recentTags;
	
	private LibraryManager libraryManager;
	private Library currentLibrary;
	
	private SearchManager search;
	
	//Initialization
	
	public GifOrganizer()
	{
		view = new GifOrganizerUI(this);
		search = new SearchManager();
		
		initSettings();
	}
	
	public void initSettings()
	{
		config = SettingsLoader.getConfig("config");
		
		//Image cache
		imageCache = new ImageCache();
		
		//Recent tags
		recentTags = new TagCache(10);
		String recentTagsStr = config.getRecentTags();
		if(recentTagsStr != null)
			recentTags.addAll(Arrays.asList(recentTagsStr.split(",")));
		
		//View settings
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
		view.selectLibrary(libraryManager.getLibrary(selectedId));
	}
	
	//UI interaction
	
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
	}
	
	@Override
	public void addFilesFromDrag(List<File> files)
	{
		boolean someInvalid = false;
		
		for(File file : files)
		{
			if(ImageLoader.IMAGE_FILTER.accept(file.getParentFile(), file.getName()))
			{
				// Get tags
				ImageLoader imageLoader = ImageLoader.getInstance(file.getAbsolutePath());
				ImageFrame[] image = imageLoader.readFull();
				Set<String> tags = view.getMultiTagInput(image, recentTags);
				if(tags == null)
					// Canceled add
					continue;
				
				// Move image
				Path source = Paths.get(file.getAbsolutePath());
				ImageData data = new ImageData(source, file.getName());
				while(currentLibrary.hasImage(data))
					data.regenId();
				Path target = Paths.get(currentLibrary.getImagePath(data));
				
				Log.info("Moving dragged image");
				Log.info("\tSource: "+source);
				Log.info("\tTarget: "+target);
				
				// Move image to library folder
				try
				{
					Files.move(source, target);
				}
				catch(FileAlreadyExistsException e)
				{
					Log.error("Failed to move image file: cannot overwrite", e);
					view.notifyMoveError();
					continue;
				}
				catch(Exception e)
				{
					Log.error("Failed to move image file", e);
					view.notifyMoveError();
					continue;
				}
				
				// Set new image tags
				for(String tag : tags)
				{
					tag = tag.trim().toLowerCase();
					data.addTag(tag);
				}
				useTags(tags.toArray(new String[0]));
				
				// Add image to library index
				data.setThumbnail(ImageLoader.getThumbnail(currentLibrary, data.getId(), data.getPath()));
				currentLibrary.addImage(data);
				saveLibrary();
				
				view.addImage(data);
				search.buildSearchFromLibrary(currentLibrary);
				view.resetSearch();
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
		ImageLoaderWorker loader = new ImageLoaderWorker(image);
		loader.execute();
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
			
			ServiceManager service = ServiceManager.getInstance(null, image.getPath());
			service.upload(currentLibrary, image, this::uploadImageCallback, this::uploadProgressCallback);
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
		
		view.notifyUpload(ServiceError.NONE);
	}
	
	@Override
	public void openFileSystem(ImageData image)
	{
		String path = currentLibrary.getImagePath(image);
		if(System.getProperty("os.name").toLowerCase().contains("windows"))
		{
			ProcessBuilder b = new ProcessBuilder("explorer", "/select,"+path);
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
				Desktop.getDesktop().open(new File(path).getParentFile());
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
	public void close()
	{
		Log.info("Closing");
		
		config.setWindowSize(view.getWidth(), view.getHeight());
		
		System.exit(0);
	}
	
	////Menu bar
	
	@Override
	public void createLibrary(File dir, String name)
	{
		Log.info("Creating library: "+name);
		Library library = new Library(name, dir.getAbsolutePath());
		
		File[] files = dir.listFiles(ImageLoader.IMAGE_FILTER);
		for(File file : files)
		{
			ImageData image = createImage(file, library);
			if(image != null)
				library.addImage(image);
		}
		
		// Add new library to manager
		LibraryManager man = LibraryManager.getInstance();
		man.addLibrary(library);
		// Save library list
		Log.info("Saving library list");
		config.setLibraries(man.getLibraries().stream().map((lib) -> lib.getPath()).collect(Collectors.toSet()));
		
		// Update UI
		view.setLibraries(LibraryManager.getInstance().getLibraries());
		view.selectLibrary(library);
	}
	
	//Workers
	
	private class LibraryLoaderWorker extends SwingWorker<Void, Void>
	{
		private Library library;
		
		public LibraryLoaderWorker(Library library)
		{
			this.library = library;
		}
		
		@Override
		protected Void doInBackground()
		{
			LibraryManager.loadLibrary(library);
			search.buildSearchFromLibrary(library);
			return null;
		}
		
		@Override
		protected void done()
		{
			view.resetSearch();
			view.setLibraryLoading(false);
			view.setImages(currentLibrary.getImages());
			view.setLibrarySize(currentLibrary.getImages().size());
		}
	}
	
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
			ImageFrame[] frames = imageCache.get(item.getId());
			if(frames != null)
				return frames;
			
			SwingUtilities.invokeLater(() -> view.setImageItemLoading(item.getId()));
			
			File file = new File(currentLibrary.getPath()+"/"+item.getPath());
			if(!file.exists() || !file.isFile())
			{
				Log.error("Bad file: "+file.getAbsolutePath());
				return frames;
			}
			
			ImageLoader loader = ImageLoader.getInstance(file.getAbsolutePath());
			frames = loader.readFull();
			if(frames != null)
				imageCache.put(item.getId(), frames);
			return frames;
		}
		
		@Override
		protected void done()
		{
			try
			{
				ImageFrame[] frames = get();
				view.setImageItemAnimating(item.getId(), frames);
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
	
	//Helpers
	
	private void useTags(String... tags)
	{
		for(String tag : tags)
		{
			currentLibrary.addTag(tag);
			recentTags.add(tag);
		}
		config.setRecentTags(recentTags.toPropertyString());
	}
	
	private void saveLibrary()
	{
		Log.info("Saving library");
		
		// Save current library
		LibraryManager.saveLibrary(currentLibrary);
	}
	
	private ImageData createImage(File file, Library library)
	{
		ImageData data = new ImageData(file.toPath(), file.getName());
		data.setThumbnail(ImageLoader.getThumbnail(library, data.getId(), data.getPath()));
		return data;
	}
	
	public void copyImageLink(ServiceLink link)
	{
		String url = ServiceManager.createUrl(link);
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(url), null);
	}
	
	//Accessor methods
	
	public GifOrganizerUI getView()
	{
		return view;
	}
}

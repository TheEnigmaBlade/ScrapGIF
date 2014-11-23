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
import javax.swing.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.ui.*;

public class GifOrganizer implements UIController
{
	private Properties config;
	
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
		config = SettingsLoader.getProperties("config");
		
		//Image cache
		imageCache = new ImageCache();
		
		//Recent tags
		recentTags = new TagCache(10);
		String recentTagsStr = config.getProperty("recent_tags");
		if(recentTagsStr != null)
			recentTags.addAll(Arrays.asList(recentTagsStr.split(",")));
		
		//View settings
		if(config.containsKey("window_width") && config.containsKey("window_height"))
		{
			try
			{
				int width = Integer.parseUnsignedInt(config.getProperty("window_width"));
				int height = Integer.parseUnsignedInt(config.getProperty("window_height"));
				view.setSize(width, height);
			}
			catch(NumberFormatException e)
			{
				Log.error("Invalid window sizes", e);
			}
		}
	}
	
	public void start()
	{
		libraryManager = LibraryManager.getInstance();
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
		setConfigProperty("current_library", currentLibrary.getId());
		
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
				Path source = Paths.get(file.getAbsolutePath());
				ImageData data = new ImageData(file.getName());
				while(currentLibrary.hasImage(data))
					data.regenId();
				Path target = Paths.get(currentLibrary.getImagePath(data));
				
				Log.info("Moving dragged image");
				Log.info("\tSource: "+source);
				Log.info("\tTarget: "+target);
				
				//Move image to library folder
				try
				{
					Files.move(source, target);
				}
				catch(IOException e)
				{
					Log.error("Failed to move image file", e);
					continue;
				}
				
				//Get new image tags
				ImageLoader imageLoader = ImageLoader.getInstance(currentLibrary.getImagePath(data));
				ImageFrame[] image = imageLoader.readFull();
				Set<String> tags = view.getMultiTagInput(image, recentTags);
				for(String tag : tags)
				{
					tag = tag.trim().toLowerCase();
					data.addTag(tag);
				}
				useTags(tags.toArray(new String[0]));
				
				//Add image to library index
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
		//TODO
		// 1. Download file
		// 2. Verify type
		// 3. addFilesFromDrag(...)
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
	public void animateImage(ImageData image)
	{
		ImageLoaderWorker loader = new ImageLoaderWorker(image);
		loader.execute();
	}
	
	@Override
	public void uploadImage(ImageData image)
	{
		//Image already uploaded
		if(image.getLinks().size() > 0)
		{
			Log.info("Image already uploaded");
			copyImageLink(image.getLinks().get(0));
			view.notifyLinkCopy();
		}
		//Upload image
		else
		{
			ServiceManager service = ServiceManager.getInstance(null, image.getPath());
			service.upload(currentLibrary, image, this::uploadImageCallback);
		}
	}
	
	public void uploadImageCallback(ImageData image, ServiceLink link)
	{
		if(link != null)
		{
			image.addLink(link);
			copyImageLink(link);
			view.notifyUpload(true);
			saveLibrary();
		}
		else
		{
			view.notifyUpload(false);
		}
	}
	
	@Override
	public void addToSearchQuery(String newQuery, boolean onEnd)
	{
		newQuery = newQuery.toLowerCase();
		new SearchWorker(newQuery, onEnd, false, this::searchCallback).execute();
	}
	
	@Override
	public void removedFromSearchQuery(String newQuery, boolean onEnd)
	{
		newQuery = newQuery.toLowerCase();
		new SearchWorker(newQuery, onEnd, true, this::searchCallback).execute();
	}
	
	private void searchCallback(List<ImageData> images)
	{
		view.setImages(images);
	}
	
	@Override
	public void close()
	{
		Log.info("Closing");
		
		setConfigProperty("window_width", String.valueOf(view.getWidth()), false);
		setConfigProperty("window_height", String.valueOf(view.getHeight()), true);
		
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
		
		LibraryManager.getInstance().addLibrary(library);
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
		private boolean onEnd, removed;
		private Consumer<List<ImageData>> callback;
		
		public SearchWorker(String diff, boolean onEnd, boolean removed, Consumer<List<ImageData>> callback)
		{
			this.diff = diff;
			this.onEnd = onEnd;
			this.removed = removed;
			this.callback = callback;
		}
		
		@Override
		protected List<ImageData> doInBackground()
		{
			if(removed)
				return search.removeFromQuery(diff, onEnd);
			return search.addToQuery(diff, onEnd);
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
		setConfigProperty("recent_tags", recentTags.toPropertyString());
	}
	
	private void setConfigProperty(String key, String value)
	{
		setConfigProperty(key, value, true);
	}
	
	private void setConfigProperty(String key, String value, boolean save)
	{
		config.setProperty(key, value);
		SettingsLoader.saveProperties("config", config);
	}
	
	private void saveLibrary()
	{
		Log.info("Saving library");
		LibraryManager.saveLibrary(currentLibrary);
	}
	
	private ImageData createImage(File file, Library library)
	{
		ImageData data = new ImageData(file.getName());
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

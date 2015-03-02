package net.enigmablade.gif.library;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.util.*;
import net.enigmablade.jsonic.*;

public class LibraryManager
{
	private static final String SETTINGS_DIR = "config";
	private static final String SETTINGS_FILE = "library.json";
	protected static final int LATEST_VERSION = 2;
	
	private static LibraryManager INSTANCE = new LibraryManager(Collections.<String>emptySet());
	
	public static LibraryManager initInstance(Set<String> libraryPaths)
	{
		return INSTANCE = new LibraryManager(libraryPaths);
	}
	
	public static LibraryManager getInstance()
	{
		return INSTANCE;
	}
	
	private Map<String, Library> libraries;
	
	// Loading
	
	public LibraryManager(Set<String> libraryPaths)
	{
		libraries = new HashMap<>(libraryPaths.size());
		
		for(String libraryPath : libraryPaths)
		{
			Library library = createLibrary(libraryPath);
			if(library != null)
			{
				Log.info("Loaded library: "+library.getName());
				libraries.put(library.getId(), library);
			}
		}
	}
	
	private static Library createLibrary(String libraryPath)
	{
		File libraryFile = getLibraryConfig(libraryPath);
		
		// File doesn't exist
		if(libraryFile == null || !libraryFile.exists())
		{
			Log.error("Library file doesn't exist: "+libraryPath);
			return null;
		}
		
		// Load and parse JSON contents
		try
		{
			JsonObject libraryObj = JsonParser.parseObject(libraryFile, false);
			return loadLibrary(libraryObj);
		}
		catch(IOException e)
		{
			Log.error("Failed to load library file", e);
		}
		catch(JsonParseException e)
		{
			Log.error("Failed to parse library file", e);
		}
		
		return null;
	}
	
	private static File getLibraryConfig(String libraryPath)
	{
		Path path = Paths.get(libraryPath, SETTINGS_DIR, SETTINGS_FILE);
		Log.info("Getting library config file: "+path.toString());
		return path.toFile();
	}
	
	private static Library loadLibrary(JsonObject libraryObj) throws JsonParseException
	{
		String type = libraryObj.getString("type");
		if(!"library".equals(type))
		{
			Log.error("Not a library");
			return null;
		}
		
		int version = libraryObj.getInt("version");
		if(version <= 2)
		{
			String id = libraryObj.getString("id");
			String name = libraryObj.getString("name");
			String path = libraryObj.getString("path");
			JsonArray images = libraryObj.getArray("images");
			
			return new Library(version, id, name, path, images);
		}
		else
		{
			Log.warn("Invalid library version: "+version);
		}
		
		return null;
	}
	
	public static void loadLibrary(Library library)
	{
		Log.info("Loading library");
		
		// Get unloaded image list
		JsonArray unloaded = library.getUnloadedImages();
		if(unloaded == null)
		{
			Library newLib = createLibrary(library.getPath());
			unloaded = newLib.getUnloadedImages();
		}
		
		// Parse image list
		for(JsonIterator it = unloaded.iterator(); it.hasNext();)
		{
			JsonObject imageObj = it.nextObject();
			ImageData data = createItem(library, imageObj);
			if(data != null)
			{
				library.addImage(data);
				for(String tag : data.getTags())
					library.addTag(tag);
			}
		}
		library.setLoaded(true);
	}
	
	private static ImageData createItem(Library library, JsonObject obj)
	{
		String id = obj.getString("id");
		String shortPath = obj.getString("path");
		boolean starred = obj.getBoolean("starred");
		
		List<String> tags = new ArrayList<>();
		JsonArray tagsA = obj.getArray("tags");
		for(JsonIterator it = tagsA.iterator(); it.hasNext();)
			tags.add(it.nextString());
		
		List<ServiceLink> links = new ArrayList<>();
		JsonArray linksA = obj.getArray("links");
		for(JsonIterator it = linksA.iterator(); it.hasNext();)
			links.add(createLink(library, it.nextObject()));
		
		File file = library.getImagePath(shortPath).toFile();
		if(!file.exists() || !file.isFile())
		{
			Log.error("Bad file: "+file.getAbsolutePath());
			return null;
		}
		
		return new ImageData(id, shortPath, tags, starred, links);
	}
	
	private static ServiceLink createLink(Library library, JsonObject obj)
	{
		int ver = library.getVersion();
		if(ver == 1)
		{
			String type = obj.getString("type");
			String id = obj.getString("id");
			return new ServiceLink("imgur", id+"."+type);
		}
		else
		{
			String host = obj.getString("host");
			String file = obj.getString("file");
			return new ServiceLink(host, file);
		}
	}
	
	public static List<File> getUnloadedImages(Library library)
	{
		if(!library.isLoaded())
			return Collections.emptyList();
		
		File dir = new File(library.getPath());
		File[] files = dir.listFiles(ImageLoader.IMAGE_FILTER);
		
		List<File> newFiles = new ArrayList<>();
		for(File file : files)
		{
			String maybeId = IOUtil.getFileName(file);
			if(!library.hasImageId(maybeId))
				newFiles.add(file);
		}
		return newFiles;
	}
	
	// Saving
	
	public static boolean saveLibrary(Library library)
	{
		Log.info("Saving library");
		
		JsonObject libraryObj = new JsonObject();
		libraryObj.put("type", "library");
		libraryObj.put("version", LATEST_VERSION);
		libraryObj.put("id", library.getId());
		libraryObj.put("name", library.getName());
		libraryObj.put("path", library.getPath());
		
		JsonArray imagesA = new JsonArray();
		for(ImageData image : library.getImages())
			imagesA.add(createItemJson(image));
		libraryObj.put("images", imagesA);
		
		return writeLibrary(library.getPath(), libraryObj);
	}
	
	private static JsonObject createItemJson(ImageData data)
	{
		JsonObject o = new JsonObject();
		o.put("id", data.getId());
		o.put("path", data.getPath());
		o.put("starred", data.isStarred());
		
		JsonArray tags = new JsonArray();
		for(String tag : data.getTags())
			tags.add(tag);
		o.put("tags", tags);
		
		JsonArray links = new JsonArray();
		for(ServiceLink link : data.getLinks())
			links.add(createLinkJson(link));
		o.put("links", links);
		
		return o;
	}
	
	private static JsonObject createLinkJson(ServiceLink link)
	{
		JsonObject o = new JsonObject();
		o.put("host", link.getService());
		o.put("file", link.getFile());
		return o;
	}
	
	private static boolean writeLibrary(String libraryPath, JsonObject libraryObj)
	{
		File libraryFile = getLibraryConfig(libraryPath);
		return IOUtil.writeFile(libraryFile, libraryObj.getJSON());
	}
	
	// Accessors
	
	public List<Library> getLibraries()
	{
		List<Library> list = new ArrayList<>(libraries.values());
		Collections.sort(list);
		return list;
	}
	
	public Library getLibrary(String id)
	{
		Library library = libraries.get(id);
		if(library == null)
			return null;
		return library;
	}
	
	public boolean addLibrary(Library library)
	{
		if(libraries.containsKey(library.getId()))
			return false;
		
		libraries.put(library.getId(), library);
		saveLibrary(library);
		return true;
	}
	
	public Library importLibrary(String libraryPath)
	{
		Log.info("Importing library: dir="+libraryPath);
		
		Library library = createLibrary(libraryPath);
		if(library != null)
		{
			Log.info("Loaded library: "+library.getName());
			libraries.put(library.getId(), library);
		}
		return library;
	}
	
	// Helpers
	
	public static boolean isLibrary(String libraryPath)
	{
		File libraryFile = getLibraryConfig(libraryPath);
		return libraryFile != null && libraryFile.exists();
	}
	
	public static boolean isValidLibraryName(String name)
	{
		//TODO
		return true;
	}
}

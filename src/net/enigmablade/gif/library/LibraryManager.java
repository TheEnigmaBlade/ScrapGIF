package net.enigmablade.gif.library;

import java.awt.image.*;
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
	
	//Loading
	
	public LibraryManager(Set<String> libraryPaths)
	{
		libraries = new HashMap<>(libraryPaths.size());
		
		for(String libraryPath : libraryPaths)
		{
			File libraryFile = getLibraryConfig(libraryPath);
			
			// File doesn't exist
			if(libraryFile == null)
			{
				Log.error("Library file doesn't exist: "+libraryPath);
				continue;
			}
			
			// Load and parse JSON contents
			try
			{
				JsonObject libraryObj = JsonParser.parseObject(libraryFile, false);
				Library library = loadLibrary(libraryObj);
				if(library != null)
				{
					Log.info("Loaded library: "+library.getName());
					libraries.put(library.getId(), library);
				}
			}
			catch(IOException e)
			{
				Log.error("Failed to load library file", e);
				continue;
			}
			catch(JsonParseException e)
			{
				Log.error("Failed to parse library file", e);
				continue;
			}
		}
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
		if(version == 1)
		{
			String id = libraryObj.getString("id");
			String name = libraryObj.getString("name");
			String path = libraryObj.getString("path");
			JsonArray images = libraryObj.getArray("images");
			
			return new Library(id, name, path, images);
		}
		
		return null;
	}
	
	public static void loadLibrary(Library library)
	{
		Log.info("Loading library");
		for(JsonIterator it = library.getUnloadedImages().iterator(); it.hasNext();)
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
		Path path = new File(library.getImagePath(shortPath)).toPath();
		boolean starred = obj.getBoolean("starred");
		
		List<String> tags = new ArrayList<>();
		JsonArray tagsA = obj.getArray("tags");
		for(JsonIterator it = tagsA.iterator(); it.hasNext();)
			tags.add(it.nextString());
		
		List<ServiceLink> links = new ArrayList<>();
		JsonArray linksA = obj.getArray("links");
		for(JsonIterator it = linksA.iterator(); it.hasNext();)
			links.add(createLink(it.nextObject()));
		
		File file = new File(library.getImagePath(shortPath));
		if(!file.exists() || !file.isFile())
		{
			Log.error("Bad file: "+file.getAbsolutePath());
			return null;
		}
		
		BufferedImage thumbnail = ImageLoader.getThumbnail(library, id, shortPath);
		if(thumbnail == null)
			return null;
		
		return new ImageData(id, path, shortPath, tags, starred, links, thumbnail);
	}
	
	private static ServiceLink createLink(JsonObject obj)
	{
		String type = obj.getString("type");
		String id = obj.getString("id");
		return new ServiceLink(type, id);
	}
	
	//Saving
	
	public static boolean saveLibrary(Library library)
	{
		Log.info("Saving library");
		
		JsonObject libraryObj = new JsonObject();
		libraryObj.put("type", "library");
		libraryObj.put("version", 1);
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
		o.put("type", link.getType());
		o.put("id", link.getId());
		return o;
	}
	
	private static boolean writeLibrary(String libraryPath, JsonObject libraryObj)
	{
		File libraryFile = getLibraryConfig(libraryPath);
		return IOUtil.writeFile(libraryFile, libraryObj.getJSON());
	}
	
	//Accessor methods
	
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
	
	public void addLibrary(Library library)
	{
		libraries.put(library.getId(), library);
		saveLibrary(library);
	}
}

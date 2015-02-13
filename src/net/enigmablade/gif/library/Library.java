package net.enigmablade.gif.library;

import java.nio.file.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.jsonic.*;

public class Library implements Comparable<Library>
{
	private int version;
	private String id, name, path;
	private Map<String, ImageData> images;
	private Set<String> tags;
	
	private boolean loaded;
	private JsonArray unloadedImages;
	
	public Library(String name, String path)
	{
		this.version = LibraryManager.LATEST_VERSION;
		this.id = Long.toString(path.hashCode(), 36);
		this.name = name;
		this.path = path;
		this.images = new LinkedHashMap<>();
		this.tags = new HashSet<>();
		
		loaded = true;
		unloadedImages = null;
	}
	
	public Library(int version, String id, String name, String path, JsonArray unloadedImages)
	{
		this.version = version;
		this.id = id;
		this.name = name;
		this.path = path;
		this.images = new LinkedHashMap<>();
		this.tags = new HashSet<>();
		
		loaded = false;
		this.unloadedImages = unloadedImages;
	}
	
	public int getVersion()
	{
		return version;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public List<ImageData> getImages()
	{
		return new ArrayList<>(images.values());
	}
	
	public boolean isLoaded()
	{
		return loaded;
	}
	
	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
		if(!loaded)
		{
			for(String key : images.keySet())
				images.get(key).destroy();
			images.clear();
		}
		else
		{
			unloadedImages = null;
		}
	}
	
	protected JsonArray getUnloadedImages()
	{
		return unloadedImages;
	}
	
	public void addImage(ImageData image)
	{
		images.put(image.getId(), image);
	}
	
	public boolean hasImage(ImageData image)
	{
		return images.containsKey(image.getId());
	}
	
	public Set<String> getTags()
	{
		return tags;
	}
	
	public boolean addTag(String tag)
	{
		return tags.add(tag);
	}
	
	//Utilities
	
	public Path getImagePath(String path)
	{
		//return this.path+File.separator+path;
		return Paths.get(this.path, path);
	}
	
	public Path getImagePath(ImageData image)
	{
		//return this.path+File.separator+image.getPath();
		return Paths.get(path, image.getPath());
	}
	
	public Path resolveImagePath(ImageData image)
	{
		return Paths.get(path, image.getPath());
	}
	
	//Overrides
	
	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof Library))
			return false;
		Library l = (Library)o;
		return id.equals(l.id);
	}
	
	@Override
	public int compareTo(Library o)
	{
		return name.compareTo(o.name);
	}
}

package net.enigmablade.gif.library;

import java.io.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.jsonic.*;

public class Library implements Comparable<Library>
{
	private String id, name, path;
	private Map<String, ImageData> images;
	private Set<String> tags;
	
	private boolean loaded;
	private JsonArray unloadedImages;
	
	public Library(String name, String path)
	{
		this.id = Long.toString(path.hashCode(), 36);
		this.name = name;
		this.path = path;
		this.images = new LinkedHashMap<>();
		this.tags = new HashSet<>();
		
		loaded = true;
		unloadedImages = null;
	}
	
	public Library(String id, String name, String path, JsonArray unloadedImages)
	{
		this.id = id;
		this.name = name;
		this.path = path;
		this.images = new LinkedHashMap<>();
		this.tags = new HashSet<>();
		
		loaded = false;
		this.unloadedImages = unloadedImages;
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
	
	public String getImagePath(String path)
	{
		return this.path+File.separator+path;
	}
	
	public String getImagePath(ImageData image)
	{
		return this.path+File.separator+image.getPath();
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

package net.enigmablade.gif.library;

import java.nio.file.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.jsonic.*;

public class Library implements Comparable<Library>
{
	private int version;
	private String id, name;
	private Path path;
	private Map<String, ImageData> images;
	private Set<String> tags;
	
	private boolean loaded;
	private JsonArray unloadedImages;
	
	// Initialization
	
	public Library(String name, String path)
	{
		this.version = LibraryManager.LATEST_VERSION;
		this.id = Long.toString(path.hashCode(), 36);
		this.name = name;
		this.path = Paths.get(path);
		this.images = new LinkedHashMap<>();
		this.tags = new HashSet<>();
		
		loaded = true;
		unloadedImages = null;
	}
	
	public Library(int version, String id, String name, Path path, JsonArray unloadedImages)
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
	
	// Accessors
	
	public int getVersion()
	{
		return version;
	}
	
	public String getId()
	{
		return id;
	}
	
	public void regenId()
	{
		int hash = id.hashCode();
		int k = (int)(Math.random()*Integer.SIZE);
		hash = (hash >>> k) | (hash << (Integer.SIZE - k));
		id = Long.toString(Math.abs(hash), 36);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public Path getPath()
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
	
	public void removeImage(ImageData image)
	{
		images.remove(image.getId());
	}
	
	public boolean hasImage(ImageData image)
	{
		return hasImageId(image.getId());
	}
	
	public boolean hasImageId(String imageId)
	{
		return images.containsKey(imageId);
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
		return this.path.resolve(path);
	}
	
	public Path getImagePath(ImageData image)
	{
		return getImagePath(image.getPath());
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

package net.enigmablade.gif.img;

import java.awt.image.*;
import java.nio.file.*;
import java.util.*;
import com.alee.log.*;
import net.enigmablade.gif.services.*;

public class ImageData
{
	private String id;
	private Path path;
	private String shortPath;
	private List<String> tags;
	private boolean starred;
	private List<ServiceLink> links;
	
	private BufferedImage thumbnail;
	
	public ImageData(Path path, String shortPath)
	{
		this.path = path;
		genId(shortPath);
		this.shortPath = id+shortPath.substring(shortPath.lastIndexOf('.'));
		
		this.tags = new ArrayList<>();
		this.starred = false;
		this.links = new ArrayList<>();
		this.thumbnail = null;
	}
	
	public ImageData(String id, Path path, String shortPath, List<String> tags, boolean starred, List<ServiceLink> links, BufferedImage thumbnail)
	{
		this.id = id;
		this.path = path;
		this.shortPath = shortPath;
		this.tags = tags;
		this.starred = starred;
		this.thumbnail = thumbnail;
		this.links = links;
	}
	
	public void destroy()
	{
		thumbnail.flush();
	}
	
	// Accessors
	
	public String getId()
	{
		return id;
	}
	
	public String getPath()
	{
		return shortPath;
	}
	
	public BufferedImage getThumbnail()
	{
		return thumbnail;
	}
	
	public void setThumbnail(BufferedImage thumbnail)
	{
		if(this.thumbnail != null)
			this.thumbnail.flush();
		this.thumbnail = thumbnail;
	}
	
	public List<String> getTags()
	{
		return tags;
	}
	
	public void addTag(String tag)
	{
		tags.add(tag);
	}
	
	public void removeTag(String tag)
	{
		tags.remove(tag);
	}
	
	public boolean isStarred()
	{
		return starred;
	}
	
	public void setStarred(boolean starred)
	{
		this.starred = starred;
	}
	
	public List<ServiceLink> getLinks()
	{
		return links;
	}
	
	public void addLink(ServiceLink link)
	{
		links.add(link);
	}
	
	public int getWidth()
	{
		return thumbnail.getWidth();
	}
	
	public int getHeight()
	{
		return thumbnail.getHeight();
	}
	
	public long getLastModified()
	{
		try
		{
			return Files.getLastModifiedTime(path).toMillis();
		}
		catch(Exception e)
		{
			Log.error("Failed to get last modified attribute for "+path.toString(), e);
			return -1;
		}
	}
	
	public long getSize()
	{
		try
		{
			return Files.size(path);
		}
		catch(Exception e)
		{
			Log.error("Failed to get file size attribute for "+path.toString(), e);
			return -1;
		}
	}
	
	// Helpers
	
	public void genId(String path)
	{
		id = path;
		regenId();
	}
	
	public void regenId()
	{
		int hash = id.hashCode();
		int k = (int)(Math.random()*Integer.SIZE);
		hash = (hash >>> k) | (hash << (Integer.SIZE - k));
		id = Long.toString(Math.abs(hash), 36);
	}
}

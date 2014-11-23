package net.enigmablade.gif.img;

import java.awt.image.*;
import java.util.*;
import net.enigmablade.gif.services.*;

public class ImageData
{
	private String id;
	private String path;
	private List<String> tags;
	private boolean starred = false;
	private List<ServiceLink> links;
	
	private BufferedImage thumbnail;
	
	public ImageData(String path)
	{
		genId(path);
		this.path = id+path.substring(path.lastIndexOf('.'));
		
		this.tags = new ArrayList<>();
		this.links = new ArrayList<>();
		this.thumbnail = null;
	}
	
	public ImageData(String id, String path, List<String> tags, boolean starred, List<ServiceLink> links, BufferedImage thumbnail)
	{
		this.id = id;
		this.path = path;
		this.tags = tags;
		this.thumbnail = thumbnail;
		this.links = links;
	}
	
	public void destroy()
	{
		thumbnail.flush();
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getPath()
	{
		return path;
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
	
	//Helpers
	
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

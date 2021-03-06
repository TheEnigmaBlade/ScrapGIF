package net.enigmablade.gif.img;

import java.awt.image.*;
import java.util.*;
import net.enigmablade.gif.services.*;

public class ImageData
{
	private String id;
	private String fileName;
	private List<String> tags;
	private boolean starred;
	private List<ServiceLink> links;
	
	private BufferedImage thumbnail;
	
	public ImageData(String fileName)
	{
		this(fileName, false);
	}
	
	public ImageData(String fileName, boolean keepFileName)
	{
		this.fileName = fileName;
		genId(fileName);
		if(!keepFileName)
			this.fileName = id+'.'+getType();
		
		this.tags = new ArrayList<>();
		this.starred = false;
		this.links = new ArrayList<>();
		this.thumbnail = null;
	}
	
	public ImageData(String id, String fileName, List<String> tags, boolean starred, List<ServiceLink> links)
	{
		this.id = id;
		this.fileName = fileName;
		this.tags = tags;
		this.starred = starred;
		this.thumbnail = null;
		this.links = links;
	}
	
	public void destroy()
	{
		if(thumbnail != null)
			thumbnail.flush();
	}
	
	// Accessors
	
	public String getId()
	{
		return id;
	}
	
	public String getPath()
	{
		return fileName;
	}
	
	public String getType()
	{
		return fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();
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
		if(thumbnail == null)
			return 0;
		return thumbnail.getWidth();
	}
	
	public int getWidth(int height)
	{
		return (int)(getAspectRatio()*height);
	}
	
	public int getHeight()
	{
		if(thumbnail == null)
			return 0;
		return thumbnail.getHeight();
	}
	
	public double getAspectRatio()
	{
		return 1.0*getWidth()/getHeight();
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
	
	// Other
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof ImageData))
			return false;
		ImageData d = (ImageData)o;
		return id.equals(d.id);
	}
}


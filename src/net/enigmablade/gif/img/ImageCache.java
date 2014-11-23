package net.enigmablade.gif.img;

import java.util.*;

public class ImageCache extends LinkedHashMap<String, ImageFrame[]>
{
	private static final int DEFAULT_SIZE = 10;
	
	private int maxSize;
	
	public ImageCache()
	{
		this(DEFAULT_SIZE);
	}
	
	public ImageCache(int maxSize)
	{
		this.maxSize = maxSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<String, ImageFrame[]> eldest)
	{
		if(size() > maxSize)
		{
			for(ImageFrame frame : eldest.getValue())
				frame.destroy();
			return true;
		}
		return false;
	}
}

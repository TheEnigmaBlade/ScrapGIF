package net.enigmablade.gif.img;

import java.util.*;

public class ImageCache
{
	private static final int DEFAULT_SIZE = 4;
	
	//private Map<String, ImageFrame[]> cache;
	private LinkedList<CacheThing> cache;
	private int capacity;
	
	public ImageCache()
	{
		this(DEFAULT_SIZE);
	}
	
	public ImageCache(final int capacity)
	{
		/*cache = new LinkedHashMap<String, ImageFrame[]>(maxSize, 0.75f, true) {
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
		};
		cache = Collections.synchronizedMap(cache);*/
		
		this.capacity = capacity;
		cache = new LinkedList<>();
	}
	
	public synchronized ImageFrame[] get(String key)
	{
		//return cache.get(key);
		
		// Get and remove thing
		CacheThing thing = null;
		for(Iterator<CacheThing> it = cache.iterator(); it.hasNext();)
		{
			CacheThing checkThing = it.next();
			if(checkThing.key.equals(key))
			{
				thing = checkThing;
				it.remove();
				break;
			}
		}
		
		// Add and return thing
		if(thing != null)
		{
			cache.addFirst(thing);
			return thing.value;
		}
		return null;
	}
	
	public synchronized void store(String key, ImageFrame[] frames)
	{
		//cache.put(key, frames);
		cache.addFirst(new CacheThing(key, frames));
		if(cache.size() > capacity)
		{
			CacheThing thing = cache.removeLast();
			for(ImageFrame frame : thing.value)
				frame.destroy();
			thing.value = null;
		}
	}
	
	// Helper classes
	
	private static class CacheThing
	{
		public String key;
		public ImageFrame[] value;
		
		public CacheThing(String key, ImageFrame[] value)
		{
			this.key = key;
			this.value = value;
		}
	}
}

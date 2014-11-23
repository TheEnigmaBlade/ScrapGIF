package net.enigmablade.gif.search;

import java.util.*;

public class TagCache extends AbstractSet<String> implements Set<String>
{
	private static final int DEFAULT_SIZE = 10;
	
	private LinkedHashMap<String, Object> tags;
	private int size;
	
	public TagCache()
	{
		this(DEFAULT_SIZE);
	}
	
	public TagCache(int size)
	{
		this.size = size;
		
		tags = new LinkedHashMap<String, Object>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Object> eldest)
			{
				return size() > TagCache.this.size;
			}
		};
	}
	
	@Override
	public boolean add(String tag)
	{
		Object prev = tags.putIfAbsent(tag, new Object());
		return prev == null;
	}
	
	@Override
	public void clear()
	{
		tags.clear();
	}
	
	@Override
	public Iterator<String> iterator()
	{
		return tags.keySet().iterator();
	}

	@Override
	public int size()
	{
		return tags.size();
	}
	
	public String toPropertyString()
	{
		StringBuilder val = new StringBuilder();
		Iterator<String> it = tags.keySet().iterator();
		for(int n = 0; n < tags.size(); n++)
		{
			val.append(it.next());
			if(n < tags.size()-1)
				val.append(',');
		}
		return val.toString();
	}
}

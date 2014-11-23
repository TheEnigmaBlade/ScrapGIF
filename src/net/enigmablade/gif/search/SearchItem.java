package net.enigmablade.gif.search;

import java.util.*;
import net.enigmablade.gif.img.*;

public class SearchItem
{
	private ImageData data;
	
	private Set<String> tags;
	
	public SearchItem(ImageData data)
	{
		this.data = data;
		
		tags = new HashSet<>(data.getTags());
		for(String tag : data.getTags())
		{
			String[] newTags = SearchManager.TAG_SPLIT_REGEX.split(tag);
			for(int n = 1; n < newTags.length; n++)
				tags.add(newTags[n]);
		}
	}
	
	public boolean query(String query)
	{
		for(String tag : tags)
			if(tag.startsWith(query))
				return true;
		return false;
	}
	
	public ImageData getData()
	{
		return data;
	}
}

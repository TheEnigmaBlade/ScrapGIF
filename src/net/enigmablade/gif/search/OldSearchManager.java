package net.enigmablade.gif.search;

import java.util.*;
import java.util.stream.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;

public class OldSearchManager
{
	private List<SearchItem> currentItems, removedItems;
	
	public OldSearchManager()
	{
		currentItems = new LinkedList<>();
		removedItems = new LinkedList<>();
	}
	
	public void buildSearchFromLibrary(Library library)
	{
		currentItems.clear();
		
		for(ImageData data : library.getImages())
			currentItems.add(new SearchItem(data));
	}
	
	public List<ImageData> addToQuery(String addition)
	{
		for(Iterator<SearchItem> it = currentItems.iterator(); it.hasNext();)
		{
			SearchItem item = it.next();
			if(!item.query(addition))
			{
				it.remove();
				removedItems.add(item);
			}
		}
		
		return currentItems.stream().map((searchItem) -> searchItem.getData()).collect(Collectors.toList());
	}
	
	public List<ImageData> removeFromQuery(String query)
	{
		currentItems.addAll(removedItems);
		removedItems.clear();
		currentItems.forEach(item -> item.reset());
		
		return addToQuery(query);
	}
}

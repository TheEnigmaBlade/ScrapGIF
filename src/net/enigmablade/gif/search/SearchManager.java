package net.enigmablade.gif.search;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.search.filters.*;

public class SearchManager
{
	public static final Pattern TAG_SPLIT_REGEX = Pattern.compile("[ _.,\\-:;]");
	
	private Deque<List<SearchItem>> states;		//Stack of states
	private List<SearchItem> initialState;
	private String query;
	private Set<String> queryParts;
	
	// Initialization
	
	public SearchManager()
	{
		states = new LinkedList<>();
		query = "";
		queryParts = new HashSet<>();
		
		filters = new HashSet<>();
	}
	
	public void buildSearchFromLibrary(Library library)
	{
		states.clear();
		
		initialState = new LinkedList<>();
		for(ImageData data : library.getImages())
			initialState.add(new SearchItem(data));
		states.push(initialState);
	}
	
	// String search
	
	public List<ImageData> addToQuery(String newQuery, boolean addedToEnd)
	{
		Objects.requireNonNull(newQuery, "You need to have a query");
		//if(newQuery.length() == 0 || newQuery.length() <= query.length())
		//	throw new IllegalArgumentException("You need to have an adding query");
		
		System.out.println("ADDING, to end="+addedToEnd);
		
		String diff = null;
		if(addedToEnd)
			diff = newQuery.substring(query.length());
		else
		{
			query = "";
			diff = newQuery;
			states.clear();
			states.push(initialState);
		}
		
		List<SearchItem> newState = initialState;
		for(int n = 0; n < diff.length() || (n == 0 && hasFilters()); n++)
		{
			if(diff.length() > 0)
			{
				query += diff.charAt(n);
				System.out.println("New query: "+query);
				queryParts = splitString(query);
				System.out.println("Query parts: "+queryParts.toString());
			}
			
			List<SearchItem> currentState = states.peek();
			
			System.out.println("Old state size: "+currentState.size());
			newState = new LinkedList<>();
			items: for(SearchItem item : currentState)
			{
				if(!testFilters(item))
					continue items;
				
				for(String queryPart : queryParts)
				{
					if(!item.query(queryPart))
						continue items;
				}
				newState.add(item);
			}
			states.push(newState);
			System.out.println("New state size: "+newState.size());
		}
		
		return getImageData(newState);
	}
	
	public List<ImageData> removeFromQuery(String newQuery, boolean removedFromEnd)
	{
		Objects.requireNonNull(query, "You need to have a query");
		//if(newQuery.length() == 0 || newQuery.length() >= query.length())
		//	throw new IllegalArgumentException("You need to have a removing query");
		
		System.out.println("REMOVING, from end="+removedFromEnd);
		query = newQuery;
		System.out.println("New query: "+query);
		queryParts = splitString(query);
		System.out.println("Query parts: "+queryParts.toString());
		
		if(removedFromEnd)
		{
			List<SearchItem> newState = null;
			for(int n = 0; n < query.length()-newQuery.length(); n++)
			{
				//Remove old (current) state
				states.pop();
				//Get previous state
				newState = states.peek();
			}
			System.out.println("New state size: "+newState.size());
			
			return getImageData(newState);
		}
		
		return addToQuery(newQuery, false);
	}
	
	//// Helpers
	
	private static Set<String> splitString(String str)
	{
		return Arrays.stream(TAG_SPLIT_REGEX.split(str)).filter(part -> part.length() > 0).collect(Collectors.toSet());
	}
	
	private static List<ImageData> getImageData(List<SearchItem> items)
	{
		if(items == null)
			return new LinkedList<>();
		return items.stream().map((searchItem) -> searchItem.getData()).collect(Collectors.toList());
	}
	
	// Filters
	
	private Set<SearchFilter> filters;
	
	public static final SearchFilter FAVORITE_FILTER = new FavoriteFilter();
	public static final SearchFilter UNTAGGED_FILTER = new UntaggedFilter();
	
	public List<ImageData> addFilter(SearchFilter filter)
	{
		if(filters.add(filter))
			return filtersChanged();
		return getImageData(states.getLast());
	}
	
	public List<ImageData> removeFilter(SearchFilter filter)
	{
		if(filters.remove(filter))
			return filtersChanged();
		return getImageData(states.getLast());
	}
	
	private List<ImageData> filtersChanged()
	{
		return removeFromQuery(query, false);
	}
	
	private boolean hasFilters()
	{
		return !filters.isEmpty();
	}
	
	private boolean testFilters(SearchItem i)
	{
		ImageData d = i.getData();
		for(SearchFilter f : filters)
			if(!f.test(d))
				return false;
		return true;
	}
}

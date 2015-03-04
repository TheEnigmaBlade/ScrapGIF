package net.enigmablade.gif.search.filters;

import net.enigmablade.gif.img.*;

public class FavoriteFilter implements SearchFilter
{
	@Override
	public boolean test(ImageData i)
	{
		return i.isStarred();
	}
}

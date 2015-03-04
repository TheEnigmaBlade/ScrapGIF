package net.enigmablade.gif.search.filters;

import net.enigmablade.gif.img.*;

public class UntaggedFilter implements SearchFilter
{
	@Override
	public boolean test(ImageData i)
	{
		return i.getTags().isEmpty();
	}
}

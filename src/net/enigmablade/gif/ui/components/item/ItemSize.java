package net.enigmablade.gif.ui.components.item;

import net.enigmablade.gif.*;

public enum ItemSize
{
	SMALL(GifConstants.THUMBNAIL_SIZE_SMALL, GifConstants.THUMBNAIL_LOAD_SMALL),
	NORMAL(GifConstants.THUMBNAIL_SIZE_NORMAL, GifConstants.THUMBNAIL_LOAD_NORMAL),
	LARGE(GifConstants.THUMBNAIL_SIZE_LARGE, GifConstants.THUMBNAIL_LOAD_LARGE);
	
	/* --- */
	
	private int size, loadSize;
	
	private ItemSize(int size, int loadSize)
	{
		this.size = size;
		this.loadSize = loadSize;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public int getLoadSize()
	{
		return loadSize;
	}
}

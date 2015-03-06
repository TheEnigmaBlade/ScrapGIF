package net.enigmablade.gif.ui.components.item;

import net.enigmablade.gif.ui.*;

public enum ItemSize
{
	SMALL(UIConstants.THUMBNAIL_SIZE_SMALL, UIConstants.THUMBNAIL_LOAD_SMALL),
	NORMAL(UIConstants.THUMBNAIL_SIZE_NORMAL, UIConstants.THUMBNAIL_LOAD_NORMAL),
	LARGE(UIConstants.THUMBNAIL_SIZE_LARGE, UIConstants.THUMBNAIL_LOAD_LARGE);
	
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

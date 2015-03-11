package net.enigmablade.gif.ui.components.item;

import net.enigmablade.gif.ui.*;

public enum ItemSize
{
	SMALL(UIConstants.THUMBNAIL_SIZE_SMALL, UIConstants.THUMBNAIL_LOAD_SMALL, UIConstants.IMAGE_GAP_SMALL),
	NORMAL(UIConstants.THUMBNAIL_SIZE_NORMAL, UIConstants.THUMBNAIL_LOAD_NORMAL, UIConstants.IMAGE_GAP_NORMAL),
	LARGE(UIConstants.THUMBNAIL_SIZE_LARGE, UIConstants.THUMBNAIL_LOAD_LARGE, UIConstants.IMAGE_GAP_LARGE);
	
	/* --- */
	
	private int size, loadSize, gap;
	
	private ItemSize(int size, int loadSize, int gap)
	{
		this.size = size;
		this.loadSize = loadSize;
		this.gap = gap;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public int getLoadSize()
	{
		return loadSize;
	}
	
	public int getGap()
	{
		return gap;
	}
}

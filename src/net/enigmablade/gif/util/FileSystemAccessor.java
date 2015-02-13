package net.enigmablade.gif.util;

import net.enigmablade.gif.img.*;

public interface FileSystemAccessor
{
	public long getImageLastModified(ImageData image);
	
	public long getImageSize(ImageData image);
}

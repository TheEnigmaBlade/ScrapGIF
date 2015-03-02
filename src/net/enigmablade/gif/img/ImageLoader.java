package net.enigmablade.gif.img;

import java.io.*;
import java.util.*;

public abstract class ImageLoader
{
	public static final FilenameFilter IMAGE_FILTER = (File dir, String name) -> {
		String ext = name.substring(name.lastIndexOf('.')+1).toLowerCase();
		return ImageLoaders.supportsImageType(ext);
	};
	
	public static final FileFilter IMAGE_FILE_FILTER = (File file) -> {
		return IMAGE_FILTER.accept(file.getParentFile(), file.getName());
	};
	
	//Loader stuff
	
	private List<String> fileTypes;
	
	public ImageLoader(String... fileTypes)
	{
		this.fileTypes = Arrays.asList(fileTypes);
	}
	
	public ImageFrame[] loadImage(File file)
	{
		return loadImage(file, false);
	}
	
	public ImageFrame loadImageStill(File file)
	{
		ImageFrame[] frames = loadImage(file, true);
		if(frames == null || frames.length == 0)
			return null;
		return frames[0];
	}
	
	protected abstract ImageFrame[] loadImage(File file, boolean onlyFirstFrame);
	
	// Accessors
	
	public List<String> getFileTypes()
	{
		return Collections.unmodifiableList(fileTypes);
	}
	
	public boolean supportsFileType(String fileType)
	{
		return fileTypes.contains(fileType);
	}
}

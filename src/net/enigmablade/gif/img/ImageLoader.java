package net.enigmablade.gif.img;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import org.imgscalr.*;
import com.alee.log.*;
import net.enigmablade.gif.img.gif.*;
import net.enigmablade.gif.library.*;

public abstract class ImageLoader
{
	public static final FilenameFilter IMAGE_FILTER = (File dir, String name) -> {
		String ext = name.substring(name.lastIndexOf('.')+1).toLowerCase();
		switch(ext)
		{
			case "gif": return true;
			default: return false;
		}
	};
	
	//Per-type instancing
	
	public static ImageLoader getInstance(String path)
	{
		String ext = path.substring(path.lastIndexOf('.')+1).toLowerCase();
		switch(ext)
		{
			case "gif": return new GifLoader(path);
			default: return null;
		}
	}
	
	//Loader stuff
	
	protected String path;
	
	public ImageLoader(String path)
	{
		this.path = path;
	}
	
	public ImageFrame[] readFull()
	{
		return read(path, false);
	}
	
	public ImageFrame readFirstFrame()
	{
		ImageFrame[] frames = read(path, true);
		if(frames == null || frames.length == 0)
			return null;
		return frames[0];
	}
	
	protected abstract ImageFrame[] read(String path, boolean onlyFirstFrame);
	
	//Thumbnails
	
	private static final String THUMBNAIL_PATH = "cache/thumbnails/";
	private static final int THUMBNAIL_MAX_SIZE = 200;
	
	public static BufferedImage getThumbnail(Library library, String imageId, String imagePath)
	{
		File thumbFile = getThumbnailFile(library, imageId);
		if(thumbFile.exists())
		{
			try
			{
				return ImageIO.read(thumbFile);
			}
			catch(IOException e)
			{
				Log.error("Failed to load existing thumbnail", e);
			}
		}
		
		ImageLoader imgLoad = ImageLoader.getInstance(library.getImagePath(imagePath));
		ImageFrame staticFrame = imgLoad.readFirstFrame();
		if(staticFrame == null)
		{
			Log.error("Failed to create thumbnail");
			return null;
		}
		
		BufferedImage thumbnail = scale(staticFrame.getImage());
		saveThumbnail(thumbnail, thumbFile);
		return thumbnail;
	}
	
	private static void saveThumbnail(BufferedImage thumbnail, File file)
	{
		try
		{
			file.getParentFile().mkdirs();
			ImageIO.write(thumbnail, "png", file);
		}
		catch(IOException e)
		{
			Log.error("Failed to save thumbnail", e);
		}
	}
	
	//Helpers
	
	private static File getThumbnailFile(Library library, String imageId)
	{
		return new File(THUMBNAIL_PATH+library.getId()+"/"+imageId+".png");
	}
	
	public static BufferedImage scale(BufferedImage image)
	{
		return Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, THUMBNAIL_MAX_SIZE);
	}
}

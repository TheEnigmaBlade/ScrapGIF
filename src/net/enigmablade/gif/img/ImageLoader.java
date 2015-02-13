package net.enigmablade.gif.img;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import com.alee.log.*;
import com.alee.utils.*;
import com.mortennobel.imagescaling.*;
import net.enigmablade.gif.*;
import net.enigmablade.gif.library.*;

public abstract class ImageLoader
{
	public static final Map<String, String> supportedFiletypes;
	static
	{
		supportedFiletypes = new HashMap<>();
		supportedFiletypes.put("gif", "net.enigmablade.gif.img.gif.GifLoader");
		supportedFiletypes.put("webm", "net.enigmablade.gif.img.webm.WebMLoader");
	}
	
	public static final FilenameFilter IMAGE_FILTER = (File dir, String name) -> {
		String ext = name.substring(name.lastIndexOf('.')+1).toLowerCase();
		return supportedFiletypes.containsKey(ext);
	};
	
	public static final FileFilter IMAGE_FILE_FILTER = (File file) -> {
		return IMAGE_FILTER.accept(file.getParentFile(), file.getName());
	};
	
	//Per-type instancing
	
	public static ImageLoader getInstance(String path)
	{
		String ext = path.substring(path.lastIndexOf('.')+1).toLowerCase();
		String className = supportedFiletypes.get(ext);
		if(className != null)
		{
			try
			{
				return ReflectUtils.createInstance(className, path);
			}
			catch(Exception e)
			{
				Log.error("Failed to create instance of image loader", e);
			}
		}
		
		return null;
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
		
		ImageLoader imgLoad = ImageLoader.getInstance(library.getImagePath(imagePath).toString());
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
		ThumpnailRescaleOp op = new ThumpnailRescaleOp(DimensionConstrain.createMaxDimension(Integer.MAX_VALUE, GifConstants.THUMBNAIL_SIZE_NORMAL));
		return op.filter(image, null);
		//return Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, GifConstants.THUMBNAIL_SIZE_NORMAL);
	}
}

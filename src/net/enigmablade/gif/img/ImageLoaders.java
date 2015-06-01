package net.enigmablade.gif.img;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import javax.imageio.*;
import com.alee.log.*;
import com.alee.utils.*;
import com.mortennobel.imagescaling.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.util.*;

public class ImageLoaders
{
	private static Map<String, ImageLoader> loaders;
	
	static
	{
		loaders = new HashMap<>();
		registerLoader("net.enigmablade.gif.img.gif.GifLoader");
		registerLoader("net.enigmablade.gif.img.webm.WebMLoader");
	}
	
	// Registration methods
	
	public static void registerLoader(String className)
	{
		Objects.requireNonNull(className);
		
		try
		{
			ImageLoader loader = ReflectUtils.createInstance(className);
			for(String fileType : loader.getFileTypes())
				loaders.put(fileType, loader);
		}
		catch(Exception e)
		{
			Log.error("Failed to create instance of image loader", e);
		}
	}
	
	public static void deregisterLoader(String fileType)
	{
		Objects.requireNonNull(fileType);
		loaders.remove(fileType);
	}
	
	// Instancing
	
	public static ImageLoader getLoader(String fileType)
	{
		if(loaders.containsKey(fileType))
			return loaders.get(fileType);
		return null;
	}
	
	// Accessors
	
	public static boolean supportsImageExtension(String ext)
	{
		return loaders.containsKey(ext);
	}
	
	public static Set<String> getSupportedExtensions()
	{
		return Collections.unmodifiableSet(loaders.keySet());
	}
	
	public static List<String> getSupportedTypes()
	{
		return loaders.values().stream().map((loader) -> loader.getTypeName()).collect(Collectors.toList());
	}
	
	//Thumbnails
	
	private static final String THUMBNAIL_PATH = "cache/thumbnails/";
	
	public static BufferedImage getThumbnail(Library library, ImageData image)
	{
		File thumbFile = getThumbnailFile(library, image);
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
		
		Path path = library.getImagePath(image);
		String fileType = IOUtil.getFileExtension(path);
		ImageLoader imgLoad = ImageLoaders.getLoader(fileType);
		ImageFrame staticFrame = imgLoad.loadImageStill(path.toFile());
		if(staticFrame == null)
		{
			Log.error("Failed to create thumbnail");
			return null;
		}
		
		BufferedImage thumbnail = scale(staticFrame.getImage(), UIConstants.THUMBNAIL_SIZE_CREATE);
		staticFrame.destroy();
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
	
	private static File getThumbnailFile(Library library, ImageData image)
	{
		return new File(THUMBNAIL_PATH+library.getId()+"/"+image.getId()+".png");
	}
	
	private static BufferedImage scale(BufferedImage image, int size)
	{
		ThumpnailRescaleOp op = new ThumpnailRescaleOp(DimensionConstrain.createMaxDimension(Integer.MAX_VALUE, size));
		return op.filter(image, null);
		//return Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, GifConstants.THUMBNAIL_SIZE_NORMAL);
	}
}

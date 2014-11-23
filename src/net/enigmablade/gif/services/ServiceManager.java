package net.enigmablade.gif.services;

import java.io.*;
import java.util.function.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.services.imgur.*;

public abstract class ServiceManager
{
	private static ServiceManager imgur;
	
	public static ServiceManager getInstance(String type, String imagePath)
	{
		//TODO: image type and size checks
		if(imgur == null)
			imgur = new Imgur();
		return imgur;
	}
	
	public static String createUrl(ServiceLink link)
	{
		ServiceManager manager = ServiceManager.getInstance(link.getType(), null);
		return manager.createUrl(link.getId());
	}
	
	//Manager stuff
	
	public void upload(Library library, ImageData image, BiConsumer<ImageData, ServiceLink> callback)
	{
		Log.info("Uploading image");
		File imageFile = new File(library.getImagePath(image.getPath()));
		if(imageFile.exists() && imageFile.isFile())
		{
			ServiceManager manager = getInstance(null, image.getPath());
			manager.upload(imageFile, image, callback);
		}
	}
	
	public abstract void upload(File file, ImageData image, BiConsumer<ImageData, ServiceLink> callback);
	
	public abstract String createUrl(String id);
}

package net.enigmablade.gif.services;

import java.io.*;
import java.util.function.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.services.imgur.*;
import net.enigmablade.gif.util.*;

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
	
	public void upload(Library library, ImageData image, TriConsumer<ServiceError, ImageData, ServiceLink> doneCallback, Consumer<Integer> progressCallback)
	{
		Log.info("Uploading image");
		File imageFile = new File(library.getImagePath(image.getPath()));
		if(imageFile.exists() && imageFile.isFile())
		{
			ServiceManager manager = getInstance(null, image.getPath());
			manager.upload(imageFile, image, doneCallback, progressCallback);
		}
	}
	
	public abstract void upload(File file, ImageData image, TriConsumer<ServiceError, ImageData, ServiceLink> doneCallback, Consumer<Integer> progressCallback);
	
	public abstract String createUrl(String id);
}

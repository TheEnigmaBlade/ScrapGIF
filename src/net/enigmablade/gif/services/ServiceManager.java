package net.enigmablade.gif.services;

import java.util.*;
import com.alee.log.*;
import com.alee.utils.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.util.*;

public abstract class ServiceManager
{
	public static Map<String, Service> supportedServices;
	static
	{
		supportedServices = new HashMap<>();
		registerService("net.enigmablade.gif.services.imgur.Imgur");
		registerService("net.enigmablade.gif.services.gfycat.Gfycat");
		registerService("net.enigmablade.gif.services.pomfse.PomfSe");
	}
	
	public static void registerService(String className)
	{
		try
		{
			Service service = ReflectUtils.createInstance(className);
			if(!supportedServices.containsKey(service.getId()))
				supportedServices.put(service.getId(), service);
		}
		catch(Exception e)
		{
			Log.error("Failed to create instance of image loader", e);
		}
	}
	
	public static Service getService(String preferred, ImageData imageData, FileSystemAccessor fileSystem)
	{
		return getService(Collections.singletonList(preferred), imageData, fileSystem);
	}
	
	public static Service getService(List<String> preferred, ImageData imageData, FileSystemAccessor fileSystem)
	{
		// Search preferred services
		for(String pref : preferred)
		{
			Service service = supportedServices.get(pref);
			
			// Check image parameters if we need to
			if(imageData != null)
			{
				// Check the preferred service if found
				if(service != null)
				{
					if(service.accepts(imageData, fileSystem))
						return service;
				}
			}
			else
			{
				return service;
			}
		}
		
		// Find another service
		if(imageData != null)
		{
			for(Service s2 : supportedServices.values())
			{
				if(s2.accepts(imageData, fileSystem))
					return s2;
			}
		}
		
		return null;
	}
	
	public static Collection<Service> getServices()
	{
		return Collections.unmodifiableSet(new HashSet<Service>(supportedServices.values()));
	}
	
	/*public ServiceError upload(String preferredService, Library library, ImageData image, TriConsumer<ServiceError, ImageData, ServiceLink> doneCallback, Consumer<Integer> progressCallback)
	{
		Log.info("Uploading image");
		File imageFile = library.getImagePath(image.getPath()).toFile();
		if(imageFile.exists() && imageFile.isFile())
		{
			Service manager = getService(preferredService, image);
			if(manager == null)
				return ServiceError.NO_SERVICE;
			manager.upload(imageFile, image, doneCallback, progressCallback);
			return ServiceError.NONE;
		}
		
		return ServiceError.NO_FILE;
	}*/
	
	public static String createUrl(ServiceLink link)
	{
		Service manager = ServiceManager.getService(link.getService(), null, null);
		if(manager == null)
			return null;
		return manager.createUrl(link);
	}
}

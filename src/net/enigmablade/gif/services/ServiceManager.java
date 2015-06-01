package net.enigmablade.gif.services;

import java.util.*;
import com.alee.log.*;
import com.alee.utils.*;
import net.enigmablade.gif.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.util.*;

public abstract class ServiceManager
{
	private static Map<String, Service> supportedServices;
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
	
	public static Service getService(String name)
	{
		return supportedServices.get(name);
	}
	
	public static boolean checkService(Service service, ImageData imageData, FileSystemAccessor fileSystem)
	{
		if(service != null)
			return service.accepts(imageData, fileSystem);
		return false;
	}
	
	public static Service getService(Config config, ImageData imageData, FileSystemAccessor fileSystem)
	{
		// Find and check the preferred service if possible
		if(imageData != null)
		{
			String preferred = config.getPreferredService(IOUtil.getFileExtension(imageData.getPath()));
			if(preferred != null)
			{
				// Check preferred service
				Service service = getService(preferred);
				if(checkService(service, imageData, fileSystem))
					return service;
			}
		}
		
		// Find another service
		if(imageData != null)
		{
			for(Service s2 : supportedServices.values())
			{
				if(checkService(s2, imageData, fileSystem))
					return s2;
			}
		}
		
		return null;
	}
	
	public static List<Service> getSupportedServices(String ext)
	{
		ext = Objects.requireNonNull(ext).toLowerCase();
		
		List<Service> services = new LinkedList<>();
		for(Service s : supportedServices.values())
		{
			Set<String> exts = s.getFileExtensions();
			if(exts.isEmpty() || exts.contains(ext))
				services.add(s);
		}
		
		return services;
	}
	
	public static Set<Service> getServices()
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
		Service manager = ServiceManager.getService(link.getService());
		if(manager == null)
		{
			Log.warn("Failed to create URL because a manager doesn't exist");
			return null;
		}
		return manager.createUrl(link);
	}
}

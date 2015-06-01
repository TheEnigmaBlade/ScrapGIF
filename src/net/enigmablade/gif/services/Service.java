package net.enigmablade.gif.services;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.util.*;

public abstract class Service implements Comparable<Service>
{
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";
	
	private String id;
	private String name;
	private String uploadUrl;
	private String formName;
	private long maxFileSize;
	private Set<String> fileTypes;
	
	protected Service(String id, String name, String uploadUrl, String formName)
	{
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
		this.uploadUrl = uploadUrl;
		this.formName = formName;
		this.maxFileSize = 0;
		this.fileTypes = Collections.emptySet();
	}
	
	public boolean accepts(ImageData image, FileSystemAccessor fileSystem)
	{
		if(maxFileSize > 0)
		{
			if(fileSystem.getImageSize(image) > maxFileSize)
				return false;
		}
		
		return fileTypes.size() == 0 || fileTypes.contains(image.getType());
	}
	
	public void upload(File file, ImageData image, TriConsumer<ServiceError, ImageData, ServiceLink> doneCallback, Consumer<Integer> progressCallback)
	{
		Log.info("Uploading to "+name+" ("+id+")");
		
		new ServiceUploadTask(file, this) {
			@Override
			protected void done(String result)
			{
				ServiceLink link = result != null ? link = new ServiceLink(id, result) : null;
				doneCallback.accept(error, image, link);
			}
			
			@Override
			protected void progressed(int val)
			{
				progressCallback.accept(val);
			}
		}.execute();
	}
	
	protected void preUpload() {}
	
	protected abstract boolean authorize(HttpURLConnection connection);
	
	protected abstract Map<String, String> getHeaders();
	
	protected String postUpload(String msg) { return msg; }
	
	protected abstract String handleResult(HttpURLConnection conn, String response);
	
	protected abstract ServiceError handleError(HttpURLConnection conn, String response);
	
	public abstract String createUrl(ServiceLink link);
	
	// Accessor methods
	
	public String getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getUploadUrl()
	{
		return uploadUrl;
	}
	
	public String getFormName()
	{
		return formName;
	}
	
	public void setMaxFileSize(long maxFileSize)
	{
		this.maxFileSize = maxFileSize;
	}
	
	public long getMaxFileSize()
	{
		return maxFileSize;
	}
	
	public void setFileExtensions(String... fileExt)
	{
		this.fileTypes = Arrays.asList(fileExt).stream().map(ft -> ft.toLowerCase()).collect(Collectors.toSet());
	}
	
	public Set<String> getFileExtensions()
	{
		return fileTypes;
	}
	
	// Other methods
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof Service))
			return false;
		Service s = (Service)o;
		return s.id.equals(id);
	}
	
	@Override
	public int compareTo(Service s)
	{
		return name.compareTo(s.name);
	}
}

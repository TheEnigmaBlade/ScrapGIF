package net.enigmablade.gif.services;

public class ServiceLink
{
	private String service;
	private String id;
	
	public ServiceLink(String service, String id)
	{
		this.service = service;
		this.id = id;
	}
	
	public String getService()
	{
		return service;
	}
	
	public String getFile()
	{
		return id;
	}
}

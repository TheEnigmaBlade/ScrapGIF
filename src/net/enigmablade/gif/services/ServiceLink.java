package net.enigmablade.gif.services;

public class ServiceLink
{
	private String type;
	private String id;
	
	public ServiceLink(String type, String id)
	{
		this.type = type;
		this.id = id;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getId()
	{
		return id;
	}
}

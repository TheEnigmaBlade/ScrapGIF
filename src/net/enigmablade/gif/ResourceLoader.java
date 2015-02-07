package net.enigmablade.gif;

import java.net.*;
import javax.swing.*;

public class ResourceLoader
{
	private static final String RESOURCES = "resources/";
	private static final String ICONS = RESOURCES+"icons/";
	
	public static URL getResource(String name)
	{
		return ResourceLoader.class.getClassLoader().getResource(name);
	}
	
	public static ImageIcon loadIcon(String name)
	{
		return new ImageIcon(getResource(ICONS+name+".png"));
	}
}

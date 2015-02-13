package net.enigmablade.gif;

import java.net.*;
import javax.swing.*;

public class ResourceLoader
{
	private static final String RESOURCES = "resources/";
	private static final String ICONS = "icons/";
	//private static final String ANIMATIONS = "";
	
	public static URL getResource(String name)
	{
		return ResourceLoader.class.getClassLoader().getResource(RESOURCES+name);
	}
	
	public static ImageIcon loadIcon(String name)
	{
		return new ImageIcon(getResource(ICONS+name+".png"));
	}
	
	/*public static ImageIcon loadAnimatedIcon(String name)
	{
		return new ImageIcon(getResource(ANIMATIONS+name+".gif"));
	}*/
}

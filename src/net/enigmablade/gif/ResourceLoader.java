package net.enigmablade.gif;

import javax.swing.*;

public class ResourceLoader
{
	private static final String RESOURCES = "resources/";
	private static final String ICONS = RESOURCES+"icons/";
	
	public static ImageIcon loadIcon(String name)
	{
		return new ImageIcon(ICONS+name+".png");
	}
}

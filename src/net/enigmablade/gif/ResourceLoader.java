package net.enigmablade.gif;

import java.net.*;
import javax.sound.sampled.*;
import javax.swing.*;
import com.alee.log.*;

public class ResourceLoader
{
	private static final String RESOURCES = "resources/";
	private static final String ICONS = "icons/";
	private static final String SOUNDS = "sounds/";
	
	public static URL getResource(String name)
	{
		return ResourceLoader.class.getClassLoader().getResource(RESOURCES+name);
	}
	
	public static ImageIcon loadIcon(String name)
	{
		return new ImageIcon(getResource(ICONS+name+".png"));
	}
	
	public static AudioInputStream loadSound(String name)
	{
		try
		{
			return AudioSystem.getAudioInputStream(getResource(SOUNDS+name+".wav"));
		}
		catch(Exception e)
		{
			Log.error("Failed to load sound file", e);
			return null;
		}
	}
}

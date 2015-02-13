package net.enigmablade.gif.util;

import java.util.*;
import java.util.stream.*;
import com.alee.log.*;
import net.enigmablade.gif.*;

public class Config extends Properties
{
	private String name;
	
	public Config(String name)
	{
		this.name = name;
	}
	
	@Override
	public Object setProperty(String key, String value)
	{
		return setProperty(key, value, true);
	}
	
	public Object setProperty(String key, String value, boolean save)
	{
		Log.debug("setProperty: key="+key+", val="+value);
		Object o = super.setProperty(key, value);
		if(save)
			SettingsLoader.saveProperties(name, this);
		return o;
	}
	
	// Config access
	
	public boolean isWindowSizeSet()
	{
		return containsKey("window_width") && containsKey("window_height");
	}
	
	public int getWindowWidth()
	{
		try
		{
			return Integer.parseUnsignedInt(getProperty("window_width"));
		}
		catch(NumberFormatException e)
		{
			Log.error("Invalid window width", e);
			return -1;
		}
	}
	
	public int getWindowHeight()
	{
		try
		{
			return Integer.parseUnsignedInt(getProperty("window_height"));
		}
		catch(NumberFormatException e)
		{
			Log.error("Invalid window height", e);
			return -1;
		}
	}
	
	public void setWindowSize(int width, int height)
	{
		setProperty("window_width", String.valueOf(width), false);
		setProperty("window_height", String.valueOf(height), true);
	}
	
	public Set<String> getLibraries()
	{
		return new HashSet<>(Arrays.asList(getProperty("libraries", "").split(";")));
	}
	
	public void setLibraries(Set<String> libraries)
	{
		setProperty("libraries", libraries.stream().collect(Collectors.joining(";")));
	}
	
	public String getRecentTags()
	{
		return getProperty("recent_tags");
	}
	
	public void setRecentTags(String tags)
	{
		setProperty("recent_tags", tags);
	}
	
	public List<String> getPreferredServices()
	{
		return Arrays.asList(getProperty("pref_service", "").split(";"));
	}
	
	public void setPreferredServices(Set<String> services)
	{
		setProperty("pref_service", services.stream().collect(Collectors.joining(";")));
	}
}

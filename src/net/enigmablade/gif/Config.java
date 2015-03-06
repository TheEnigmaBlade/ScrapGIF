package net.enigmablade.gif;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import com.alee.log.*;
import net.enigmablade.gif.ui.components.item.*;

public class Config extends Properties
{
	private String name;
	
	public Config(String name)
	{
		this.name = name;
	}
	
	@Override
	public synchronized Object setProperty(String key, String value)
	{
		return setProperty(key, value, true);
	}
	
	public synchronized Object setProperty(String key, String value, boolean save)
	{
		Log.debug("setProperty: key="+key+", val="+value);
		Object o = super.setProperty(key, value);
		if(save)
			SettingsLoader.saveProperties(name, this);
		return o;
	}
	
	public synchronized int getIntProperty(String key, int defaultValue)
	{
		try
		{
			String value = getProperty(key);
			if(value == null)
				return defaultValue;
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e)
		{
			Log.error("Invalid int property", e);
			return defaultValue;
		}
	}
	
	public synchronized boolean getBooleanProperty(String key, boolean defaultValue)
	{
		String value = getProperty(key);
		if("true".equalsIgnoreCase(value))
			return true;
		if("false".equalsIgnoreCase(value))
			return false;
		return defaultValue;
	}
	
	// Config access
	
	//// UI
	
	public synchronized boolean useNativeFrame()
	{
		return getBooleanProperty("use_native_frame", false);
	}
	
	public synchronized void setUseNativeFrame(boolean use)
	{
		setProperty("use_native_frame", String.valueOf(use));
	}
	
	public synchronized boolean isWindowSizeSet()
	{
		return containsKey("window_width") && containsKey("window_height");
	}
	
	public synchronized int getWindowWidth()
	{
		return getIntProperty("window_width", -1);
	}
	
	public synchronized int getWindowHeight()
	{
		return getIntProperty("window_height", -1);
	}
	
	public synchronized void setWindowSize(int width, int height)
	{
		setProperty("window_width", String.valueOf(width), false);
		setProperty("window_height", String.valueOf(height), true);
	}
	
	public synchronized ItemSize getImageSize()
	{
		try
		{
			return ItemSize.valueOf(getProperty("image_size", "normal").toUpperCase());
		}
		catch(IllegalArgumentException e)
		{
			Log.error("Item size value doesn't exist", e);
			return ItemSize.NORMAL;
		}
	}
	
	public synchronized void setImageSize(ItemSize size)
	{
		setProperty("image_size", size.name().toLowerCase());
	}
	
	/*public synchronized boolean isShowStarredOnly()
	{
		return getBooleanProperty("show_starred_only", false);
	}
	
	public synchronized void setShowStarredOnly(boolean showStarred)
	{
		setProperty("show_starred_only", String.valueOf(showStarred));
	}
	
	public synchronized boolean isShowUntaggedOnly()
	{
		return getBooleanProperty("show_untagged_only", false);
	}
	
	public synchronized void setShowUntaggedOnly(boolean showUntagged)
	{
		setProperty("show_untagged_only", String.valueOf(showUntagged));
	}*/
	
	//// Data
	
	public synchronized Set<String> getLibraries()
	{
		return new HashSet<>(Arrays.asList(getProperty("libraries", "").split(";")));
	}
	
	public synchronized void setLibraries(Set<Path> libraries)
	{
		setProperty("libraries", libraries.stream().map(Path::toString).collect(Collectors.joining(";")));
	}
	
	public synchronized String getRecentTags()
	{
		return getProperty("recent_tags");
	}
	
	public synchronized void setRecentTags(String tags)
	{
		setProperty("recent_tags", tags);
	}
	
	public synchronized List<String> getPreferredServices()
	{
		return Arrays.asList(getProperty("pref_service", "").split(";"));
	}
	
	public synchronized void setPreferredServices(Set<String> services)
	{
		setProperty("pref_service", services.stream().collect(Collectors.joining(";")));
	}
	
	public synchronized boolean isCheckNewImages()
	{
		return getBooleanProperty("check_new_images", true);
	}
	
	public synchronized void setCheckNewImages(boolean check)
	{
		setProperty("check_new_images", String.valueOf(check));
	}
	
	public synchronized boolean isSoundEffectsEnabled()
	{
		return getBooleanProperty("sound_effects", true);
	}
	
	public synchronized void setSoundEffectsEnabled(boolean enabled)
	{
		setProperty("sound_effects", String.valueOf(enabled));
	}
}

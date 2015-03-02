package net.enigmablade.gif;

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
	
	public int getIntProperty(String key, int defaultValue)
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
	
	public boolean getBooleanProperty(String key, boolean defaultValue)
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
	
	public boolean isWindowSizeSet()
	{
		return containsKey("window_width") && containsKey("window_height");
	}
	
	public int getWindowWidth()
	{
		return getIntProperty("window_width", -1);
	}
	
	public int getWindowHeight()
	{
		return getIntProperty("window_height", -1);
	}
	
	public void setWindowSize(int width, int height)
	{
		setProperty("window_width", String.valueOf(width), false);
		setProperty("window_height", String.valueOf(height), true);
	}
	
	public ItemSize getImageSize()
	{
		//TODO
		return ItemSize.NORMAL;
	}
	
	public void setImageSize(ItemSize size)
	{
		//TODO
	}
	
	/*public boolean isShowStarredOnly()
	{
		return getBooleanProperty("show_starred_only", false);
	}
	
	public void setShowStarredOnly(boolean showStarred)
	{
		setProperty("show_starred_only", String.valueOf(showStarred));
	}
	
	public boolean isShowUntaggedOnly()
	{
		return getBooleanProperty("show_untagged_only", false);
	}
	
	public void setShowUntaggedOnly(boolean showUntagged)
	{
		setProperty("show_untagged_only", String.valueOf(showUntagged));
	}*/
	
	//// Data
	
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
	
	public boolean isCheckNewImages()
	{
		return getBooleanProperty("check_new_images", true);
	}
	
	public void setCheckNewImages(boolean check)
	{
		setProperty("check_new_images", String.valueOf(check));
	}
	
	public boolean isSoundEffectsEnabled()
	{
		return getBooleanProperty("sound_effects", true);
	}
	
	public void setSoundEffectsEnabled(boolean enabled)
	{
		setProperty("sound_effects", String.valueOf(enabled));
	}
}

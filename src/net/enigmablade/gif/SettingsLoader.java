package net.enigmablade.gif;

import java.io.*;
import java.util.*;
import com.alee.log.*;
import net.enigmablade.gif.util.*;
import net.enigmablade.jsonic.*;

public class SettingsLoader
{
	private static final String SETTINGS_FOLDER = "settings/";
	
	public static Properties getServiceSettings(String name)
	{
		return getConfig("services/"+name);
	}
	
	public static JsonObject getJson(String name)
	{
		File file = getFile(name+".json");
		if(!file.exists())
		{
			Log.warn("Settings file not found: "+file.getPath());
			return null;
		}
		
		try
		{
			return JsonParser.parseObject(file);
		}
		catch(JsonParseException | IOException e)
		{
			Log.error("Failed to load JSON settings", e);
			return null;
		}
	}
	
	public static boolean saveJson(String name, JsonElement json)
	{
		File file = setupWrite(name+".json");
		return IOUtil.writeFile(file, json.getJSON());
	}
	
	public static Config getConfig(String name)
	{
		File file = getFile(name+".properties");
		Config p = new Config(name);
		
		if(!file.exists())
		{
			Log.warn("Settings file not found: "+file.getPath());
			return p;
		}
		
		try(InputStream in = new FileInputStream(file))
		{
			p.load(in);
		}
		catch(IOException e)
		{
			Log.error("Failed to load properties", e);
		}
		return p;
	}
	
	public static boolean saveProperties(String name, Properties properties)
	{
		File file = setupWrite(name+".properties");
		try(Writer out = new FileWriter(file))
		{
			properties.store(out, "GIF Organizer configuration file");
			return true;
		}
		catch(IOException e)
		{
			Log.error("Failed to write to file", e);
			return false;
		}
	}
	
	public static File getFile(String name)
	{
		return new File(SETTINGS_FOLDER+name);
	}
	
	public static File setupWrite(String name)
	{
		return IOUtil.setupWrite(getFile(name));
	}
}

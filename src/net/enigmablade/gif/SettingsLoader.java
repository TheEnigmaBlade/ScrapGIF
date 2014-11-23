package net.enigmablade.gif;

import java.io.*;
import java.util.*;
import com.alee.log.*;
import net.enigmablade.jsonic.*;

public class SettingsLoader
{
	private static final String SETTINGS_FOLDER = "settings/";
	
	public static File[] getLibraries()
	{
		File libraryDir = getFile("libraries");
		if(!libraryDir.exists() || !libraryDir.isDirectory())
			return new File[0];
		return libraryDir.listFiles((File dir, String name) -> name.endsWith(".json"));
	}
	
	public static boolean saveLibrary(String name, JsonElement json)
	{
		return saveJson("libraries/"+name, json);
	}
	
	public static Properties getServiceSettings(String name)
	{
		return getProperties("services/"+name);
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
		String jsonStr = json.getJSON();
		try(PrintStream out = new PrintStream(file))
		{
			out.print(jsonStr);
			out.flush();
			return true;
		}
		catch(IOException e)
		{
			Log.error("Failed to write to file", e);
			return false;
		}
	}
	
	public static Properties getProperties(String name)
	{
		File file = getFile(name+".properties");
		Properties p = new Properties();
		
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
		File file = getFile(name);
		if(!file.exists())
		{
			file.getParentFile().mkdirs();
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				Log.error("Failed to create file", e);
				return null;
			}
		}
		return file;
	}
}

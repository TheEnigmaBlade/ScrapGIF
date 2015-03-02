package net.enigmablade.gif.util;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import com.alee.log.*;

public class IOUtil
{
	public static String getFileName(String name)
	{
		return name.substring(0, name.lastIndexOf('.'));
	}
	
	public static String getFileName(Path path)
	{
		return getFileName(path.toFile());
	}
	
	public static String getFileName(File file)
	{
		return getFileName(file.getName());
	}
	
	public static String getFileExtension(String name)
	{
		return name.substring(name.lastIndexOf('.')+1);
	}
	
	public static String getFileExtension(Path path)
	{
		return getFileExtension(path.toFile());
	}
	
	public static String getFileExtension(File file)
	{
		return getFileExtension(file.getName());
	}
	
	public static File setupWrite(File file)
	{
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
	
	public static boolean writeFile(File file, String contents)
	{
		try(PrintStream out = new PrintStream(setupWrite(file)))
		{
			out.print(contents);
			out.flush();
			return true;
		}
		catch(IOException e)
		{
			Log.error("Failed to write to file", e);
			return false;
		}
	}
	
	public static boolean downloadFile(URL url, File outFile)
	{
		try(InputStream in = url.openStream();
				FileOutputStream out = new FileOutputStream(outFile);)
		{
			ReadableByteChannel inChan = Channels.newChannel(in);
			FileChannel outChan = out.getChannel();
			outChan.transferFrom(inChan, 0, Long.MAX_VALUE);
			return true;
		}
		catch(IOException e)
		{
			Log.error("Failed to download file", e);
			return false;
		}
	}
	
	// Conversions
	
	public static double bytesToKilobytes(long bytes)
	{
		return bytes / 1024.0;
	}
	
	public static double bytesToMegabytes(long bytes)
	{
		return bytesToKilobytes(bytes) / 1024;
	}
	
	public static long kilobytesToBytes(double kilobytes)
	{
		return (long)(kilobytes * 1024);
	}
	
	public static long megabytesToBytes(double megabytes)
	{
		return kilobytesToBytes(megabytes * 1024);
	}
	
	// Other
	
	public static void openWebsite(String url)
	{
		if(Desktop.isDesktopSupported())
		{
			try
			{
				Desktop.getDesktop().browse(new URL(url).toURI());
			}
			catch(Exception e)
			{
				Log.error("Failed to open website: "+url, e);
			}
		}
	}
}

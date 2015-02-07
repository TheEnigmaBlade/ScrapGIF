package net.enigmablade.gif.util;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import com.alee.log.*;

public class IOUtil
{
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
		if(!Files.isWritable(file.toPath()))
		{
			Log.error("File isn't writable: "+file.getPath());
			return false;
		}
		
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
	
	public static double bytesToKilobytes(long bytes)
	{
		return bytes / 1024.0;
	}
	
	public static double bytesToMegabytes(long bytes)
	{
		return bytesToKilobytes(bytes) / 1024;
	}
}

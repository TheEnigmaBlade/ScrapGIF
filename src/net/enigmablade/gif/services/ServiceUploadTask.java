package net.enigmablade.gif.services;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.swing.*;
import com.alee.log.*;
import net.enigmablade.gif.util.*;

public abstract class ServiceUploadTask extends SwingWorker<String, Integer>
{
	protected File file;
	private long fileSize;	// In KB
	
	protected ServiceError error = ServiceError.NONE;
	
	public ServiceUploadTask(File file)
	{
		this.file = file;
		try
		{
			fileSize = Files.size(file.toPath());
			Log.info("Image size: %d bytes (%.2f MB)", fileSize, IOUtil.bytesToMegabytes(fileSize));
		}
		catch(IOException e)
		{
			Log.error("Failed to get upload file size: "+file.getPath(), e);
			fileSize = -1;
		}
	}
	
	@Override
	public void done()
	{
		String result = null;
		try
		{
			result = get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			Log.error(e);
		}
		done(result);
	}
	
	@Override
	protected void process(List<Integer> chunks)
	{
		for(int i : chunks)
			progressed(i);
	}
	
	protected abstract void done(String result);
	
	protected abstract void progressed(int value);
	
	// Helpers
	
	protected long copy(InputStream in, OutputStream out, Consumer<Integer> cb) throws IOException
	{
		int lastVal = -1;
		
		byte[] buffer = new byte[4096];
		long count = 0;
		int n = 0;
		while(-1 != (n = in.read(buffer)))
		{
			out.write(buffer, 0, n);
			count += n;
			
			int val = (int)(100.0*count/fileSize);
			if(val > lastVal)
				cb.accept(lastVal = val);
		}
		return count;
	}
	
	protected static String readStream(InputStream in)
	{
		StringBuilder sb = new StringBuilder();
		try(Scanner scanner = new Scanner(in))
		{
			while(scanner.hasNext())
				sb.append(scanner.next());
		}
		return sb.toString();
	}
}

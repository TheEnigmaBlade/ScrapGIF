package net.enigmablade.gif.services;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import com.alee.log.*;

public abstract class ServiceUploadTask extends SwingWorker<String, Void>
{
	protected File file;
	
	public ServiceUploadTask(File file)
	{
		this.file = file;
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
	
	protected abstract void done(String result);
	
	//Helpers
	
	protected static int copy(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[8192];
		int count = 0;
		int n = 0;
		while(-1 != (n = in.read(buffer)))
		{
			out.write(buffer, 0, n);
			count += n;
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

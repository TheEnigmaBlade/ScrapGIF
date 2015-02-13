package net.enigmablade.gif.services;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.swing.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.*;
import org.apache.http.impl.client.*;
import com.alee.log.*;
import net.enigmablade.gif.util.*;

public abstract class ServiceUploadTask extends SwingWorker<String, Integer>
{
	protected File file;
	private long fileSize;	// In KB
	
	private Service service;
	
	protected ServiceError error = ServiceError.NONE;
	
	public ServiceUploadTask(File file, Service service)
	{
		this.file = Objects.requireNonNull(file);
		this.service = Objects.requireNonNull(service);
		
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
	protected String doInBackground()
	{
		if(service.getFormName() == null)
		{
			HttpURLConnection conn = null;
			try(InputStream in = new FileInputStream(file))
			{
				Log.debug("Connecting to server");
				service.preUpload();
				conn = createConnection(service.getUploadUrl());
				conn.setRequestProperty("User-Agent", Service.DEFAULT_USER_AGENT);
				
				if(!service.authorize(conn))
					Log.warn("Service authorization failed; don't be surprised if the upload fails");
				
				Log.debug("Uploading image");
				try(OutputStream out = conn.getOutputStream())
				{
					copy(in, out, p -> publish(p));
					out.flush();
				}
				
				Log.info("Response = "+conn.getResponseCode());
				
				// Success
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					Log.debug("OK");
					String msg = readStream(conn.getInputStream());
					return service.handleResult(conn, msg);
				}
				// Failure
				else
				{
					Log.error("FAILURE");
					String msg = readStream(conn.getErrorStream());
					error = service.handleError(conn, msg);
					return null;
				}
			}
			catch(IOException e)
			{
				Log.error("Connection error during upload", e);
				error = ServiceError.CONNECTION;
				return null;
			}
			catch(Exception e)
			{
				Log.error("Unexpected error during upload", e);
				error = ServiceError.UNEXPECTED;
				return null;
			}
			finally
			{
				try
				{
					if(conn != null)
						conn.disconnect();
				}
				catch(Exception ignore) {}
			}
		}
		else
		{
			try
			{
				service.preUpload();
				
				/* Workaround for AWS S3 certificate rejection */
				@SuppressWarnings("deprecation")
				SSLContextBuilder sslBuilder = new SSLContextBuilder();
				@SuppressWarnings("deprecation")
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBuilder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				/* --- */
				try(CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build())
				{
					HttpPost httpPost = new HttpPost(service.getUploadUrl());
					httpPost.setHeader("User-Agent", Service.DEFAULT_USER_AGENT);
					/*Map<String, String> customHeaders = service.getHeaders();
					if(customHeaders != null)
						for(String key : customHeaders.keySet())
							httpPost.setHeader(key, customHeaders.get(key));*/
					
					FileBody fileBody = new FileBody(file);
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					Map<String, String> customHeaders = service.getHeaders();
					if(customHeaders != null)
						for(String key : customHeaders.keySet())
							builder.addTextBody(key, customHeaders.get(key));
					if(service.getFormName() != null)
						builder.addPart(service.getFormName(), fileBody);
					
					HttpEntity reqEntity = builder.build();
					httpPost.setEntity(reqEntity);
					
					try(CloseableHttpResponse response = httpClient.execute(httpPost))
					{
						HttpEntity resEntity = response.getEntity();
						int status = response.getStatusLine().getStatusCode();
						
						// Success
						if(status >= 200 && status < 300)
						{
							Log.info("OK");
							//System.out.println(EntityUtils.toString(resEntity));
							String msg = readStream(resEntity.getContent());
							msg = service.postUpload(msg);
							return service.handleResult(null, msg);
						}
						// Failure
						else
						{
							Log.error("FAILURE");
							String msg = readStream(resEntity.getContent());
							error = service.handleError(null, msg);
							return null;
						}
					}
				}
			}
			catch(IOException e)
			{
				Log.error("Connection error during upload", e);
				error = ServiceError.CONNECTION;
				return null;
			}
			catch(Exception e)
			{
				Log.error("Unexpected error during upload", e);
				error = ServiceError.UNEXPECTED;
				return null;
			}
		}
	}
	
	protected HttpURLConnection createConnection(String url) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		return conn;
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
	
	public static String readStream(InputStream in)
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

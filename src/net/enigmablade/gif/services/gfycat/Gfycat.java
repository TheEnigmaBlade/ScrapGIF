package net.enigmablade.gif.services.gfycat;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import com.alee.log.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.jsonic.*;

public class Gfycat extends Service
{
	private static String IMAGE_KEY;
	
	public Gfycat()
	{
		super("gfycat", "Gfycat", "https://gifaffe.s3.amazonaws.com/", "file");
		setFileExtensions("gif", "webm", "mp4");
	}
	
	@Override
	protected void preUpload()
	{
		String things = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random r = new Random();
		StringBuilder randStr = new StringBuilder(10);
		for(int n = 0; n < 10; n++)
			randStr.append(things.charAt(r.nextInt(things.length())));
		IMAGE_KEY = randStr.toString();
	}
	
	@Override
	protected boolean authorize(HttpURLConnection connection)
	{
		return true;
	}
	
	@Override
	protected String postUpload(String msg)
	{
		try(CloseableHttpClient httpClient = HttpClients.createDefault())
		{
			HttpGet httpGet = new HttpGet("http://upload.gfycat.com/transcode/"+IMAGE_KEY);
			httpGet.setHeader("User-Agent", DEFAULT_USER_AGENT);
			
			try(CloseableHttpResponse response = httpClient.execute(httpGet))
			{
				HttpEntity resEntity = response.getEntity();
				return ServiceUploadTask.readStream(resEntity.getContent());
			}
		}
		catch(IOException e)
		{
			Log.error("Connection error during image processing", e);
			return null;
		}
		catch(Exception e)
		{
			Log.error("Unexpected error during image processing", e);
			return null;
		}
	}
	
	@Override
	protected String handleResult(HttpURLConnection conn, String response)
	{
		JsonObject root = JsonParser.parseObject(response);
		return root.getString("gfyName");
	}
	
	@Override
	protected ServiceError handleError(HttpURLConnection conn, String response)
	{
		System.out.println("Error Response:");
		System.out.println(response);
		return ServiceError.SERVER;
	}
	
	@Override
	public String createUrl(ServiceLink link)
	{
		Objects.requireNonNull(link);
		if(!getId().equals(link.getService()))
			return null;
		return "http://gfycat.com/"+link.getFile();
	}

	@Override
	protected Map<String, String> getHeaders()
	{
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("key", IMAGE_KEY);
		headers.put("acl", "private");
		headers.put("AWSAccessKeyId", "AKIAIT4VU4B7G2LQYKZQ");
		headers.put("policy", "eyAiZXhwaXJhdGlvbiI6ICIyMDIwLTEyLTAxVDEyOjAwOjAwLjAwMFoiLAogICAgICAgICAgICAiY29uZGl0aW9ucyI6IFsKICAgICAgICAgICAgeyJidWNrZXQiOiAiZ2lmYWZmZSJ9LAogICAgICAgICAgICBbInN0YXJ0cy13aXRoIiwgIiRrZXkiLCAiIl0sCiAgICAgICAgICAgIHsiYWNsIjogInByaXZhdGUifSwKCSAgICB7InN1Y2Nlc3NfYWN0aW9uX3N0YXR1cyI6ICIyMDAifSwKICAgICAgICAgICAgWyJzdGFydHMtd2l0aCIsICIkQ29udGVudC1UeXBlIiwgIiJdLAogICAgICAgICAgICBbImNvbnRlbnQtbGVuZ3RoLXJhbmdlIiwgMCwgNTI0Mjg4MDAwXQogICAgICAgICAgICBdCiAgICAgICAgICB9");
		headers.put("success_action_status", "200");
		headers.put("signature", "mk9t/U/wRN4/uU01mXfeTe2Kcoc=");
		headers.put("Content-Type", "image/gif");
		return headers;
	}
}

package net.enigmablade.gif.services.imgur;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.*;
import com.alee.log.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.jsonic.*;

public abstract class ImgurUploadTask extends ServiceUploadTask
{
	private static final String UPLOAD_URL = "https://api.imgur.com/3/image";
	
	public ImgurUploadTask(File file)
	{
		super(file);
	}
	
	@Override
	protected String doInBackground()
	{
		HttpsURLConnection conn = null;
		
		try(InputStream in = new FileInputStream(file))
		{
			conn = (HttpsURLConnection)new URL(UPLOAD_URL).openConnection();
			conn.setDoOutput(true);
			
			ImgurAuthorization.getInstance().authorizeHttpURLConnection(conn);
			
			try(OutputStream out = conn.getOutputStream())
			{
				copy(in, out, p -> publish(p));
				out.flush();
			}
			
			Log.info("Response = "+conn.getResponseCode());
			
			Log.info("Ingur client credits: "+conn.getHeaderField("X-RateLimit-ClientRemaining")+"/"+conn.getHeaderField("X-RateLimit-ClientLimit"));
			Log.info("Imgur user credits:   "+conn.getHeaderField("X-RateLimit-UserRemaining")+"/"+conn.getHeaderField("X-RateLimit-UserLimit"));
			
			//Success
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
			{
				Log.info("OK");
				String msg = readStream(conn.getInputStream());
				JsonObject root = JsonParser.parseObject(msg);
				String id = root.getObject("data").getString("id");
				//String deletehash = root.getObject("data").getString("deletehash");
				return id;
			}
			//Failure
			else
			{
				Log.error("FAILURE");
				String msg = readStream(conn.getErrorStream());
				Log.error("Response: "+msg);
				error = ServiceError.SERVER;
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
				conn.disconnect();
			}
			catch (Exception ignore) {}
		}
	}
}

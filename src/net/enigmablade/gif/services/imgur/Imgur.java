package net.enigmablade.gif.services.imgur;

import java.net.*;
import java.util.*;
import com.alee.log.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.util.*;
import net.enigmablade.jsonic.*;

public class Imgur extends Service
{
	public Imgur()
	{
		super("imgur", "Imgur", "https://api.imgur.com/3/image", null);
		setFileExtensions("gif", "gifv");
		setMaxFileSize(IOUtil.megabytesToBytes(50));
	}
	
	@Override
	protected boolean authorize(HttpURLConnection connection)
	{
		ImgurAuthorization.getInstance().authorizeHttpURLConnection(connection);
		return true;
	}
	
	@Override
	protected String handleResult(HttpURLConnection conn, String response)
	{
		checkCredits(conn);
		
		JsonObject root = JsonParser.parseObject(response);
		String link = root.getObject("data").getString("link");
		return link.substring(link.lastIndexOf('/')+1);
	}
	
	@Override
	protected ServiceError handleError(HttpURLConnection conn, String response)
	{
		checkCredits(conn);
		
		Log.error("Response: "+response);
		return ServiceError.SERVER;
	}
	
	private void checkCredits(HttpURLConnection conn)
	{
		Log.info("Client credits: "+conn.getHeaderField("X-RateLimit-ClientRemaining")+"/"+conn.getHeaderField("X-RateLimit-ClientLimit"));
		Log.info("User credits:   "+conn.getHeaderField("X-RateLimit-UserRemaining")+"/"+conn.getHeaderField("X-RateLimit-UserLimit"));
	}
	
	@Override
	public String createUrl(ServiceLink link)
	{
		Objects.requireNonNull(link);
		if(!getId().equals(link.getService()))
			return null;
		return "http://i.imgur.com/"+link.getFile();
	}

	@Override
	protected Map<String, String> getHeaders()
	{
		return null;
	}
}

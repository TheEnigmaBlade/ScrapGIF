package net.enigmablade.gif.services.pomfse;

import java.net.*;
import java.util.*;
import com.alee.log.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.util.*;
import net.enigmablade.jsonic.*;

public class PomfSe extends Service
{
	public PomfSe()
	{
		super("pomf", "Pomf.se", "https://pomf.se/upload.php", "files[]");
		setMaxFileSize(IOUtil.megabytesToBytes(50));
	}
	
	@Override
	protected void preUpload()
	{
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}
	
	@Override
	protected boolean authorize(HttpURLConnection connection)
	{
		return true;
	}
	
	@Override
	protected String handleResult(HttpURLConnection conn, String response)
	{
		JsonObject root = JsonParser.parseObject(response);
		JsonArray files = root.getArray("files");
		JsonObject file = files.getObject(0);
		return file.getString("url");
	}
	
	@Override
	protected ServiceError handleError(HttpURLConnection conn, String response)
	{
		JsonObject root = JsonParser.parseObject(response);
		Log.error("Response: "+root.getString("error"));
		return ServiceError.SERVER;
	}
	
	@Override
	public String createUrl(ServiceLink link)
	{
		Objects.requireNonNull(link);
		if(!getId().equals(link.getService()))
			return null;
		return "http://a.pomf.se/"+link.getFile();
	}

	@Override
	protected Map<String, String> getHeaders()
	{
		return null;
	}
}

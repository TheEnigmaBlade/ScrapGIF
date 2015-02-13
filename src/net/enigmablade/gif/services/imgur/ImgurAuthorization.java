package net.enigmablade.gif.services.imgur;

import java.net.*;
import java.util.*;
import net.enigmablade.gif.*;

public class ImgurAuthorization
{
	private static ImgurAuthorization INSTANCE;
	
	public static ImgurAuthorization getInstance()
	{
		if(INSTANCE == null)
			INSTANCE = new ImgurAuthorization();
		return INSTANCE;
	}
	
	@SuppressWarnings("unused")
	private String clientId, clientSecret;
	
	private ImgurAuthorization()
	{
		Properties settings = SettingsLoader.getServiceSettings("imgur");
		clientId = settings.getProperty("client_id");
		clientSecret = settings.getProperty("client_secret");
	}
	
	public boolean authorizeHttpURLConnection(HttpURLConnection conn)
	{
		conn.setRequestProperty("Authorization", "Client-ID "+clientId);
		return true;
	}
}

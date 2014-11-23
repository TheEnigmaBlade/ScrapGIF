package net.enigmablade.gif.services.imgur;

import java.io.*;
import com.alee.log.*;

public class ImgurTest
{
	public static void main(String[] args)
	{
		initLog();
		
		ImgurUploadTask uploader = new ImgurUploadTask(new File("C:\\Users\\EnigmaBlade\\Pictures\\Anime\\Kemonomimi\\vNOoACI.jpg")) {
			@Override
			protected void done(String result)
			{
				System.out.println("http://i.imgur.com/"+result);
			}
		};
		uploader.execute();
	}
	
	private static void initLog()
	{
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		
		Log.initialize();
	}
}

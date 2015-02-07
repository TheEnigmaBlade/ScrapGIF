package tests.img.webm;

import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.img.webm.*;

public class WebMTest
{
	public static void main(String[] args)
	{
		initLog();
		
		String path = "C:\\Users\\EnigmaBlade\\Desktop\\gay_bulge.webm";
		WebMLoader loader = new WebMLoader(path);
		ImageFrame[] frames = loader.readFull();
		System.out.println("Num read frames: "+frames.length);
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

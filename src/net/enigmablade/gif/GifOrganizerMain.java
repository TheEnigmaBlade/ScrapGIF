package net.enigmablade.gif;

import com.alee.laf.*;
import com.alee.log.*;
import net.enigmablade.gif.ui.*;

public class GifOrganizerMain
{
	public static void main(String[] args)
	{
		boolean console = false;
		for(String arg : args)
			switch(arg)
			{
				case "--console": console = true;
			}
		
		initLog(console);
		Log.info("Starting application");
		
		initLAF();
		
		GifOrganizer controller = initController();
		start(controller, controller.getView());
		
		Log.info("Done!");
		Log.info("----------");
	}
	
	private static void initLog(boolean console)
	{
		System.setProperty("org.slf4j.simpleLogger.logFile", console ? "System.out" : "lastrun.log");
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		
		Log.initialize();
	}
	
	private static void initLAF()
	{
		Log.info("Initializing look and feel...");
		
		//Properties config = SettingsLoader.getConfig("config");
		//if(Boolean.parseBoolean(config.getProperty("dark_mode", "false")))
		//	StyleManager.setDefaultSkin(DarkSkin.class);
		if(!WebLookAndFeel.install())
		{
			Log.error("Failed to install look and feel");
			System.exit(0);
		}
		
		WebLookAndFeel.setDecorateFrames(true);
		WebLookAndFeel.setDecorateDialogs(true);
	}
	
	private static GifOrganizer initController()
	{
		return new GifOrganizer();
	}
	
	private static void start(GifOrganizer controller, GifOrganizerUI view)
	{
		Log.info("Opening app...");
		
		view.setVisible(true);
		controller.start();
	}
}

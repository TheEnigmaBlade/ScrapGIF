package net.enigmablade.gif;

import java.util.*;
import com.alee.laf.*;
import com.alee.log.*;
import com.alee.managers.style.*;
import net.enigmablade.gif.ui.*;

public class GifOrganizerMain
{
	public static void main(String[] args)
	{
		initLog();
		Log.info("Starting application");
		
		initLAF();
		
		GifOrganizer controller = initController();
		start(controller, controller.getView());
		
		Log.info("Done!");
		Log.info("----------");
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
	
	private static void initLAF()
	{
		Log.info("Initializing look and feel...");
		
		Properties config = SettingsLoader.getProperties("config");
		if(Boolean.parseBoolean(config.getProperty("dark_mode", "false")))
			StyleManager.setDefaultSkin(DarkSkin.class);
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

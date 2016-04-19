package net.enigmablade.gif;

import com.alee.laf.*;
import com.alee.log.*;
import com.alee.managers.language.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.ui.lang.*;

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
		
		Config config = SettingsLoader.getConfig("config");
		initLAF(!config.useNativeFrame());
		
		GifOrganizer controller = new GifOrganizer(config);
		start(controller, controller.getView());
		
		Log.info("Done!");
		Log.info("----------");
	}
	
	private static void initLog(boolean console)
	{
		Log.initialize();
		
		System.setProperty("org.slf4j.simpleLogger.logFile", console ? "System.out" : "lastrun.log");
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", console ? "debug" : "info");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
		System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
	}
	
	private static void initLAF(boolean decorateFrame)
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
		
		if(decorateFrame)
		{
			WebLookAndFeel.setDecorateFrames(true);
			WebLookAndFeel.setDecorateDialogs(true);
		}
		
		LanguageManager.setSupportedLanguages(LanguageManager.ENGLISH);
		LanguageManager.initialize();
		//LanguageManager.registerLanguageUpdater(new LangAbstractButtonLU());
		//LanguageManager.registerLanguageUpdater(new LangWebFrameLU());
		LanguageManager.registerLanguageUpdater(new WebSearchFieldLU());
		LanguageManager.addDictionary(ResourceLoader.getResource("languages.xml"));
	}
	
	private static void start(GifOrganizer controller, GifOrganizerUI view)
	{
		Log.info("Opening app...");
		
		view.setVisible(true);
		controller.start();
	}
}

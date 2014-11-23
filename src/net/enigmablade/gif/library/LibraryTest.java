package net.enigmablade.gif.library;

import java.util.*;
import com.alee.log.*;

public class LibraryTest
{
	public static void main(String[] args)
	{
		initLog();
		
		Log.info("Creating new library");
		Library library = new Library("Test", "C:\\Users\\EnigmaBlade\\Desktop\\GIFs\\Anime\\Hyouka");
		Log.info("\tID: "+library.getId());
		Log.info("\tName: "+library.getName());
		Log.info("\tPath: "+library.getPath());
		
		Log.info("Saving new library");
		LibraryManager.saveLibrary(library);
		
		LibraryManager manager = LibraryManager.getInstance();
		List<Library> libraries = manager.getLibraries();
		Log.info("Num libraries: "+libraries.size());
		for(Library lib : libraries)
			Log.info("\t"+lib.getName());
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

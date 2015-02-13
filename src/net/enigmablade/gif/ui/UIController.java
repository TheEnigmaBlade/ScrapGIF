package net.enigmablade.gif.ui;

import java.io.*;
import java.net.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;

public interface UIController
{
	// Main UI
	
	public void setLibrary(Library library);
	
	public void animateImage(ImageData item);
	
	public void uploadImage(ImageData image);
	
	public void openFileSystem(ImageData image);
	
	// Tagging
	
	public void tagImage(ImageData data);
	
	public void starImage(ImageData data);
	
	// Searching
	
	public void setSearchFavorites(String currentQuery, boolean onlyFavs);
	
	public void addToSearchQuery(String newQuery, boolean onEnd, boolean onlyFavs);
	
	public void removedFromSearchQuery(String newQuery, boolean onEnd, boolean onlyFavs);
	
	// Library
	
	public void createLibrary(File dir, String name);
	
	public void createLibraryFrom(File dir, String name);
	
	public void importLibrary(File dir);
	
	public boolean isValidLibrary(File dir);
	
	public void manageLibraries();
	
	// Images
	
	public void addLocalImage();
	
	public void addWebImage();
	
	public void addImageFolder();
	
	// Settings
	
	public void setLanguage(String language);
	
	// Other
	
	public void close();
	
	public void addFilesFromDrag(List<File> files);
	
	public void addUrlFromDrag(URL url);
	
	// Information
	
	public long getImageLastModified(ImageData image);
	
	public long getImageSize(ImageData image);
}

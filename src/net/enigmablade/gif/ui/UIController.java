package net.enigmablade.gif.ui;

import java.io.*;
import java.net.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.ui.components.item.*;

public interface UIController
{
	// Main UI
	
	public void setLibrary(Library library);
	
	public void loadThumbnail(ImageData image);
	
	public void animateImage(ImageData item);
	
	public void imageHidden(ImageData image);
	
	public void uploadImage(ImageData image);
	
	public void removeImage(ImageData image);
	
	public void openFileSystem(ImageData image);
	
	// Tagging
	
	public void tagImage(ImageData data);
	
	public void starImage(ImageData data);
	
	// Searching
	
	public void setSearchFavorites(boolean enable);
	
	public void setSearchUntagged(boolean enable);
	
	public void addToSearchQuery(String newQuery, boolean onEnd);
	
	public void removedFromSearchQuery(String newQuery, boolean onEnd);
	
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
	
	public void setImageSize(ItemSize size);
	
	public void setCheckNewImages(boolean check);
	
	public void setExtensionService(String extension, String service);
	
	public void setUseNativeFrame(boolean use);
	
	public void setLanguage(String language);
	
	// Other
	
	public void close();
	
	public void addFilesFromDrag(List<File> files, boolean copy);
	
	public void addUrlFromDrag(URL url);
	
	// Information
	
	public long getImageLastModified(ImageData image);
	
	public long getImageSize(ImageData image);
}

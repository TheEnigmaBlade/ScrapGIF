package net.enigmablade.gif.ui;

import java.io.*;
import java.net.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;

public interface UIController
{
	//Main UI
	
	public void setLibrary(Library library);
	
	public void animateImage(ImageData item);
	
	public void uploadImage(ImageData image);
	
	//Tagging
	
	public void tagImage(ImageData data);
	
	//Searching
	
	public void addToSearchQuery(String newQuery, boolean onEnd);
	
	public void removedFromSearchQuery(String newQuery, boolean onEnd);
	
	//Menu bar
	
	public void createLibrary(File dir, String name);

	//Other
	
	public void close();

	public void addFilesFromDrag(List<File> files);

	public void addUrlFromDrag(URL url);
}

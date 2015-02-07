package net.enigmablade.gif.services.imgur;

import java.io.*;
import java.util.function.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.util.*;

public class Imgur extends ServiceManager
{
	@Override
	public void upload(File file, ImageData image, TriConsumer<ServiceError, ImageData, ServiceLink> doneCallback, Consumer<Integer> progressCallback)
	{
		new ImgurUploadTask(file) {
			@Override
			protected void done(String result)
			{
				ServiceLink link = result != null ? link = new ServiceLink("imgur", result) : null;
				doneCallback.accept(error, image, link);
			}
			
			@Override
			protected void progressed(int val)
			{
				System.out.println("Progress: "+val);
				progressCallback.accept(val);
			}
		}.execute();
	}
	
	@Override
	public String createUrl(String id)
	{
		return "http://i.imgur.com/"+id+".gifv";
	}
}

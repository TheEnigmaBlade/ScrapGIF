package net.enigmablade.gif.services.imgur;

import java.io.*;
import java.util.function.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.services.*;

public class Imgur extends ServiceManager
{
	@Override
	public void upload(File file, ImageData image, BiConsumer<ImageData, ServiceLink> callback)
	{
		new ImgurUploadTask(file) {
			@Override
			protected void done(String result)
			{
				//Success
				if(result != null)
				{
					ServiceLink link = new ServiceLink("imgur", result);
					callback.accept(image, link);
				}
				//Error
				else
				{
					callback.accept(image, null);
				}
			}
		}.execute();
	}
	
	@Override
	public String createUrl(String id)
	{
		return "http://i.imgur.com/"+id+".gifv";
	}
}

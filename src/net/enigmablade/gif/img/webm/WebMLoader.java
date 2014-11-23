package net.enigmablade.gif.img.webm;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;

public class WebMLoader extends ImageLoader
{
	public WebMLoader(String path)
	{
		super(path);
	}

	@Override
	protected ImageFrame[] read(String path, boolean onlyFirstFrame)
	{
		Log.info("Reading WebM: "+path);
		
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("webp");
		if(!readers.hasNext())
		{
			Log.warn("No supported WebM decoders");
			return new ImageFrame[0];
		}
		
		Log.info("Supported WebM decoders:");
		ImageReader reader = readers.next();
		Log.info(reader.getClass().toString());
		readers.forEachRemaining(r -> Log.info(r.getClass().toString()));
		
		ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);
		try(ImageInputStream in = new FileImageInputStream(new File(path)))
		{
			reader.setInput(in);
			
			IIOMetadata metadata = reader.getStreamMetadata();
			
			BufferedImage image;
			try
			{
				image = reader.read(0);
			}
			catch(IndexOutOfBoundsException e)
			{
				Log.error("End of image", e);
			}
			
		}
		catch(FileNotFoundException e)
		{
			Log.error("File does not exist", e);
			return null;
		}
		catch(IOException e)
		{
			Log.error("Failed to load WebM", e);
			return null;
		}
		
		return frames.toArray(new ImageFrame[frames.size()]);
	}
}

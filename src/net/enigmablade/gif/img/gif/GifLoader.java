package net.enigmablade.gif.img.gif;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import org.w3c.dom.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;

public class GifLoader extends ImageLoader
{
	public GifLoader()
	{
		super("GIF", "gif");
	}
	
	@Override
	protected ImageFrame[] loadImage(File file, boolean onlyFirstFrame)
	{
		Log.debug("Reading GIF: "+file.getPath());
		ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);
		
		ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
		try(InputStream stream = new FileInputStream(file))
		{
			reader.setInput(ImageIO.createImageInputStream(stream));
			
			int lastx = 0;
			int lasty = 0;
			
			int width = -1;
			int height = -1;
			
			IIOMetadata metadata = reader.getStreamMetadata();
			
			Color backgroundColor = null;
			
			if(metadata != null)
			{
				IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
				
				NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
				NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");
				
				if(globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0)
				{
					IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);
					
					if(screenDescriptor != null)
					{
						width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
						height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
					}
				}
				
				if(globalColorTable != null && globalColorTable.getLength() > 0)
				{
					IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);
					
					if(colorTable != null)
					{
						String bgIndex = colorTable.getAttribute("backgroundColorIndex");
						
						IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
						while(colorEntry != null)
						{
							if(colorEntry.getAttribute("index").equals(bgIndex))
							{
								int red = Integer.parseInt(colorEntry.getAttribute("red"));
								int green = Integer.parseInt(colorEntry.getAttribute("green"));
								int blue = Integer.parseInt(colorEntry.getAttribute("blue"));
								
								backgroundColor = new Color(red, green, blue);
								break;
							}
							
							colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
						}
					}
				}
			}
			
			BufferedImage master = null;
			boolean hasBackround = false;
			
			for(int frameIndex = 0; !onlyFirstFrame || frameIndex == 0; frameIndex++)
			{
				BufferedImage image;
				try
				{
					image = reader.read(frameIndex);
				}
				catch(IndexOutOfBoundsException io)
				{
					break;
				}
				
				if(width == -1 || height == -1)
				{
					width = image.getWidth();
					height = image.getHeight();
				}
				
				IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
				IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
				NodeList children = root.getChildNodes();
				
				int delay = Integer.valueOf(gce.getAttribute("delayTime"))*10;
				if(delay <= 10)
					delay = 100;
				
				String disposal = gce.getAttribute("disposalMethod");
				
				if(master == null)
				{
					master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					hasBackround = image.getWidth() == width && image.getHeight() == height;
					
					Graphics2D g2 = master.createGraphics();
					g2.setColor(backgroundColor);
					g2.fillRect(0, 0, master.getWidth(), master.getHeight());
					g2.drawImage(image, 0, 0, null);
					g2.dispose();
				}
				else
				{
					int x = 0;
					int y = 0;
					
					for(int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++)
					{
						Node nodeItem = children.item(nodeIndex);
						
						if(nodeItem.getNodeName().equals("ImageDescriptor"))
						{
							NamedNodeMap map = nodeItem.getAttributes();
							x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
							y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
						}
					}
					
					if(disposal.equals("restoreToPrevious"))
					{
						BufferedImage from = null;
						for(int i = frameIndex - 1; i >= 0; i--)
						{
							if(!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0)
							{
								from = frames.get(i).getImage();
								break;
							}
						}
						
						ColorModel model = from.getColorModel();
						boolean alpha = from.isAlphaPremultiplied();
						WritableRaster raster = from.copyData(null);
						
						master.flush();
						master = new BufferedImage(model, raster, alpha, null);
					}
					else if(disposal.equals("restoreToBackgroundColor") && backgroundColor != null)
					{
						if(!hasBackround || frameIndex > 1)
						{
							Graphics2D g2 = master.createGraphics();
							g2.fillRect(lastx, lasty, frames.get(frameIndex - 1).getWidth(), frames.get(frameIndex - 1).getHeight());
							g2.dispose();
						}
					}
					
					Graphics2D g2 = master.createGraphics();
					g2.drawImage(image, x, y, null);
					g2.dispose();
					
					lastx = x;
					lasty = y;
				}
				
				ColorModel model = master.getColorModel();
				boolean alpha = master.isAlphaPremultiplied();
				WritableRaster raster = master.copyData(null);
				BufferedImage copy = new BufferedImage(model, raster, alpha, null);
				frames.add(new ImageFrame(copy, delay, disposal));
				
				image.flush();
				master.flush();
			}
			
			reader.dispose();
		}
		catch(FileNotFoundException e)
		{
			Log.error("File does not exist", e);
			return null;
		}
		catch(IOException e)
		{
			Log.error("Failed to load GIF", e);
			return null;
		}
		catch(Exception e)
		{
			Log.error("Unknown error when loading GIF", e);
			return null;
		}
		
		return frames.toArray(new ImageFrame[frames.size()]);
	}
}

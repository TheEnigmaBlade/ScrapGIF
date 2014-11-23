package net.enigmablade.gif.img;

import java.awt.image.*;

public class ImageFrame
{
	private int delay;
	private BufferedImage image;
	private String disposal;
	private int width, height;
	
	public ImageFrame(BufferedImage image, int delay, String disposal)
	{
		this.image = image;
		this.delay = delay;
		this.disposal = disposal;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	
	public void destroy()
	{
		image.flush();
		image = null;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public void setImage(BufferedImage image)
	{
		this.image = image;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public String getDisposal()
	{
		return disposal;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}
package net.enigmablade.gif.ui;

import java.util.concurrent.atomic.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.ui.components.item.*;

public class Animator implements Runnable
{
	private ItemImage panel;
	private ImageFrame[] frames;
	
	private AtomicBoolean running = new AtomicBoolean(true);
	private AtomicInteger frame = new AtomicInteger(0);
	
	public Animator(ItemImage panel, ImageFrame[] frames)
	{
		this.panel = panel;
		this.frames = frames;
	}
	
	@Override
	public void run()
	{
		while(running.get())
		{
			panel.repaint();
			try
			{
				Thread.sleep(frames[frame.get()].getDelay());
			}
			catch(InterruptedException e){}
			
			if(frame.incrementAndGet() >= frames.length)
				frame.set(0);
		}
	}
	
	public boolean isAnimating()
	{
		return running.get();
	}
	
	public void stopAnimation()
	{
		running.set(false);
		frame.set(0);
	}
	
	public ImageFrame getFrame()
	{
		return frames[frame.get()];
	}
}

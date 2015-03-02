package net.enigmablade.gif.ui.components.web;

import java.awt.event.*;
import javax.swing.*;
import com.alee.laf.button.*;

public class CustomWebButton extends WebButton
{
	public CustomWebButton(String text)
	{
		super(text);
	}
	
	public CustomWebButton(ImageIcon icon)
	{
		super(icon);
	}
	
	public CustomWebButton(String text, ImageIcon icon)
	{
		super(text, icon);
	}
	
	public void clearActionListeners()
	{
		for(ActionListener l : getActionListeners())
			removeActionListener(l);
	}
}

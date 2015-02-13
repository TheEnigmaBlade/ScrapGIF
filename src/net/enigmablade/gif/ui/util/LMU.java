package net.enigmablade.gif.ui.util;

import java.awt.*;
import javax.swing.*;
import com.alee.managers.language.*;

public class LMU
{
	public static void registerContainer(Container c, String key)
	{
		StringBuilder s = new StringBuilder(key);
		Container p = c.getParent();
		if(p != null)
		{
			String pKey = LanguageManager.getLanguageContainerKey(c);
			if(pKey != null)
				s.insert(0, pKey+".");
		}
		LanguageManager.registerLanguageContainer(c, s.toString());
	}
	
	public static void registerContainer(JMenu c, String key)
	{
		StringBuilder s = new StringBuilder(key);
		Container p = c.getParent();
		if(p != null)
		{
			String pKey = LanguageManager.getLanguageContainerKey(p);
			if(pKey != null)
				s.insert(0, pKey+".");
		}
		LanguageManager.registerLanguageContainer(c.getPopupMenu(), s.toString());
	}
}

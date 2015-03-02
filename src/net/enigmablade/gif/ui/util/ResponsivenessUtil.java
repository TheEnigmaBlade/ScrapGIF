package net.enigmablade.gif.ui.util;

import javax.swing.*;

public class ResponsivenessUtil
{
	public static void delayAction(int delay, final Runnable action)
	{
		Timer timer = new Timer(0, (evt) -> action.run());
		timer.setInitialDelay(delay);
		timer.setRepeats(false);
		timer.start();
		
	}
}

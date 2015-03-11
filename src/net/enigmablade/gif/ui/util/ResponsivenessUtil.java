package net.enigmablade.gif.ui.util;

import javax.swing.*;

public class ResponsivenessUtil
{
	public static void delayAction(int delay, final Runnable action)
	{
		Timer timer = new Timer(delay, (evt) -> action.run());
		timer.setRepeats(false);
		timer.start();
	}
}

package net.enigmablade.gif.ui.renderers;

import java.awt.*;
import javax.swing.*;
import com.alee.laf.list.*;

@SuppressWarnings("rawtypes")
public class CustomWebListCellRenderer extends WebListCellRenderer
{
	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
	{
		WebListElement e = (WebListElement)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		e.setPreferredHeight(24);
		return e;
	}
}

package net.enigmablade.gif.ui.lang;

import java.awt.*;

public interface FontLU
{
	default Font setFamily(Font font, String family)
	{
		return new Font(family, font.getStyle(), font.getSize());
	}
}

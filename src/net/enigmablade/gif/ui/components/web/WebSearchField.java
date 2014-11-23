package net.enigmablade.gif.ui.components.web;

import com.alee.extended.image.*;
import com.alee.laf.text.*;
import net.enigmablade.gif.*;

public class WebSearchField extends WebTextField
{
	public WebSearchField()
	{
		setTrailingComponent(new WebImage(ResourceLoader.loadIcon("magnifier")));
		setInputPrompt("Search...");
		setMargin(0, 0, 0, 2);
	}
}

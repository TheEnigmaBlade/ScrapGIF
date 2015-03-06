package net.enigmablade.gif.ui.components.web;

import java.awt.*;
import com.alee.extended.image.*;
import com.alee.laf.button.*;
import com.alee.laf.text.*;
import net.enigmablade.gif.*;

public class WebSearchField extends WebTextField
{
	public WebSearchField()
	{
		setInputPrompt("Search...");
		setMargin(0, 2, 0, 0);
		WebImage img = new WebImage(ResourceLoader.loadIcon("magnifier"));
		img.setMargin(0, 0, 0, 2);
		setLeadingComponent(img);
		
		WebButton clearButton = new WebButton("\u00D7");
		clearButton.setFontSizeAndStyle(14, Font.BOLD);
		clearButton.setRound(WebTextFieldStyle.round);
		clearButton.setDrawShade(false);
		clearButton.setDrawSides(false, true, false, false);
		clearButton.setFocusable(false);
		clearButton.setShadeWidth(0);
		clearButton.setMoveIconOnPress(false);
		clearButton.setRolloverDecoratedOnly(true);
		clearButton.setCursor(Cursor.getDefaultCursor());
		clearButton.addActionListener(evt -> setText(""));
		setTrailingComponent(clearButton);
	}
}

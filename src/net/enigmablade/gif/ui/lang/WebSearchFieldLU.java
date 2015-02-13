package net.enigmablade.gif.ui.lang;

import java.awt.*;
import com.alee.laf.*;
import com.alee.managers.language.data.*;
import com.alee.managers.language.updaters.*;
import net.enigmablade.gif.ui.components.web.*;

public class WebSearchFieldLU extends DefaultLanguageUpdater<WebSearchField> implements FontLU
{
	@Override
	public void update(final WebSearchField c, final String key, final Value value, final Object... data)
	{
		c.setInputPrompt(getDefaultText(value, data));
		
		Font f = c.getFont();
		String family;
		switch(value.getLang())
		{
			case "jp":	family = "Meiryo UI"; break;
			default:	family = WebFonts.getSystemControlFont().getFamily(); break;
		}
		c.setFont(setFamily(f, family));
	}
}

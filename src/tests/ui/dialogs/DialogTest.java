package tests.ui.dialogs;

import java.util.*;
import com.alee.laf.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.ui.components.item.*;
import net.enigmablade.gif.ui.dialogs.*;

public class DialogTest
{
	public static void main(String[] args)
	{
		initLAF();
		ItemImage.initStaticComponents();
		
		String path = "C:\\Users\\EnigmaBlade\\Pictures\\GIFs\\Anime\\85msxn.gif";
		ImageLoader imageLoader = ImageLoader.getInstance(path);
		ImageFrame[] image = imageLoader.readFull();
		
		TagCache recentTags = new TagCache(5);
		recentTags.add("Recent tag 1");
		recentTags.add("Recent tag 2");
		recentTags.add("Recent tag 3");
		recentTags.add("Recent tag 4");
		recentTags.add("Recent tag 5");
		
		MultiTagDialog d = new MultiTagDialog();
		Set<String> tags = d.openDialog(image, recentTags);
		System.out.println(tags);
	}
	
	private static void initLAF()
	{
		Log.info("Initializing look and feel...");
		
		//StyleManager.setDefaultSkin(DarkSkin.class);
		if(!WebLookAndFeel.install())
		{
			Log.error("Failed to install look and feel");
			System.exit(0);
		}
		
		WebLookAndFeel.setDecorateFrames(true);
		WebLookAndFeel.setDecorateDialogs(true);
	}
}

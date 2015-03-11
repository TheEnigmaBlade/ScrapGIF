package net.enigmablade.gif.ui.dialogs;

import java.awt.*;
import com.alee.extended.panel.*;
import com.alee.laf.label.*;
import com.alee.laf.rootpane.*;
import net.enigmablade.gif.*;

public class AboutDialog extends WebDialog
{
	private AboutDialog()
	{
		initComponents();
		initListeners();
	}
	
	private void initComponents()
	{
		setTitle("About");
		setLanguage("giforg.dialog.about.title");
		setLanguageContainerKey("giforg.dialog.about");
		
		setCloseOnFocusLoss(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(0, 0, 400, 300);
		
		setLayout(new GridLayout(2, 1));
		
		add(new CenterPanel(new WebLabel("Version "+GifOrganizer.VERSION_STR)));
		add(new CenterPanel(new WebLabel("This space intentionally left blank.")));
	}
	
	private void initListeners()
	{
		
	}
	
	// Instancing
	
	private static AboutDialog instance;
	
	public static void open(Container parent)
	{
		if(instance == null)
			instance = new AboutDialog();
		
		instance.setLocationRelativeTo(parent);
		instance.setVisible(true);
	}
}

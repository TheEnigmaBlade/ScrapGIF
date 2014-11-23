package net.enigmablade.gif.ui.dialogs;

import java.awt.*;
import javax.swing.*;
import com.alee.laf.label.*;
import com.alee.laf.rootpane.*;
import net.enigmablade.gif.ui.components.web.*;

public class LibraryLoadDialog extends WebDialog
{
	//Instancing
	
	private static LibraryLoadDialog instance;
	
	public static void showDialog(Component parent)
	{
		if(parent != null)
		{
			if(instance == null)
				instance = new LibraryLoadDialog();
			instance.center(parent);
			instance.setVisible(true);
		}
		else if(instance != null)
		{
			instance.setVisible(false);
		}
	}
	
	//Main
	
	public LibraryLoadDialog()
	{
		setModal(true);
		setShowWindowButtons(false);
		setShowTitleComponent(false);
		
		initComponents();
		
		pack();
	}
	
	private void initComponents()
	{
		CustomWebPanel contentPane = new CustomWebPanel();
		contentPane.setPreferredSize(200, 75);
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		
		WebLabel message = new WebLabel("Loading library...");
		message.setHorizontalAlignment(JLabel.CENTER);
		message.setFontSize(16);
		contentPane.add(message, BorderLayout.CENTER);
	}
}

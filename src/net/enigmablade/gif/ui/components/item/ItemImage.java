package net.enigmablade.gif.ui.components.item;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.swing.*;
import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.log.*;
import net.enigmablade.gif.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.ui.components.web.*;

public class ItemImage extends CustomWebOverlay
{
	private ImageData data;
	
	private WebPanel imagePanel;
	private static WebPanel loadingOverlay;
	private static WebPanel menuOverlay;
	private static CustomWebButton menuUpload, menuFolder, menuTags, menuStar;
	private static Consumer<ImageData> uploadAction, folderAction, tagAction, starAction;
	
	private ImageFrame[] frames;
	private boolean menuOpen = false;
	
	private Animator animator;
	private static Deque<Animator> animators = new ConcurrentLinkedDeque<>();
	
	//Initialization
	
	public ItemImage(ImageData data)
	{
		this.data = data;
		this.frames = null;
		
		if(data != null)
			setPreferredSize(data.getWidth(), data.getHeight());
		initComponents();
		initListeners();
	}
	
	private void initComponents()
	{
		imagePanel = new WebPanel() {
			@Override
			public void paintComponent(Graphics g)
			{
				g.drawImage((animator == null ? data.getThumbnail() : frames[animator.getFrame()].getImage()), 0, 0, getWidth(), getHeight(), null);
			}
		};
		setComponent(imagePanel);
	}
	
	private void initListeners()
	{
		setRequestFocusEnabled(true);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				requestFocus();
			}
		});
	}
	
	public static void initStaticComponents()
	{
		WebLabel loader = new WebLabel(new ImageIcon("resources/loader.gif"));
		//loader.setPreferredSize(new Dimension(100, 48));
		//loader.setOpaque(false);
		//loader.setTransparency(0.8f);
		
		loadingOverlay = new CenterPanel(loader);
		//overlayPanel.setBackground(new Color(220, 220, 220, 100));
		//overlayPanel.setOpaque(true);
		loadingOverlay.setVisible(false);
		
		menuOverlay = new WebPanel() {
			@Override
			public void paintComponent(Graphics g)
			{
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		menuOverlay.setOpaque(false);
		menuOverlay.setBackground(new Color(220, 220, 220, 75));
		menuOverlay.setVisible(false);
		menuOverlay.setLayout(new TableLayout(
				new double[]{6, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 6},
				new double[]{TableLayoutConstants.FILL, 50, 6},
				6, 0));
		
		menuUpload = new CustomWebButton(ResourceLoader.loadIcon("image-export"));
		menuUpload.setDrawShade(false);
		menuUpload.setDrawFocus(false);
		menuUpload.setRolloverShine(true);
		menuOverlay.add(menuUpload, new TableLayoutConstraints(1, 1));
		menuFolder = new CustomWebButton(ResourceLoader.loadIcon("blue-folder-open"));
		menuFolder.setDrawShade(false);
		menuFolder.setDrawFocus(false);
		menuFolder.setRolloverShine(true);
		menuOverlay.add(menuFolder, new TableLayoutConstraints(2, 1));
		menuTags = new CustomWebButton(ResourceLoader.loadIcon("tag-label"));
		menuTags.setDrawShade(false);
		menuTags.setDrawFocus(false);
		menuTags.setRolloverShine(true);
		menuOverlay.add(menuTags, new TableLayoutConstraints(3, 1));
		menuStar = new CustomWebButton(ResourceLoader.loadIcon("star"));
		menuStar.setDrawShade(false);
		menuStar.setDrawFocus(false);
		menuStar.setRolloverShine(true);
		menuOverlay.add(menuStar, new TableLayoutConstraints(4, 1));
	}
	
	public static void initStaticListeners(Consumer<ImageData> uploadAction, Consumer<ImageData> folderAction, Consumer<ImageData> tagAction, Consumer<ImageData> starAction)
	{
		ItemImage.uploadAction = uploadAction;
		ItemImage.folderAction = folderAction;
		ItemImage.tagAction = tagAction;
		ItemImage.starAction = starAction;
	}
	
	//Interaction methods
	
	public void setLoading()
	{
		Log.info("Starting loading animation");
		addOverlay(loadingOverlay);
		loadingOverlay.setVisible(true);
	}
	
	public void startAnimation(ImageFrame[] frames)
	{
		this.frames = frames;
		
		//Log.info("Starting GIF animation");
		loadingOverlay.setVisible(false);
		removeOverlay(loadingOverlay);
		
		if(!menuOpen)
		{
			synchronized(animators)
			{
				animators.forEach(thread -> thread.stopAnimation());
				animators.clear();
				
				animator = new Animator(this, frames);
				animators.add(animator);
			}
			
			Thread animatorThread = new Thread(animator);
			animatorThread.setDaemon(true);
			animatorThread.start();
		}
	}
	
	public void stopAnimation()
	{
		//Log.info("Stopping GIF animation");
		loadingOverlay.setVisible(false);
		removeOverlay(loadingOverlay);
		
		if(animator != null)
			animator.stopAnimation();
		animator = null;
		frames = null;
		SwingUtilities.invokeLater(() -> repaint());
	}
	
	public static void stopAnimations()
	{
		synchronized(animators)
		{
			animators.forEach(image -> {
				image.stopAnimation();
			});
			animators.clear();
		}
	}
	
	public void openMenu()
	{
		Log.info("Opening item menu for "+data.getId());
		menuOpen = true;
		menuUpload.addActionListener(evt -> uploadAction.accept(data));
		menuFolder.addActionListener(evt -> folderAction.accept(data));
		menuTags.addActionListener(evt -> tagAction.accept(data));
		menuStar.addActionListener(evt -> starAction.accept(data));
		
		addOverlay(menuOverlay);
		menuOverlay.setVisible(true);
	}
	
	public void closeMenu()
	{
		//Log.info("Closing item menu");
		menuOpen = false;
		menuOverlay.setVisible(false);
		removeOverlay(menuOverlay);
		
		menuUpload.clearActionListeners();
		menuFolder.clearActionListeners();
		menuTags.clearActionListeners();
		menuStar.clearActionListeners();
	}
	
	public boolean isMenuOpen()
	{
		return menuOpen;
	}
	
	//Accessor methods
	
	public ImageData getData()
	{
		return data;
	}
}

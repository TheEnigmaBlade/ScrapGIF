package net.enigmablade.gif.ui.components.item;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.log.*;
import com.alee.managers.language.data.*;
import com.alee.managers.tooltip.*;
import net.enigmablade.gif.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.ui.components.web.*;
import net.enigmablade.gif.util.*;

public class ItemImage extends CustomWebOverlay
{
	private static final DateFormat dateFormat;
	private static final DecimalFormat sizeFormat;
	
	static
	{
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
		sizeFormat = (DecimalFormat)NumberFormat.getNumberInstance();
		sizeFormat.applyPattern("#0.0#");
	}
	
	private ImageData data;
	
	private WebPanel imagePanel;
	private static WebPanel loadingOverlay;
	private static WebPanel menuOverlay;
	private static WebValueLabel nameLabel, dateLabel, sizeLabel, uploadedLabel, tagsLabel;
	private static CustomWebButton menuUpload, menuFolder, menuTags, menuStar;
	private static Consumer<ImageData> uploadAction, folderAction, tagAction, starAction;
	
	private ImageFrame[] frames;
	private boolean menuOpen = false;
	
	private Animator animator;
	private static Deque<ItemImage> animating = new ConcurrentLinkedDeque<>();
	
	//Initialization
	
	public ItemImage(ImageData data)
	{
		this.data = data;
		this.frames = null;
		
		if(data != null)
			setPreferredSize(Math.max(data.getWidth(), GifConstants.MIN_IMAGE_WIDTH), data.getHeight());
		initComponents();
		initListeners();
	}
	
	private void initComponents()
	{
		imagePanel = new WebPanel() {
			@Override
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;
				g2.setColor(GifConstants.IMAGE_BACKGROUND);
				g2.fillRect(0, 0, getWidth(), getHeight());
				
				int imageX = 0, imageW = getWidth();
				if(data != null)
				{
					imageX = (getWidth() - data.getWidth())/2;
					imageW = data.getWidth();
				}
				
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, GifConstants.previewInterpolation);
				g2.drawImage((animator == null ? data.getThumbnail() : frames[animator.getFrame()].getImage()), imageX, 0, imageW, getHeight(), null);
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
		// Create buttons
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
		menuOverlay.setBackground(new Color(220, 220, 220, 150));
		menuOverlay.setVisible(false);
		menuOverlay.setLayout(new TableLayout(
				new double[]{1, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 1},
				new double[]{TableLayoutConstants.FILL, GifConstants.IMAGE_BUTTON_HEIGHT, 2},
				1, 0));
		
		// Info panel
		WebPanel infoPanel = new WebPanel();
		infoPanel.setOpaque(false);
		infoPanel.setMargin(6);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		menuOverlay.add(infoPanel, new TableLayoutConstraints(1, 0, 4, 0));
		
		int fontSize = 12;
		nameLabel = new WebValueLabel("Name: ");
		nameLabel.setFontSize(fontSize+3);
		nameLabel.setDrawShade(true);
		infoPanel.add(nameLabel);
		dateLabel = new WebValueLabel("Date: ");
		dateLabel.setFontSize(fontSize);
		dateLabel.setDrawShade(true);
		dateLabel.setMargin(2, 10, 0, 0);
		infoPanel.add(dateLabel);
		sizeLabel = new WebValueLabel("Size: ");
		sizeLabel.setFontSize(fontSize);
		sizeLabel.setDrawShade(true);
		sizeLabel.setMargin(2, 10, 0, 0);
		infoPanel.add(sizeLabel);
		uploadedLabel = new WebValueLabel("Uploaded: ");
		uploadedLabel.setFontSize(fontSize);
		uploadedLabel.setDrawShade(true);
		uploadedLabel.setMargin(2, 10, 0, 0);
		infoPanel.add(uploadedLabel);
		tagsLabel = new WebValueLabel("<html>", "</html>");
		tagsLabel.setFontSize(fontSize-1);
		tagsLabel.setForeground(new Color(20, 20, 20));
		tagsLabel.setMargin(8, 10, 0, 0);
		infoPanel.add(tagsLabel);
		
		// Buttons
		menuUpload = new CustomWebButton(GifConstants.exportIcon);
		menuUpload.setDrawShade(false);
		menuUpload.setDrawFocus(false);
		menuUpload.setRolloverShine(true);
		menuOverlay.add(menuUpload, new TableLayoutConstraints(1, 1));
		menuFolder = new CustomWebButton(GifConstants.folderClosedIcon);
		menuFolder.setRolloverIcon(GifConstants.folderOpenIcon);
		menuFolder.setDrawShade(false);
		menuFolder.setDrawFocus(false);
		menuFolder.setRolloverShine(true);
		menuOverlay.add(menuFolder, new TableLayoutConstraints(2, 1));
		menuTags = new CustomWebButton(GifConstants.tagIcon);
		menuTags.setRolloverIcon(GifConstants.tagFillIcon);
		menuTags.setDrawShade(false);
		menuTags.setDrawFocus(false);
		menuTags.setRolloverShine(true);
		menuOverlay.add(menuTags, new TableLayoutConstraints(3, 1));
		menuStar = new CustomWebButton(GifConstants.starEnabledIcon);
		menuStar.setRolloverIcon(GifConstants.starDisabledIcon);
		menuStar.setDrawShade(false);
		menuStar.setDrawFocus(false);
		menuStar.setRolloverShine(true);
		menuOverlay.add(menuStar, new TableLayoutConstraints(4, 1));
		
		TooltipManager.addTooltip(menuUpload, "Upload image", TooltipWay.up);
		TooltipManager.addTooltip(menuFolder, "Show in file system", TooltipWay.up);
		TooltipManager.addTooltip(menuTags, "Add tag", TooltipWay.up);
		TooltipManager.addTooltip(menuStar, "Toggle star", TooltipWay.up);
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
			synchronized(animating)
			{
				animating.forEach(thread -> thread.stopAnimation());
				animating.clear();
				
				animator = new Animator(this, frames);
				animating.add(this);
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
		synchronized(animating)
		{
			animating.forEach(image -> {
				image.stopAnimation();
			});
			animating.clear();
		}
	}
	
	public void openMenu()
	{
		Log.info("Opening item menu for "+data.getId());
		
		// Setup info
		nameLabel.setValue(data.getPath());
		dateLabel.setValue(data.getLastModified() < 0 ? "Unknown" : dateFormat.format(new Date(data.getLastModified())));
		sizeLabel.setValue(data.getSize() < 0 ? "Unknown" : sizeFormat.format(IOUtil.bytesToMegabytes(data.getSize()))+" MB");
		uploadedLabel.setValue(data.getLinks().size() > 0);
		tagsLabel.setValue(data.getTags().stream().collect(Collectors.joining(", ")));
		
		// Setup buttons
		boolean starred = data.isStarred();
		menuStar.setIcon(starred ? GifConstants.starEnabledIcon : GifConstants.starDisabledIcon);
		menuStar.setRolloverIcon(starred ? GifConstants.starDisabledIcon : GifConstants.starEnabledIcon);
		
		menuUpload.addActionListener(evt -> uploadAction.accept(data));
		menuFolder.addActionListener(evt -> folderAction.accept(data));
		menuTags.addActionListener(evt -> tagAction.accept(data));
		menuStar.addActionListener(evt -> starAction.accept(data));
		
		menuOpen = true;
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

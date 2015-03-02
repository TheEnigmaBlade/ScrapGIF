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
import com.alee.extended.progress.*;
import com.alee.laf.button.*;
import com.alee.laf.panel.*;
import com.alee.log.*;
import com.alee.managers.language.data.*;
import com.alee.managers.popup.*;
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
	
	private UIController controller;
	private ImageData data;
	
	private WebPanel imagePanel;
	private WebProgressOverlay loadOverlay;
	//private static WebPanel loadingOverlay;
	private static WebPanel menuOverlay;
	private static WebValueLabel nameLabel, dateLabel, sizeLabel, uploadedLabel, tagsLabel;
	private static CustomWebButton menuUpload, menuTags, menuStar, menuMore;
	private static WebButtonPopup menuMorePopup;
	private static CustomWebButton menuRemove, menuFolder;
	private static Consumer<ImageData> uploadAction, tagAction, starAction, removeAction, folderAction;
	
	private ImageFrame[] frames;
	private boolean isVisible = false;
	private boolean isSizeSet = false;
	private boolean menuOpen = false;
	
	private Animator animator;
	private static Deque<ItemImage> animating = new ConcurrentLinkedDeque<>();
	
	// Initialization
	
	public ItemImage(UIController controller, ImageData data, ItemSize size)
	{
		this.controller = controller;
		this.data = data;
		this.frames = null;
		setSize(size);
		resetSizeSet();
		
		initComponents(size);
		initListeners();
	}
	
	private void initComponents(ItemSize size)
	{
		imagePanel = new WebPanel() {
			@Override
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;
				
				// Fill background
				g2.setColor(GifConstants.IMAGE_BACKGROUND);
				g2.fillRect(0, 0, getWidth(), getHeight());
				
				// Get image positioning information (adjust if image width is below the min width)
				int imageX = 0, imageW = getWidth();
				if(data != null)
				{
					imageW = data.getWidth(size.getSize());
					imageX = (getWidth() - imageW)/2;
				}
				
				// Draw a frame from an available animation OR the thumbnail
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, GifConstants.PREVIEW_INTERPOLATION);
				
				if(animator != null && frames != null && frames[animator.getFrame()] != null)	// frames shouldn't be null when animator is non-null, but sometimes it was...
					// Animation
					g2.drawImage(frames[animator.getFrame()].getImage(), imageX, 0, imageW, getHeight(), null);
				else if(data != null)
					// Thumbnail
					g2.drawImage(data.getThumbnail(), imageX, 0, imageW, getHeight(), null);
			}
		};
		setComponent(imagePanel);
		
		loadOverlay = new WebProgressOverlay();
		loadOverlay.setConsumeEvents(false);
		loadOverlay.setProgressWidth(size.getLoadSize());
		loadOverlay.setProgressColor(imagePanel.getBackground());
		loadOverlay.setSpeed(2);
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
		//WebLabel loader = new WebLabel(GifConstants.loaderAnimation);
		//loader.setPreferredSize(new Dimension(100, 48));
		//loader.setOpaque(false);
		//loader.setTransparency(0.8f);
		
		//loadingOverlay = new CenterPanel(loader);
		//overlayPanel.setBackground(new Color(220, 220, 220, 100));
		//overlayPanel.setOpaque(true);
		//loadingOverlay.setVisible(false);
		
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
		menuTags = new CustomWebButton(GifConstants.tagIcon);
		menuTags.setRolloverIcon(GifConstants.tagFillIcon);
		menuTags.setDrawShade(false);
		menuTags.setDrawFocus(false);
		menuTags.setRolloverShine(true);
		menuOverlay.add(menuTags, new TableLayoutConstraints(2, 1));
		menuStar = new CustomWebButton(GifConstants.starEnabledIcon);
		menuStar.setRolloverIcon(GifConstants.starDisabledIcon);
		menuStar.setDrawShade(false);
		menuStar.setDrawFocus(false);
		menuStar.setRolloverShine(true);
		menuOverlay.add(menuStar, new TableLayoutConstraints(3, 1));
		menuMore = new CustomWebButton("...");//GifConstants.moreIcon);
		menuMore.setFontSize(16);
		menuMore.setBoldFont();
		menuMore.setForeground(WebButtonStyle.bottomBgColor.darker());
		menuMore.setVerticalTextPosition(SwingConstants.TOP);
		menuMore.setDrawShade(false);
		menuMore.setDrawFocus(false);
		menuMore.setRolloverShine(true);
		menuOverlay.add(menuMore, new TableLayoutConstraints(4, 1));
		
		// More buttons
		menuRemove = new CustomWebButton("Remove image", GifConstants.removeIcon);
		menuRemove.setHorizontalAlignment(SwingUtilities.LEFT);
		menuRemove.setDrawShade(false);
		menuRemove.setDrawFocus(false);
		menuRemove.setRolloverShine(true);
		menuRemove.setDrawSides(false, false, false, false);
		menuRemove.setEnabled(false);
		
		menuFolder = new CustomWebButton("Show file", GifConstants.folderClosedIcon);
		menuFolder.setRolloverIcon(GifConstants.folderOpenIcon);
		menuFolder.setHorizontalAlignment(SwingUtilities.LEFT);
		menuFolder.setDrawShade(false);
		menuFolder.setDrawFocus(false);
		menuFolder.setRolloverShine(true);
		menuFolder.setDrawSides(false, false, false, false);
		
		menuMorePopup = new WebButtonPopup(menuMore, PopupWay.leftUp);
		menuMorePopup.setContent(new GroupPanel(0, false, menuRemove, menuFolder));
		
		TooltipManager.addTooltip(menuUpload, "Upload image", TooltipWay.up);
		TooltipManager.addTooltip(menuTags, "Add tag", TooltipWay.up);
		TooltipManager.addTooltip(menuStar, "Toggle star", TooltipWay.up);
		TooltipManager.addTooltip(menuMore, "More options", TooltipWay.up);
		
		TooltipManager.addTooltip(menuRemove, "Remove image from library", TooltipWay.up);
		TooltipManager.addTooltip(menuFolder, "Show in file system", TooltipWay.up);
	}
	
	public static void initStaticListeners(Consumer<ImageData> uploadAction, Consumer<ImageData> tagAction, Consumer<ImageData> starAction, Consumer<ImageData> removeAction, Consumer<ImageData> folderAction)
	{
		ItemImage.uploadAction = uploadAction;
		ItemImage.tagAction = tagAction;
		ItemImage.starAction = starAction;
		ItemImage.removeAction = removeAction;
		ItemImage.folderAction = folderAction;
	}
	
	// Interaction methods
	
	public boolean updateVisibility()
	{
		boolean newVis = isVisibleInScroll();
		boolean visChange = isVisible != newVis;
		isVisible = newVis;
		return visChange;
	}
	
	public void setLoading()
	{
		Log.debug("Starting loading animation");
		
		loadOverlay.setShowLoad(true);
		setComponent(loadOverlay);
		loadOverlay.setComponent(imagePanel);
		revalidate();
	}
	
	public void stopLoading(boolean force)
	{
		Log.debug("Stopping loading animation");
		
		loadOverlay.setShowLoad(false);
		if(force)
			setComponent(imagePanel);
	}
	
	public void startAnimation(ImageFrame[] frames)
	{
		Log.debug("Starting GIF animation");
		
		this.frames = Objects.requireNonNull(frames);
		
		stopLoading(false);
		
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
		Log.debug("Stopping GIF animation");
		
		if(animator != null)
			animator.stopAnimation();
		animator = null;
		frames = null;
		
		if(isVisible)
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
		Log.debug("Opening item menu for "+data.getId());
		
		// Setup info
		long lastModified = controller.getImageLastModified(data);
		long size = controller.getImageSize(data);
		nameLabel.setValue(data.getPath());
		dateLabel.setValue(lastModified < 0 ? "Unknown" : dateFormat.format(new Date(lastModified)));
		sizeLabel.setValue(size < 0 ? "Unknown" : sizeFormat.format(IOUtil.bytesToMegabytes(size))+" MB");
		uploadedLabel.setValue(data.getLinks().size() > 0);
		tagsLabel.setValue(data.getTags().stream().collect(Collectors.joining(", ")));
		
		// Setup buttons
		boolean starred = data.isStarred();
		menuStar.setIcon(starred ? GifConstants.starEnabledIcon : GifConstants.starDisabledIcon);
		menuStar.setRolloverIcon(starred ? GifConstants.starDisabledIcon : GifConstants.starEnabledIcon);
		
		menuUpload.addActionListener(evt -> uploadAction.accept(data));
		menuTags.addActionListener(evt -> tagAction.accept(data));
		menuStar.addActionListener(evt -> starAction.accept(data));
		menuRemove.addActionListener(evt -> removeAction.accept(data));
		menuFolder.addActionListener(evt -> folderAction.accept(data));
		
		stopAnimations();
		stopLoading(true);
		
		menuOpen = true;
		addOverlay(menuOverlay);
		menuOverlay.setVisible(true);
	}
	
	public void closeMenu()
	{
		Log.debug("Closing item menu");
		
		menuOpen = false;
		menuOverlay.setVisible(false);
		removeOverlay(menuOverlay);
		
		menuUpload.clearActionListeners();
		menuTags.clearActionListeners();
		menuStar.clearActionListeners();
		menuFolder.clearActionListeners();
		menuRemove.clearActionListeners();
	}
	
	public boolean isMenuOpen()
	{
		return menuOpen;
	}
	
	// Accessor methods
	
	public boolean isVisibleInScroll()
	{
		if(!isVisible())
			return false;
		
		return !getVisibleRect().isEmpty();
	}
	
	public void setSize(ItemSize size)
	{
		if(!isSizeSet)
		{
			if(data != null)
				setPreferredSize(Math.min(Math.max(data.getWidth(size.getSize()), GifConstants.MIN_IMAGE_WIDTH), GifConstants.MAX_IMAGE_WIDTH), size.getSize());
			if(loadOverlay != null)
				loadOverlay.setProgressWidth(size.getLoadSize());
			isSizeSet = true;
		}
	}
	
	public void resetSizeSet()
	{
		isSizeSet = false;
	}
	
	public ImageData getData()
	{
		return data;
	}
	
	// Other
	
	@Override
	public int hashCode()
	{
		if(data == null)
			return super.hashCode();
		return data.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof ItemImage))
			return false;
		ItemImage i = (ItemImage)o;
		if(data != null)
			return data.equals(i.data);
		return super.equals(o);
	}
}

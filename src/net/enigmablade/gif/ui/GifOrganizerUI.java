package net.enigmablade.gif.ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.alee.extended.filechooser.*;
import com.alee.extended.label.*;
import com.alee.extended.layout.*;
import com.alee.extended.statusbar.*;
import com.alee.global.StyleConstants;
import com.alee.laf.*;
import com.alee.laf.checkbox.*;
import com.alee.laf.combobox.*;
import com.alee.laf.menu.*;
import com.alee.laf.optionpane.*;
import com.alee.laf.panel.*;
import com.alee.laf.progressbar.*;
import com.alee.laf.scroll.*;
import com.alee.log.*;
import com.alee.managers.notification.*;
import com.alee.utils.*;
import net.enigmablade.gif.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.ui.components.item.*;
import net.enigmablade.gif.ui.components.web.*;
import net.enigmablade.gif.ui.dialogs.*;
import net.enigmablade.gif.ui.models.*;

@SuppressWarnings("unchecked")
public class GifOrganizerUI extends CustomWebFrame
{
	private static DataFlavor urlFlavor = null;
	
	private UIController controller;
	
	// Components
	//// Top bar
	private WebComboBox libraryComboBox;
	private WebSearchField librarySearchField;
	private WebCheckBox librarySearchFavorite;
	
	//// Main area
	private WebScrollPane itemScrollPane;
	private ItemPanel itemPanel;
	
	//// Bottom bar
	private WebValueLabel imagesCount, imagesTotal;
	private WebProgressBar progressBar;
	
	//// Menu bar
	
	private WebMenuItem newLibraryMenuItem, newLibraryFolderMenuItem, manageLibrariesMenuItem, exitMenuItem;
	private WebMenuItem addImageMenuItem, addWebImageMenuItem, addFolderMenuItem, settingsMenuItem;
	private WebMenuItem aboutMenuItem, siteMenuItem, webMenuItem;
	
	// Models
	private CollectionComboBoxModel<Library> libraryComboBoxModel;
	
	// Data
	private ItemImage hoveredImage;
	
	// Initialization
	
	static
	{
		StyleConstants.fastAnimationDelay = 2;
		
		try
		{
			urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
		}
		catch(HeadlessException | ClassNotFoundException e)
		{
			Log.error("Failed to create URL data flavor", e);
		}
	}
	
	public GifOrganizerUI(UIController controller)
	{
		Log.info("Initializing UI...");
		
		this.controller = controller;
		
		initFrame();
		initComponents();
		initMenuBar();
		initModels();
		initListeners();
		initMenuBarListeners();
		initFinish();
	}
	
	private void initFrame()
	{
		Log.info("Init settings");
		setTitle("GIF Organizer");
		//setShowTitleComponent(false);
		setPreferredSize(1000, 800);
		setRound(4);
		setShadeWidth(0);
		setResizable(true);
		setShowResizeCorner(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	private void initComponents()
	{
		Log.info("Init components");
		
		WebPanel contentPane = new WebPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		
		//Top bar
		
		WebPanel topBar = new WebPanel();
		topBar.setUndecorated(true);
		topBar.setLayout(new TableLayout(
				new double[]{150, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED},
				new double[]{TableLayoutConstants.FILL},
				2, 0));
		contentPane.add(topBar, BorderLayout.NORTH);
		
		libraryComboBox = new WebComboBox();
		topBar.add(libraryComboBox, new TableLayoutConstraints(0, 0));
		
		librarySearchField = new WebSearchField();
		topBar.add(librarySearchField, new TableLayoutConstraints(1, 0));
		
		librarySearchFavorite = new WebCheckBox("Favorites only");
		librarySearchFavorite.setMargin(0, 0, 0, 3);
		topBar.add(librarySearchFavorite, new TableLayoutConstraints(2, 0));
		
		//Main area
		
		itemPanel = new ItemPanel();
		
		itemScrollPane = new WebScrollPane(itemPanel, true, true);
		itemScrollPane.setVerticalScrollBarPolicy(WebScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		itemScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		itemScrollPane.getVerticalScrollBar().setBlockIncrement(100);
		itemScrollPane.setDrawFocus(false);
		contentPane.add(itemScrollPane, BorderLayout.CENTER);
		
		//Bottom bar
		
		WebStatusBar statusBar = new WebStatusBar();
		statusBar.setUndecorated(true);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		imagesCount = new WebValueLabel("", " of");
		imagesCount.setValue(0);
		imagesCount.setMargin(0, 2, 0, 0);
		statusBar.add(imagesCount);
		
		imagesTotal = new WebValueLabel("", " images");
		imagesTotal.setValue(0);
		imagesTotal.setMargin(0, 1, 0, 0);
		statusBar.add(imagesTotal);
		
		progressBar = new WebProgressBar();
		progressBar.setEnabled(false);
		progressBar.setStringPainted(true);
		progressBar.setString("");
		progressBar.setFontSize(11);
		statusBar.add(progressBar, ToolbarLayout.END);
		
		//Static
		ItemImage.initStaticComponents();
	}
	
	private void initModels()
	{
		libraryComboBox.setModel(libraryComboBoxModel = new CollectionComboBoxModel<>());
	}
	
	private void initListeners()
	{
		Log.info("Init listeners");
		
		//Window
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt)
			{
				dispose();
				controller.close();
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent evt)
			{
				stopAnimations();
			}
		});
		
		//Top bar
		
		libraryComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt)
			{
				if(evt.getStateChange() == ItemEvent.SELECTED)
				{
					Library library = (Library)evt.getItem();
					controller.setLibrary(library);
				}
			}
		});
		
		librarySearchField.getDocument().addDocumentListener(new DocumentListener() {
			//String oldValue = "";
			
			@Override
			public void insertUpdate(DocumentEvent evt)
			{
				try
				{
					//String added = evt.getDocument().getText(evt.getOffset(), evt.getLength());
					//oldValue += added;
					
					Document d = evt.getDocument();
					String query = d.getText(0, d.getLength());
					
					System.out.println("Offset="+evt.getOffset()+", length="+d.getLength());
					controller.addToSearchQuery(query, evt.getOffset() == d.getLength()-1, librarySearchFavorite.isSelected());
				}
				catch(BadLocationException e)
				{
					Log.error("Failed to get added substring (shouldn't happen)", e);
				}
			}
			
			@Override
			public void removeUpdate(DocumentEvent evt)
			{
				try
				{
					//String newValue = d.getText(0, d.getLength());
					//String diff = oldValue.substring(newValue.length());
					//oldValue = newValue;
					
					Document d = evt.getDocument();
					String query = d.getText(0, d.getLength());
					
					System.out.println("Offset="+evt.getOffset()+", length="+d.getLength());
					controller.removedFromSearchQuery(query, evt.getOffset() == d.getLength()-1, librarySearchFavorite.isSelected());
				}
				catch(BadLocationException e)
				{
					Log.error("Failed to get added substring (shouldn't happen)", e);
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) { /* Not text input */ }
			
		});
		
		librarySearchFavorite.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt)
			{
				controller.setSearchFavorites(librarySearchField.getText(), librarySearchFavorite.isSelected());
			}
		});
		
		//Items
		
		itemPanel.setItemMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt)
			{
				if(SwingUtils.isLeftMouseButton(evt))
				{
					ItemImage item = (ItemImage)evt.getSource();
					if(!item.isMenuOpen())
					{
						item.stopAnimation();
						item.openMenu();
					}
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				ItemImage item = (ItemImage)evt.getSource();
				if(!item.isMenuOpen())
				{
					stopAnimations();
					
					controller.animateImage(item.getData());
					hoveredImage = item;
				}
			}
			
			@Override
			public void mouseExited(MouseEvent evt)
			{
				ItemImage item = (ItemImage)evt.getSource();
				if(!item.contains(evt.getPoint()))
				{
					item.stopAnimation();
					item.closeMenu();
					hoveredImage = null;
				}
			}
		});
		
		itemScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent evt)
			{
				stopAnimations();
			}
		});
		
		//Drag-and-drop
		
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent evt)
			{
				Transferable transferable = evt.getTransferable();
				if((urlFlavor != null && transferable.isDataFlavorSupported(urlFlavor)) || transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					evt.acceptDrag(evt.getDropAction());
				else
					evt.rejectDrag();
			}
			
			@Override
			public void drop(DropTargetDropEvent evt)
			{
				Log.info("File dropped");
				Transferable transferable = evt.getTransferable();
				try
				{
					if(transferable.isDataFlavorSupported(urlFlavor))
					{
						evt.acceptDrop(evt.getDropAction());
						
						Log.info("Dropped URL");
						URL droppedUrl = (URL)transferable.getTransferData(urlFlavor);
						controller.addUrlFromDrag(droppedUrl);
					}
					else if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					{
						evt.acceptDrop(evt.getDropAction());
						
						Log.info("Dropped files");
						List<File> droppedFiles = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
						controller.addFilesFromDrag(droppedFiles);
					}
					else
					{
						evt.rejectDrop();
					}
				}
				catch(IOException | UnsupportedFlavorException e)
				{
					Log.error("Failed to get drop target", e);
				}
			}
		}, true, null);
		
		//Static
		
		ItemImage.initStaticListeners(this::uploadImage, this::openImageFolder, this::tagImage, this::starImage);
	}
	
	private void initMenuBar()
	{
		WebMenuBar bar = new WebMenuBar();
		bar.setMenuBarStyle(MenuBarStyle.attached);
		bar.setUndecorated(true);
		setJMenuBar(bar);
		
		// Library menu
		
		WebMenu libraryMenu = new WebMenu("Library");
		bar.add(libraryMenu);
		
		newLibraryMenuItem = new WebMenuItem("Create new...");
		newLibraryMenuItem.setIcon(GifConstants.newAlbumIcon);
		newLibraryMenuItem.setEnabled(false);
		libraryMenu.add(newLibraryMenuItem);
		
		newLibraryFolderMenuItem = new WebMenuItem("Import folder...");
		newLibraryFolderMenuItem.setIcon(GifConstants.importAlbumIcon);
		libraryMenu.add(newLibraryFolderMenuItem);
		
		libraryMenu.addSeparator();
		
		manageLibrariesMenuItem = new WebMenuItem("Manage...");
		manageLibrariesMenuItem.setIcon(GifConstants.manageAlbumsIcon);
		manageLibrariesMenuItem.setEnabled(false);
		libraryMenu.add(manageLibrariesMenuItem);
		
		libraryMenu.addSeparator();
		
		exitMenuItem = new WebMenuItem("Quit");
		exitMenuItem.setIcon(GifConstants.exitIcon);
		libraryMenu.add(exitMenuItem);
		
		// Edit menu
		
		WebMenu editMenu = new WebMenu("Edit");
		bar.add(editMenu);
		
		addImageMenuItem = new WebMenuItem("Add image...");
		addImageMenuItem.setIcon(GifConstants.addImageIcon);
		addImageMenuItem.setEnabled(false);
		editMenu.add(addImageMenuItem);
		
		addWebImageMenuItem = new WebMenuItem("Add web image...");
		addWebImageMenuItem.setIcon(GifConstants.addWebImageIcon);
		addWebImageMenuItem.setEnabled(false);
		editMenu.add(addWebImageMenuItem);
		
		addFolderMenuItem = new WebMenuItem("Add folder...");
		addFolderMenuItem.setIcon(GifConstants.addFolderIcon);
		addFolderMenuItem.setEnabled(false);
		editMenu.add(addFolderMenuItem);
		
		editMenu.addSeparator();
		
		settingsMenuItem = new WebMenuItem("Settings...");
		settingsMenuItem.setIcon(GifConstants.settingsIcon);
		editMenu.add(settingsMenuItem);
		
		// About
		
		WebMenu aboutMenu = new WebMenu("About");
		bar.add(aboutMenu);
		
		aboutMenuItem = new WebMenuItem("About...");
		aboutMenuItem.setIcon(GifConstants.aboutIcon);
		aboutMenuItem.setEnabled(false);
		aboutMenu.add(aboutMenuItem);
		
		aboutMenu.addSeparator();
		
		siteMenuItem = new WebMenuItem("EnigmaBlade.net");
		siteMenuItem.setIcon(WebLinkLabel.LINK_ICON);
		aboutMenu.add(siteMenuItem);
		
		webMenuItem = new WebMenuItem("Web Look and Feel");
		webMenuItem.setIcon(WebLookAndFeel.getIcon(16));
		aboutMenu.add(webMenuItem);
	}
	
	private void initMenuBarListeners()
	{
		newLibraryMenuItem.addActionListener(evt -> {
			
		});
		
		newLibraryFolderMenuItem.addActionListener(evt -> {
			File dir = WebDirectoryChooser.showDialog(this, "Select library folder");
			String name = WebOptionPane.showInputDialog(this, "Name the library:");
			if(dir != null && name != null)
				controller.createLibrary(dir, name);
		});
		
		manageLibrariesMenuItem.addActionListener(evt -> {
			
		});
	}
	
	private void initFinish()
	{
		pack();
		center();
	}
	
	// UI methods
	
	public void uploadImage(ImageData data)
	{
		Log.info("Upload image triggered for "+data.getId());
		controller.uploadImage(data);
	}
	
	public void openImageFolder(ImageData data)
	{
		Log.info("Open folder triggered for "+data.getId());
		controller.openFileSystem(data);
	}
	
	public void tagImage(ImageData data)
	{
		Log.info("Tag image triggered for "+data.getId());
		controller.tagImage(data);
	}
	
	public void starImage(ImageData data)
	{
		Log.info("Star image triggered for "+data.getId());
		controller.starImage(data);
	}
	
	private void stopAnimations()
	{
		ItemImage.stopAnimations();
		
		if(hoveredImage != null)
		{
			hoveredImage.closeMenu();
			hoveredImage = null;
		}
	}
	
	// Interaction
	
	public void setLibraries(List<Library> libraries)
	{
		ItemListener[] lists = libraryComboBox.getItemListeners();
		for(ItemListener list : lists)
			libraryComboBox.removeItemListener(list);
		
		libraryComboBoxModel.addAllElements(libraries);
		libraryComboBox.setSelectedItem(null);
		
		for(ItemListener list : lists)
			libraryComboBox.addItemListener(list);
	}
	
	public void selectLibrary(Library library)
	{
		libraryComboBox.setSelectedItem(library);
	}
	
	public void setLibraryLoading(boolean loading)
	{
		LibraryLoadDialog.showDialog(loading ? this : null);
	}
	
	public void setLibrarySize(int size)
	{
		imagesTotal.setValue(size);
	}
	
	public void addImage(ImageData data)
	{
		itemPanel.addImage(data);
		updateNumImages();
		
		SwingUtilities.invokeLater(() -> {
			itemPanel.doLayout();
			itemPanel.revalidate();
			itemPanel.repaint();
			//itemScrollPane.revalidate();
			//itemScrollPane.repaint();
		});
	}
	
	public void addAllImages(List<ImageData> datas)
	{
		for(ImageData data : datas)
			itemPanel.addImage(data);
		updateNumImages();
		
		SwingUtilities.invokeLater(() -> {
			itemPanel.doLayout();
			itemPanel.revalidate();
			itemPanel.repaint();
		});
	}
	
	public void setImages(List<ImageData> datas)
	{
		itemPanel.clearImages();
		addAllImages(datas);
	}
	
	public void removeImage(ImageData data)
	{
		itemPanel.removeImage(data.getId());
		updateNumImages();
		
		SwingUtilities.invokeLater(() -> {
			itemPanel.doLayout();
			itemPanel.revalidate();
			itemPanel.repaint();
			//itemScrollPane.revalidate();
			//itemScrollPane.repaint();
		});
	}
	
	public void setImageItemLoading(String id)
	{
		itemPanel.setItemLoading(id);
	}
	
	public void setImageItemAnimating(String id, ImageFrame[] frames)
	{
		itemPanel.setItemAnimated(id, frames);
	}
	
	public void refreshImageMenu()
	{
		hoveredImage.closeMenu();
		hoveredImage.openMenu();
	}
	
	public void resetSearch()
	{
		librarySearchField.clear();
		SwingUtilities.invokeLater(() -> itemPanel.requestFocus());
	}
	
	// Information display
	
	public void startProgress()
	{
		progressBar.setEnabled(true);
		progressBar.setIndeterminate(true);
	}
	
	public void setProgress(int progress)
	{
		if(progressBar.isIndeterminate())
			progressBar.setIndeterminate(false);
		
		progressBar.setValue(progress);
	}
	
	public void endProgress()
	{
		progressBar.setEnabled(false);
		progressBar.setIndeterminate(false);
		progressBar.setString("");
	}
	
	public void startUploadProgress()
	{
		startProgress();
		progressBar.setString("Uploading");
		progressBar.setMaximum(100);
	}
	
	public void endUploadProgress()
	{
		progressBar.setIndeterminate(true);
		progressBar.setString("Waiting for image link");
	}
	
	public void startDownloadProgress()
	{
		startProgress();
		progressBar.setString("Downloading");
		progressBar.setMaximum(100);
	}
	
	// Notifications
	
	public void notifyUpload(ServiceError error)
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		if(error == ServiceError.NONE)
		{
			notice.setDisplayTime(5000);
			notice.setIcon(NotificationIcon.information);
			notice.setContent("Image successfully uploaded");
		}
		else
		{
			notice.setDisplayTime(8000);
			notice.setIcon(NotificationIcon.error);
			notice.setContent("Image upload failed");
		}
		NotificationManager.showNotification(this, notice);
	}
	
	public void notifyLinkCopy()
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.information);
		notice.setContent("Image link copied");
		NotificationManager.showNotification(this, notice);
	}
	
	public void notifyBadFile()
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.error);
		notice.setContent("Invalid files were not added");
		NotificationManager.showNotification(this, notice);
	}
	
	public void notifyMoveError()
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.error);
		notice.setContent("Could not move files to library");
		NotificationManager.showNotification(this, notice);
	}
	
	public void notifyDownloadError()
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.error);
		notice.setContent("Failed to download image");
		NotificationManager.showNotification(this, notice);
	}
	
	// Internal updates
	
	private void updateNumImages()
	{
		imagesCount.setValue(itemPanel.getNumImages());
	}
	
	// Dialog requests
	
	public String getTagInput()
	{
		//TODO add tag suggestions (based on use?)
		return WebOptionPane.showInputDialog(this, "Enter a tag:", "New tag", WebOptionPane.QUESTION_MESSAGE);
	}
	
	public Set<String> getMultiTagInput(ImageFrame[] image, TagCache recentTags)
	{
		//TODO add tag suggestions (based on use?)
		//Set<String> tags = new HashSet<>();
		//String tag;
		//while((tag = WebOptionPane.showInputDialog(this, "Enter a tag:", "New tag", WebOptionPane.QUESTION_MESSAGE)) != null)
		//	tags.add(tag);
		return MultiTagDialog.showDialog(image, recentTags);
	}
	
	// Settings
	
	@Override
	public void setSize(int width, int height)
	{
		setPreferredSize(width, height);
		pack();
		center();
	}
}

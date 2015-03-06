package net.enigmablade.gif.ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import com.alee.extended.filechooser.*;
import com.alee.extended.label.*;
import com.alee.extended.layout.*;
import com.alee.extended.statusbar.*;
import com.alee.global.StyleConstants;
import com.alee.laf.*;
import com.alee.laf.checkbox.*;
import com.alee.laf.combobox.*;
import com.alee.laf.filechooser.*;
import com.alee.laf.menu.*;
import com.alee.laf.optionpane.*;
import com.alee.laf.panel.*;
import com.alee.laf.progressbar.*;
import com.alee.laf.scroll.*;
import com.alee.log.*;
import com.alee.managers.language.*;
import com.alee.managers.notification.*;
import com.alee.utils.*;
import com.alee.utils.swing.*;
import net.enigmablade.gif.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.library.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.services.*;
import net.enigmablade.gif.ui.components.item.*;
import net.enigmablade.gif.ui.components.web.*;
import net.enigmablade.gif.ui.dialogs.*;
import net.enigmablade.gif.ui.models.*;
import net.enigmablade.gif.ui.util.*;
import net.enigmablade.gif.util.*;

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
	
	private WebMenuItem newLibraryMenuItem, newLibraryFromMenuItem, importLibraryMenuItem, exportLibrariesMenuItem, manageLibrariesMenuItem, exitMenuItem;
	private WebMenuItem addImageMenuItem, addWebImageMenuItem, addFolderMenuItem;
	private WebCheckBoxMenuItem showStarredMenuItem, showUntaggedMenuItem, checkNewImagesMenuItem, useNativeFrameMenuItem;
	private WebRadioButtonMenuItem smallSizeMenuItem, normalSizeMenuItem, largeSizeMenuItem;
	private WebMenuItem aboutMenuItem, changelogMenuItem, siteMenuItem, webMenuItem, iconsMenuItem;
	
	private List<WebRadioButtonMenuItem> languageMenuItems;
	
	// Models
	private CollectionComboBoxModel<Library> libraryComboBoxModel;
	private Set<ItemImage> visibleImages;
	
	// Listeners
	private AdjustmentListener imageScrollListener;
	private boolean imageScrollListenerEnabled = true;
	private boolean searchFavoritesLock = false;
	
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
	
	public GifOrganizerUI(UIController controller, Config config)
	{
		Log.info("Initializing UI...");
		
		this.controller = controller;
		
		initFrame();
		initComponents();
		initMenuBar();
		initModels();
		initSettings(config);
		initListeners();
		initMenuBarListeners();
		initFinish();
	}
	
	private void initFrame()
	{
		Log.info("Init settings");
		
		setLanguage("giforg.title");
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
				0, 0));
		contentPane.add(topBar, BorderLayout.NORTH);
		LMU.registerContainer(topBar, "giforg.toolbar");
		
		libraryComboBox = new WebComboBox();
		libraryComboBox.setEnabled(false);
		topBar.add(libraryComboBox, new TableLayoutConstraints(0, 0));
		
		librarySearchField = new WebSearchField();
		librarySearchField.setDrawShade(false);
		librarySearchField.setEnabled(false);
		topBar.add(librarySearchField, new TableLayoutConstraints(1, 0));
		librarySearchField.setLanguage("search");
		
		librarySearchFavorite = new WebCheckBox("Starred only");
		librarySearchFavorite.setMargin(0, 0, 0, 3);
		librarySearchFavorite.setEnabled(false);
		topBar.add(librarySearchFavorite, new TableLayoutConstraints(2, 0));
		librarySearchFavorite.setLanguage("favorites");
		
		//Main area
		
		itemPanel = new ItemPanel(controller);
		
		itemScrollPane = new WebScrollPane(itemPanel, true, true);
		itemScrollPane.setVerticalScrollBarPolicy(WebScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
		visibleImages = Collections.synchronizedSet(new HashSet<>());
	}
	
	private void initSettings(Config config)
	{
		checkNewImagesMenuItem.setSelected(config.isCheckNewImages());
		useNativeFrameMenuItem.setSelected(config.useNativeFrame());
		
		switch(config.getImageSize())
		{
			case SMALL: smallSizeMenuItem.setSelected(true);
				break;
			case NORMAL: normalSizeMenuItem.setSelected(true);
				break;
			case LARGE: largeSizeMenuItem.setSelected(true);
				break;
		}
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
				hoveredImage = null;
			}
		});
		
		//Top bar
		
		libraryComboBox.addItemListener(evt -> {
			if(isSelected(evt))
			{
				Library library = (Library)evt.getItem();
				controller.setLibrary(library);
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
					
					//System.out.println("Offset="+evt.getOffset()+", length="+d.getLength());
					controller.addToSearchQuery(query, evt.getOffset() == d.getLength()-1);
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
					
					//System.out.println("Offset="+evt.getOffset()+", length="+d.getLength());
					controller.removedFromSearchQuery(query, evt.getOffset() == d.getLength()-1);
				}
				catch(BadLocationException e)
				{
					Log.error("Failed to get added substring (shouldn't happen)", e);
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) { /* Not text input */ }
			
		});
		
		librarySearchFavorite.addItemListener(evt -> ui_setSearchFavorites(isSelected(evt)));
		
		//Items
		
		itemPanel.setItemMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt)
			{
				if(SwingUtils.isLeftMouseButton(evt) || SwingUtils.isRightMouseButton(evt))
				{
					ItemImage image = (ItemImage)evt.getSource();
					if(!image.isMenuOpen())
					{
						image.stopAnimation();
						image.openMenu();
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
					
					ResponsivenessUtil.delayAction(UIConstants.ANIMATION_DELAY, () -> {
						if(item.getMousePosition() != null)
						{
							controller.animateImage(item.getData());
							hoveredImage = item;
						}
					});
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
					if(item.equals(hoveredImage))
						hoveredImage = null;
				}
			}
		});
		
		itemScrollPane.getVerticalScrollBar().addAdjustmentListener(imageScrollListener = new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent evt)
			{
				stopAnimations();
				updateVisibleImages();
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
						controller.addFilesFromDrag(droppedFiles, evt.getDropAction() == DnDConstants.ACTION_COPY);
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
		
		ItemImage.initStaticListeners(this::ui_uploadImage, this::ui_tagImage, this::ui_starImage, this::ui_removeImage, this::ui_openImageFolder);
	}
	
	private void initMenuBar()
	{
		WebMenuBar bar = new WebMenuBar();
		bar.setMenuBarStyle(MenuBarStyle.attached);
		bar.setUndecorated(true);
		setJMenuBar(bar);
		LMU.registerContainer(bar, "giforg.menu");
		
		// Library menu
		
		WebMenu libraryMenu = new WebMenu("Library");
		bar.add(libraryMenu);
		libraryMenu.setLanguage("library.title");
		LMU.registerContainer(libraryMenu, "library");
		
		newLibraryMenuItem = new WebMenuItem("Create new...");
		newLibraryMenuItem.setIcon(UIConstants.newAlbumIcon);
		libraryMenu.add(newLibraryMenuItem);
		newLibraryMenuItem.setLanguage("new");
		
		newLibraryFromMenuItem = new WebMenuItem("Create from folder...");
		newLibraryFromMenuItem.setIcon(UIConstants.newAlbumFromIcon);
		libraryMenu.add(newLibraryFromMenuItem);
		newLibraryFromMenuItem.setLanguage("newfrom");
		
		importLibraryMenuItem = new WebMenuItem("Import...");
		importLibraryMenuItem.setIcon(UIConstants.importAlbumIcon);
		libraryMenu.add(importLibraryMenuItem);
		importLibraryMenuItem.setLanguage("import");
		
		libraryMenu.addSeparator();
		
		addImageMenuItem = new WebMenuItem("Add image...");
		addImageMenuItem.setIcon(UIConstants.addImageIcon);
		addImageMenuItem.setEnabled(false);
		libraryMenu.add(addImageMenuItem);
		addImageMenuItem.setLanguage("add");
		
		addWebImageMenuItem = new WebMenuItem("Add web image...");
		addWebImageMenuItem.setIcon(UIConstants.addWebImageIcon);
		addWebImageMenuItem.setEnabled(false);
		libraryMenu.add(addWebImageMenuItem);
		addWebImageMenuItem.setLanguage("addweb");
		
		addFolderMenuItem = new WebMenuItem("Add folder...");
		addFolderMenuItem.setIcon(UIConstants.addFolderIcon);
		addFolderMenuItem.setEnabled(false);
		libraryMenu.add(addFolderMenuItem);
		addFolderMenuItem.setLanguage("addfolder");
		
		libraryMenu.addSeparator();
		
		manageLibrariesMenuItem = new WebMenuItem("Manage...");
		manageLibrariesMenuItem.setIcon(UIConstants.manageAlbumsIcon);
		manageLibrariesMenuItem.setEnabled(false);
		libraryMenu.add(manageLibrariesMenuItem);
		manageLibrariesMenuItem.setLanguage("manage");
		
		exportLibrariesMenuItem = new WebMenuItem("Export...");
		exportLibrariesMenuItem.setIcon(UIConstants.exportAlbumIcon);
		exportLibrariesMenuItem.setEnabled(false);
		libraryMenu.add(exportLibrariesMenuItem);
		exportLibrariesMenuItem.setLanguage("export");
		
		libraryMenu.addSeparator();
		
		exitMenuItem = new WebMenuItem("Exit");
		exitMenuItem.setIcon(UIConstants.exitIcon);
		libraryMenu.add(exitMenuItem);
		exitMenuItem.setLanguage("exit");
		
		// View menu
		
		WebMenu viewMenu = new WebMenu("View");
		bar.add(viewMenu);
		viewMenu.setLanguage("view.title");
		LMU.registerContainer(viewMenu, "view");
		
		//// Image size
		WebMenu imageSizeMenu = new WebMenu("Image size");
		viewMenu.add(imageSizeMenu);
		imageSizeMenu.setLanguage("imagesize");
		LMU.registerContainer(imageSizeMenu, "imagesize");
		
		ButtonGroup sizeButtonGroup = new ButtonGroup();
		
		smallSizeMenuItem = new WebRadioButtonMenuItem("Small");
		imageSizeMenu.add(smallSizeMenuItem);
		sizeButtonGroup.add(smallSizeMenuItem);
		smallSizeMenuItem.setLanguage("small");
		
		normalSizeMenuItem = new WebRadioButtonMenuItem("Normal");
		imageSizeMenu.add(normalSizeMenuItem);
		sizeButtonGroup.add(normalSizeMenuItem);
		normalSizeMenuItem.setLanguage("normal");
		
		largeSizeMenuItem = new WebRadioButtonMenuItem("Large");
		imageSizeMenu.add(largeSizeMenuItem);
		sizeButtonGroup.add(largeSizeMenuItem);
		largeSizeMenuItem.setLanguage("large");
		
		viewMenu.addSeparator();
		
		showStarredMenuItem = new WebCheckBoxMenuItem("Show starred only");
		showStarredMenuItem.setIcon(UIConstants.noStarIcon);
		viewMenu.add(showStarredMenuItem);
		showStarredMenuItem.setLanguage("showstarred");
		
		showUntaggedMenuItem = new WebCheckBoxMenuItem("Show untagged only");
		showUntaggedMenuItem.setIcon(UIConstants.noTagIcon);
		viewMenu.add(showUntaggedMenuItem);
		showUntaggedMenuItem.setLanguage("showuntagged");
		
		// Settings menu
		
		WebMenu settingsMenu = new WebMenu("Settings");
		bar.add(settingsMenu);
		settingsMenu.setLanguage("settings.title");
		LMU.registerContainer(settingsMenu, "settings");
		
		checkNewImagesMenuItem = new WebCheckBoxMenuItem("Check for new images");
		settingsMenu.add(checkNewImagesMenuItem);
		checkNewImagesMenuItem.setLanguage("checknewimages");
		
		//// Upload services
		WebMenu uploadServiceMenu = new WebMenu("Upload services");
		uploadServiceMenu.setEnabled(false);
		settingsMenu.add(uploadServiceMenu);
		uploadServiceMenu.setLanguage("uploadservice");
		
		//TODO: fill upload services menu
		
		settingsMenu.addSeparator();
		
		useNativeFrameMenuItem = new WebCheckBoxMenuItem("Use system window (requires restart)");
		settingsMenu.add(useNativeFrameMenuItem);
		useNativeFrameMenuItem.setLanguage("nativeframe");
		
		//// Languages
		WebMenu languageMenu = new WebMenu("Language");
		settingsMenu.add(languageMenu);
		languageMenu.setLanguage("language");
		
		languageMenuItems = new ArrayList<>();
		ButtonGroup languageButtonGroup = new ButtonGroup();
		for(String lang : LanguageManager.getSupportedLanguages())
		{
			WebRadioButtonMenuItem langItem = new WebRadioButtonMenuItem(lang);
			langItem.setName(lang);
			langItem.setIcon(LanguageManager.getLanguageIcon(lang));
			if(lang.equals(LanguageManager.getLanguage()))
				langItem.setSelected(true);
			
			languageMenuItems.add(langItem);
			languageMenu.add(langItem);
			languageButtonGroup.add(langItem);
		}
		
		// About
		
		WebMenu aboutMenu = new WebMenu("About");
		bar.add(aboutMenu);
		aboutMenu.setLanguage("about.title");
		LMU.registerContainer(aboutMenu, "about");
		
		aboutMenuItem = new WebMenuItem("About...");
		aboutMenuItem.setIcon(UIConstants.aboutIcon);
		aboutMenuItem.setEnabled(false);
		aboutMenu.add(aboutMenuItem);
		aboutMenuItem.setLanguage("about");
		
		changelogMenuItem = new WebMenuItem("Changelog...");
		changelogMenuItem.setIcon(UIConstants.aboutIcon);
		changelogMenuItem.setEnabled(false);
		aboutMenu.add(changelogMenuItem);
		changelogMenuItem.setLanguage("changelog");
		
		aboutMenu.addSeparator();
		
		siteMenuItem = new WebMenuItem("EnigmaBlade.net");
		siteMenuItem.setIcon(WebLinkLabel.LINK_ICON);
		aboutMenu.add(siteMenuItem);
		
		webMenuItem = new WebMenuItem("Web Look and Feel");
		webMenuItem.setIcon(WebLookAndFeel.getIcon(16));
		aboutMenu.add(webMenuItem);
		
		iconsMenuItem = new WebMenuItem("Fugue Icons");
		iconsMenuItem.setIcon(ResourceLoader.loadIcon("fugue"));
		aboutMenu.add(iconsMenuItem);
	}
	
	private void initMenuBarListeners()
	{
		// Library menu
		
		newLibraryMenuItem.addActionListener(evt -> ui_newLibrary());
		
		newLibraryFromMenuItem.addActionListener(evt -> ui_newLibraryFrom());
		
		importLibraryMenuItem.addActionListener(evt -> ui_importLibrary());
		
		exportLibrariesMenuItem.addActionListener(evt -> ui_exportLibrary());
		
		manageLibrariesMenuItem.addActionListener(evt -> controller.manageLibraries());
		
		exitMenuItem.addActionListener(evt -> controller.close());
		
		addImageMenuItem.addActionListener(evt -> controller.addLocalImage());
		
		addWebImageMenuItem.addActionListener(evt -> controller.addWebImage());
		
		addFolderMenuItem.addActionListener(evt -> controller.addImageFolder());
		
		// View menu
		
		smallSizeMenuItem.addItemListener(evt -> ui_setImageSize(evt, ItemSize.SMALL));
		normalSizeMenuItem.addItemListener(evt -> ui_setImageSize(evt, ItemSize.NORMAL));
		largeSizeMenuItem.addItemListener(evt -> ui_setImageSize(evt, ItemSize.LARGE));
		
		showStarredMenuItem.addItemListener(evt -> ui_setSearchFavorites(isSelected(evt)));
		
		showUntaggedMenuItem.addItemListener(evt -> ui_setSearchUntagged(isSelected(evt)));
		
		// Settings menu
		
		checkNewImagesMenuItem.addItemListener(evt -> controller.setCheckNewImages(isSelected(evt)));
		
		useNativeFrameMenuItem.addItemListener(evt -> controller.setUseNativeFrame(isSelected(evt)));
		
		for(WebRadioButtonMenuItem item : languageMenuItems)
		{
			item.addItemListener((evt) -> {
				if(isSelected(evt))
					controller.setLanguage(item.getName());
			});
		}
		
		// About menu
		
		aboutMenuItem.addActionListener(evt -> ui_about());
		
		changelogMenuItem.addActionListener(evt -> ui_changelog());
		
		siteMenuItem.addActionListener(evt -> ui_website());
		
		webMenuItem.addActionListener(evt -> ui_lafWebsite());
		
		iconsMenuItem.addActionListener(evt -> ui_iconsWebsite());
	}
	
	private void initFinish()
	{
		pack();
		center();
	}
	
	// UI action methods
	
	private void ui_uploadImage(ImageData data)
	{
		Log.info("Upload image triggered for "+data.getId());
		controller.uploadImage(data);
	}
	
	private void ui_tagImage(ImageData data)
	{
		Log.info("Tag image triggered for "+data.getId());
		controller.tagImage(data);
	}
	
	private void ui_starImage(ImageData data)
	{
		Log.info("Star image triggered for "+data.getId());
		controller.starImage(data);
	}
	
	private void ui_openImageFolder(ImageData data)
	{
		Log.info("Open folder triggered for "+data.getId());
		controller.openFileSystem(data);
	}
	
	private void ui_removeImage(ImageData data)
	{
		Log.info("Remove image triggered for "+data.getId());
		controller.removeImage(data);
	}
	
	//// Menu bar action methods
	
	private void ui_newLibrary()
	{
		Log.debug("Creating new library");
		
		File dir = WebDirectoryChooser.showDialog(this, "Select parent folder");
		if(dir == null)
		{
			Log.debug("No library parent folder selected");
			return;
		}
		
		String name = getLibraryName();
		if(name == null)
		{
			Log.debug("No library name given");
			return;
		}
		
		controller.createLibrary(dir, name);
	}
	
	private void ui_newLibraryFrom()
	{
		Log.debug("Creating new library from");
		
		File dir = WebDirectoryChooser.showDialog(this, "Select library folder");
		if(dir == null)
		{
			Log.debug("No library folder selected");
			return;
		}
		
		String name = getLibraryName();
		if(name == null)
		{
			Log.debug("No library name given");
			return;
		}
		
		controller.createLibraryFrom(dir, name);
	}
	
	private String getLibraryName()
	{
		String name = null;
		do
		{
			name = WebOptionPane.showInputDialog(this, "Name the library:");
			
		} while(name != null && !LibraryManager.isValidLibraryName(name));
		
		return name;
	}
	
	private void ui_importLibrary()
	{
		Log.debug("Importing library");
		
		//TODO add valid library feedback or custom file filter or custom iconifier (is that a thing?)
		File dir;
		do
		{
			dir = WebDirectoryChooser.showDialog(this, "Select parent folder");
			if(dir == null)
				return;
			if(!controller.isValidLibrary(dir))
				Log.warn("Selected library is not valid ("+dir.getAbsolutePath()+")");
		} while(!controller.isValidLibrary(dir));
		
		controller.importLibrary(dir);
	}
	
	private void ui_exportLibrary()
	{
		//TODO
	}
	
	private void ui_about()
	{
		//TODO open about dialog
	}
	
	private void ui_changelog()
	{
		IOUtil.openWebsite("https://github.com/TheEnigmaBlade/ScrapGIF/blob/master/CHANGES.md");
	}
	
	private void ui_website()
	{
		IOUtil.openWebsite("http://enigmablade.net/");
	}
	
	private void ui_lafWebsite()
	{
		IOUtil.openWebsite("http://weblookandfeel.com/");
	}
	
	private void ui_iconsWebsite()
	{
		IOUtil.openWebsite("http://p.yusukekamiyamane.com/");
	}
	
	private void ui_setImageSize(ItemEvent evt, ItemSize size)
	{
		if(isSelected(evt))
			controller.setImageSize(size);
	}
	
	private void ui_setSearchFavorites(boolean selected)
	{
		if(!searchFavoritesLock)
		{
			searchFavoritesLock = true;
			librarySearchFavorite.setSelected(selected);
			showStarredMenuItem.setSelected(selected);
			searchFavoritesLock = false;
			
			controller.setSearchFavorites(selected);
		}
	}
	
	private void ui_setSearchUntagged(boolean selected)
	{
		controller.setSearchUntagged(selected);
	}
	
	// Receive from controller
	
	public void setLibraries(List<Library> libraries)
	{
		// Disable listeners
		ItemListener[] lists = libraryComboBox.getItemListeners();
		for(ItemListener list : lists)
			libraryComboBox.removeItemListener(list);
		
		// Clear and add libraries
		libraryComboBoxModel.removeAllElements();
		libraryComboBoxModel.addAllElements(libraries);
		libraryComboBox.setSelectedItem(null);
		
		libraryComboBox.setEnabled(libraryComboBoxModel.getSize() > 0);
		
		// Re-enable listeners
		for(ItemListener list : lists)
			libraryComboBox.addItemListener(list);
	}
	
	public void selectLibrary(Library library)
	{
		libraryComboBox.setSelectedItem(library);
		
		boolean enable = library != null;
		// Menu bar
		addImageMenuItem.setEnabled(enable);
		addWebImageMenuItem.setEnabled(enable);
		addFolderMenuItem.setEnabled(enable);
		// Main content
		librarySearchField.setEnabled(enable);
		librarySearchFavorite.setEnabled(enable);
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
		/*final ItemImage image = */itemPanel.addImage(data);
		updateNumImages();
		
		SwingUtilities.invokeLater(() -> {
			updateVisibleImages();
			//updateImageVisibility(image, false);
			
			itemPanel.revalidate();
			itemPanel.repaint();
		});
	}
	
	public void addAllImages(List<ImageData> datas)
	{
		Log.debug("Adding "+datas.size()+" images");
		
		for(ImageData data : datas)
			itemPanel.addImage(data);
		updateNumImages();
		
		SwingUtilities.invokeLater(() -> {
			updateVisibleImages();
			itemPanel.revalidate();
			itemPanel.repaint();
			enableImagesScrollListener(true);
		});
	}
	
	public void setImages(List<ImageData> datas)
	{
		Log.debug("Setting images");
		
		enableImagesScrollListener(false);
		
		itemPanel.clearImages();
		
		for(ItemImage image : visibleImages)
			image.updateVisibility();
		visibleImages.clear();
		
		addAllImages(datas);
	}
	
	public void removeImage(ImageData data)
	{
		itemPanel.removeImage(data.getId());
		updateNumImages();
		
		SwingUtilities.invokeLater(() -> {
			updateVisibleImages();
			
			itemPanel.revalidate();
			itemPanel.repaint();
		});
	}
	
	public void updatedThumbnail(ImageData data)
	{
		itemPanel.updatedThumbnail(data);
	}
	
	public void setImageItemLoading(String id, boolean loading)
	{
		if(loading)
			itemPanel.setItemLoading(id);
		else
			itemPanel.stopItemLoading(id);
	}
	
	public void setImageItemAnimating(String id, ImageFrame[] frames)
	{
		itemPanel.setItemAnimated(id, frames);
	}
	
	public void setImageSize(ItemSize size)
	{
		itemPanel.setItemSize(size);
		
		SwingUtilities.invokeLater(() -> {
			updateVisibleImages();
			
			itemPanel.revalidate();
			itemPanel.repaint();
		});
	}
	
	public void refreshImageMenu()
	{
		//hoveredImage.closeMenu();
		if(hoveredImage != null && hoveredImage.isMenuOpen())
			hoveredImage.openMenu();
	}
	
	public void resetSearch()
	{
		librarySearchField.clear();
		librarySearchFavorite.setSelected(false);
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
		progressBar.setValue(0);
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
	
	public void notifyUpload(ServiceError error, boolean playSound, Callback clickCallback)
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setContent(getUploadErrorMessage(error));
		if(error == ServiceError.NONE)
		{
			notice.setDisplayTime(5000);
			notice.setIcon(NotificationIcon.information);
			notice.addNotificationListener(new NotificationListener() {
				@Override
				public void optionSelected(NotificationOption option) {}
				@Override
				public void closed() {}
				@Override
				public void accepted()
				{
					if(clickCallback != null)
						clickCallback.call();
				}
			});
			
			if(playSound)
				playSoundEffect(UIConstants.uploadSuccessSound);
		}
		else
		{
			notice.setDisplayTime(8000);
			notice.setIcon(NotificationIcon.error);
			
			//if(playSound)
			//	playSoundEffect(GifConstants.uploadErrorSound);
		}
		
		NotificationManager.showNotification(this, notice);
	}
	
	public void notifyLinkCopy(ServiceError error, boolean playSound, Callback clickCallback)
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setContent(getCopyErrorMessage(error));
		if(error == ServiceError.NONE)
		{
			notice.setDisplayTime(5000);
			notice.setIcon(NotificationIcon.information);
			notice.addNotificationListener(new NotificationListener() {
				@Override
				public void optionSelected(NotificationOption option) {}
				@Override
				public void closed() {}
				@Override
				public void accepted()
				{
					if(clickCallback != null)
						clickCallback.call();
				}
			});
			
			//if(playSound)
			//	playSoundEffect(GifConstants.uploadSound);
		}
		else
		{
			notice.setDisplayTime(8000);
			notice.setIcon(NotificationIcon.error);
			notice.setContent(getCopyErrorMessage(error));
			
			//if(playSound)
			//	playSoundEffect(GifConstants.uploadErrorSound);
		}
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
	
	public void notifyFileRelocateError(boolean move)
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.error);
		notice.setContent("Could not "+(move ? "move" : "copy")+" files to library");
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
	
	public void notifyLibraryCreateError()
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.error);
		notice.setContent("Failed to create library");
		NotificationManager.showNotification(this, notice);
	}
	
	public void notifyLibraryImportError()
	{
		WebNotificationPopup notice = new WebNotificationPopup();
		notice.setDisplayTime(5000);
		notice.setIcon(NotificationIcon.error);
		notice.setContent("Failed to import library");
		NotificationManager.showNotification(this, notice);
	}
	
	// Internal updates
	
	private void stopAnimations()
	{
		ItemImage.stopAnimations();
		
		if(hoveredImage != null)
		{
			hoveredImage.closeMenu();
			hoveredImage = null;
		}
	}
	
	private void updateNumImages()
	{
		imagesCount.setValue(itemPanel.getNumImages());
	}
	
	private void updateVisibleImages()
	{
		// Remove thumbnail from images no longer visible
		synchronized(visibleImages)
		{
			for(Iterator<ItemImage> it = visibleImages.iterator(); it.hasNext();)
			{
				ItemImage image = it.next();
				if(updateImageVisibility(image, true))
					it.remove();
			}
		}
		
		// Load thumbnail for newly visible images
		Set<ItemImage> nonVisibleImages = itemPanel.getNonVisible(visibleImages);
		for(ItemImage image : nonVisibleImages)
			updateImageVisibility(image, false);
		revalidate();
	}
	
	private boolean updateImageVisibility(ItemImage image, boolean previouslyVisible)
	{
		if(image.updateVisibility())
		{
			if(previouslyVisible)
			{
				image.getData().setThumbnail(null);
				image.stopAnimation();
			}
			else
			{
				visibleImages.add(image);
				controller.loadThumbnail(image.getData());
			}
			
			return true;
		}
		
		return false;
	}
	
	// Dialog requests
	
	public boolean getNewImagesConfirmation(int numImages)
	{
		return WebOptionPane.showConfirmDialog(this,
				numImages+" new "+(numImages == 1 ? "image was" : "images were")+" found in the library folder.\n"
						+ "Do you want to add "+(numImages == 1 ? "it" : "them")+" now?",
				"New images found",
				WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE) == WebOptionPane.YES_OPTION;
	}
	
	public boolean getRemoveImageConfirmation()
	{
		return WebOptionPane.showConfirmDialog(this,
				"Are you sure you want to remove this image?",
				"Remove image?",
				WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE) == WebOptionPane.YES_OPTION;
	}
	
	public String getTagInput()
	{
		//TODO add recent tags selection
		//TODO add multiple tags
		return WebOptionPane.showInputDialog(this, "Enter a tag:", "New tag", WebOptionPane.QUESTION_MESSAGE);
	}
	
	public Set<String> getMultiTagInput(ImageFrame[] image, TagCache recentTags)
	{
		return MultiTagDialog.showDialog(image, recentTags);
	}
	
	public File getImageFileInput()
	{
		return WebFileChooser.showOpenDialog(this, new Customizer<WebFileChooser>() {
			@Override
			public void customize(WebFileChooser chooser)
			{
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setDialogTitle("Select image");
				chooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription()
					{
						Set<String> types = ImageLoaders.getSupportedImageTypes();
						StringBuilder s = new StringBuilder("Animated images (");
						s.append(types.stream().sorted().collect(Collectors.joining(", ")));
						s.append(')');
						return s.toString();
					}
					
					@Override
					public boolean accept(File f)
					{
						return ImageLoader.IMAGE_FILE_FILTER.accept(f);
					}
				});
			}
		});
	}
	
	public String getImageUrlInput()
	{
		return WebOptionPane.showInputDialog(this, "Image URL:");
	}
	
	public File getImageFolderInput()
	{
		return WebDirectoryChooser.showDialog(this, "Select image folder");
	}
	
	// Helpers
	
	private void enableImagesScrollListener(boolean enable)
	{
		if(enable && !imageScrollListenerEnabled)
			itemScrollPane.getVerticalScrollBar().addAdjustmentListener(imageScrollListener);
		else
			itemScrollPane.getVerticalScrollBar().removeAdjustmentListener(imageScrollListener);
		imageScrollListenerEnabled = enable;
	}
	
	private String getCopyErrorMessage(ServiceError error)
	{
		return LanguageManager.get("giforg.notification.copy_error."+error.name().toLowerCase());
	}
	
	private String getUploadErrorMessage(ServiceError error)
	{
		return LanguageManager.get("giforg.notification.upload_error."+error.name().toLowerCase());
	}
	
	private void playSoundEffect(String name)
	{
		try
		{
			Clip clip = AudioSystem.getClip();
			clip.open(ResourceLoader.loadSound(name));
			clip.start();
		}
		catch(Exception e)
		{
			Log.error("Failed to play sound effect: "+name, e);
		}
	}
	
	//// Static utilities
	
	private static boolean isSelected(ItemEvent evt)
	{
		return evt.getStateChange() == ItemEvent.SELECTED;
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

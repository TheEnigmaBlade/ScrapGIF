package net.enigmablade.gif.ui;

import java.awt.*;
import javax.swing.*;
import net.enigmablade.gif.*;

public class UIConstants
{
	// Sizes
	public static final int THUMBNAIL_SIZE_SMALL	= 100,
							THUMBNAIL_SIZE_NORMAL	= 150,
							THUMBNAIL_SIZE_LARGE	= 200;
	
	public static final int THUMBNAIL_LOAD_SMALL	= 15,
							THUMBNAIL_LOAD_NORMAL	= 20,
							THUMBNAIL_LOAD_LARGE	= 25;
	
	public static final int MIN_IMAGE_WIDTH = 150,
							MAX_IMAGE_WIDTH = MIN_IMAGE_WIDTH*3;
	
	public static final int IMAGE_BUTTON_HEIGHT = 40;
	
	// Performance
	public static final int ANIMATION_DELAY = 150;
	
	public static final Object PREVIEW_INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
	
	// Colors
	public static Color IMAGE_BACKGROUND = new Color(200, 200, 200);
	
	// Icons
	public static ImageIcon newAlbumIcon		= ResourceLoader.loadIcon("photo-album--plus"),
							newAlbumFromIcon	= ResourceLoader.loadIcon("blue-folder-import"),
							importAlbumIcon		= ResourceLoader.loadIcon("photo-album--arrow"),
							exportAlbumIcon		= ResourceLoader.loadIcon("application-export"),
							manageAlbumsIcon	= null,
							exitIcon			= ResourceLoader.loadIcon("door"),
							addImageIcon		= ResourceLoader.loadIcon("image--plus"),
							addWebImageIcon		= ResourceLoader.loadIcon("globe--plus"),
							addFolderIcon		= ResourceLoader.loadIcon("blue-folder--plus"),
							noStarIcon			= ResourceLoader.loadIcon("star-empty"),
							noTagIcon			= ResourceLoader.loadIcon("tag"),
							settingsIcon		= ResourceLoader.loadIcon("wrench-screwdriver"),
							aboutIcon			= ResourceLoader.loadIcon("information"),
							
							exportIcon			= ResourceLoader.loadIcon("big/image-export"),
							moreIcon			= ResourceLoader.loadIcon("big/plus-circle"),
							tagIcon				= ResourceLoader.loadIcon("big/tag"),
							tagFillIcon			= ResourceLoader.loadIcon("big/tag-label"),
							starEnabledIcon		= ResourceLoader.loadIcon("big/star"),
							starDisabledIcon	= ResourceLoader.loadIcon("big/star-empty"),
							
							removeIcon			= ResourceLoader.loadIcon("big/minus"),
							folderClosedIcon	= ResourceLoader.loadIcon("big/blue-folder"),
							folderOpenIcon		= ResourceLoader.loadIcon("big/blue-folder-open");
	
	// Sounds
	public static String uploadSuccessSound = "upload_success";
}

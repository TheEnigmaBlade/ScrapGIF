package net.enigmablade.gif.ui;

import java.awt.*;
import javax.swing.*;
import net.enigmablade.gif.*;

public class UIConstants
{
	// Sizes
	public static final int THUMBNAIL_SIZE_SMALL	= 150,
							THUMBNAIL_SIZE_NORMAL	= 200,
							THUMBNAIL_SIZE_LARGE	= 250;
	
	public static final int THUMBNAIL_LOAD_SMALL	= 10,
							THUMBNAIL_LOAD_NORMAL	= 16,
							THUMBNAIL_LOAD_LARGE	= 22;
	
	public static final int IMAGE_GAP_SMALL		= 10,
							IMAGE_GAP_NORMAL	= 15,
							IMAGE_GAP_LARGE		= 20;
	
	public static final int MIN_IMAGE_WIDTH = 150,
							MAX_IMAGE_WIDTH = MIN_IMAGE_WIDTH*4;
	
	public static final int IMAGE_BUTTON_HEIGHT = 40;
	
	// Performance
	public static final int ANIMATION_DELAY = 125;
	
	public static final Object PREVIEW_INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
	
	// Colors
	public static Color IMAGE_BACKGROUND = new Color(200, 200, 200);
	public static Color IMAGE_SHADOW_COLOR = new Color(20, 20, 20);
	public static float IMAGE_SHADOW_TRANS = 0.6f;
	
	// Icons
	public static ImageIcon newAlbumIcon		= ResourceLoader.loadIcon("photo-album--plus"),
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

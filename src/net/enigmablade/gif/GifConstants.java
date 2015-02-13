package net.enigmablade.gif;

import java.awt.*;
import javax.swing.*;

public class GifConstants
{
	// Sizes
	public static final int THUMBNAIL_SIZE_SMALL	= 150,
							THUMBNAIL_SIZE_NORMAL	= 200,
							THUMBNAIL_SIZE_LARGE	= 250;
	
	public static final int THUMBNAIL_LOAD_SMALL	= 15,
							THUMBNAIL_LOAD_NORMAL	= 25,
							THUMBNAIL_LOAD_LARGE	= 35;
	
	public static final int MIN_IMAGE_WIDTH = 150;
	
	public static final int IMAGE_BUTTON_HEIGHT = 40;
	
	// Performance
	public static Object previewInterpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
	
	// Colors
	public static Color IMAGE_BACKGROUND = new Color(200, 200, 200);
	
	// Icons
	public static ImageIcon newAlbumIcon		= ResourceLoader.loadIcon("photo-album--plus"),
							newAlbumFromIcon	= ResourceLoader.loadIcon("blue-folder-import"),
							importAlbumIcon		= ResourceLoader.loadIcon("photo-album--arrow"),
							manageAlbumsIcon	= null,
							exitIcon			= ResourceLoader.loadIcon("door"),
							addImageIcon		= ResourceLoader.loadIcon("image--plus"),
							addWebImageIcon		= ResourceLoader.loadIcon("globe--plus"),
							addFolderIcon		= ResourceLoader.loadIcon("blue-folder--plus"),
							settingsIcon		= ResourceLoader.loadIcon("wrench-screwdriver"),
							aboutIcon			= ResourceLoader.loadIcon("information"),
							exportIcon			= ResourceLoader.loadIcon("big/image-export"),
							folderClosedIcon	= ResourceLoader.loadIcon("big/blue-folder"),
							folderOpenIcon		= ResourceLoader.loadIcon("big/blue-folder-open"),
							tagIcon				= ResourceLoader.loadIcon("big/tag"),
							tagFillIcon			= ResourceLoader.loadIcon("big/tag-label"),
							starEnabledIcon		= ResourceLoader.loadIcon("big/star"),
							starDisabledIcon	= ResourceLoader.loadIcon("big/star-empty");
	
	//public static ImageIcon loaderAnimation = ResourceLoader.loadAnimatedIcon("loader");
}

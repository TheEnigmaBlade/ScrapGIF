package net.enigmablade.gif.ui.renderers;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.border.Border;

public class DropShadowBorder implements Border
{
	private static enum Position {TOP, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT}
	
	private static final Map<Double,Map<Position,BufferedImage>> CACHE
	= new HashMap<Double,Map<Position,BufferedImage>>();
	
	private Color shadowColor;
	private int shadowSize;
	private float shadowOpacity;
	private int cornerSize;
	
	public DropShadowBorder() {
		this(Color.BLACK, 5);
	}
	
	public DropShadowBorder(Color shadowColor, int shadowSize) {
		this(shadowColor, shadowSize, .5f, 12);
	}
	
	public DropShadowBorder(Color shadowColor, int shadowSize,
			float shadowOpacity, int cornerSize) {
		this.shadowColor = shadowColor;
		this.shadowSize = shadowSize;
		this.shadowOpacity = shadowOpacity;
		this.cornerSize = cornerSize;
	}
	
	@Override
	public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
		/*
		 * 1) Get images for this border
		 * 2) Paint the images for each side of the border that should be painted
		 */
		Map<Position,BufferedImage> images = getImages((Graphics2D)graphics);
		
		Graphics2D g2 = (Graphics2D)graphics.create();
		
		try
		{
			//The location and size of the shadows depends on which shadows are being
			//drawn. For instance, if the left & bottom shadows are being drawn, then
			//the left shadow extends all the way down to the corner, a corner is drawn,
			//and then the bottom shadow begins at the corner. If, however, only the
			//bottom shadow is drawn, then the bottom-left corner is drawn to the
			//right of the corner, and the bottom shadow is somewhat shorter than before.
			
			Point topLeftShadowPoint = null;
			topLeftShadowPoint = new Point();
			topLeftShadowPoint.setLocation(x, y);
			
			Point bottomLeftShadowPoint = null;
			bottomLeftShadowPoint = new Point();
			bottomLeftShadowPoint.setLocation(x, y + height - shadowSize);
			
			Point bottomRightShadowPoint = null;
			bottomRightShadowPoint = new Point();
			bottomRightShadowPoint.setLocation(x + width - shadowSize, y + height - shadowSize);
			
			Point topRightShadowPoint = null;
			topRightShadowPoint = new Point();
			topRightShadowPoint.setLocation(x + width - shadowSize, y);
			
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_SPEED);
			
			Rectangle leftShadowRect =
					new Rectangle(x,
							topLeftShadowPoint.y + shadowSize,
							shadowSize,
							bottomLeftShadowPoint.y - topLeftShadowPoint.y - shadowSize);
			g2.drawImage(images.get(Position.LEFT),
					leftShadowRect.x, leftShadowRect.y,
					leftShadowRect.width, leftShadowRect.height, null);
			
			Rectangle bottomShadowRect =
					new Rectangle(bottomLeftShadowPoint.x + shadowSize,
							y + height - shadowSize,
							bottomRightShadowPoint.x - bottomLeftShadowPoint.x - shadowSize,
							shadowSize);
			g2.drawImage(images.get(Position.BOTTOM),
					bottomShadowRect.x, bottomShadowRect.y,
					bottomShadowRect.width, bottomShadowRect.height, null);
			
			Rectangle rightShadowRect =
					new Rectangle(x + width - shadowSize,
							topRightShadowPoint.y + shadowSize,
							shadowSize,
							bottomRightShadowPoint.y - topRightShadowPoint.y - shadowSize);
			g2.drawImage(images.get(Position.RIGHT),
					rightShadowRect.x, rightShadowRect.y,
					rightShadowRect.width, rightShadowRect.height, null);
			
			Rectangle topShadowRect =
					new Rectangle(topLeftShadowPoint.x + shadowSize,
							y,
							topRightShadowPoint.x - topLeftShadowPoint.x - shadowSize,
							shadowSize);
			g2.drawImage(images.get(Position.TOP),
					topShadowRect.x, topShadowRect.y,
					topShadowRect.width, topShadowRect.height, null);
			
			g2.drawImage(images.get(Position.TOP_LEFT), topLeftShadowPoint.x, topLeftShadowPoint.y, null);
			g2.drawImage(images.get(Position.BOTTOM_LEFT), bottomLeftShadowPoint.x, bottomLeftShadowPoint.y, null);
			g2.drawImage(images.get(Position.BOTTOM_RIGHT), bottomRightShadowPoint.x, bottomRightShadowPoint.y, null);
			g2.drawImage(images.get(Position.TOP_RIGHT), topRightShadowPoint.x, topRightShadowPoint.y, null);
		}
		finally
		{
			g2.dispose();
		}
	}
	
	private Map<Position,BufferedImage> getImages(Graphics2D g2) {
		//first, check to see if an image for this size has already been rendered
		//if so, use the cache. Else, draw and save
		Map<Position,BufferedImage> images = CACHE.get(shadowSize + (shadowColor.hashCode() * .3) + (shadowOpacity * .12));//TODO do a real hash
		if (images == null) {
			images = new HashMap<Position,BufferedImage>();
			
			/*
			 * To draw a drop shadow, I have to:
			 *  1) Create a rounded rectangle
			 *  2) Create a BufferedImage to draw the rounded rect in
			 *  3) Translate the graphics for the image, so that the rectangle
			 *     is centered in the drawn space. The border around the rectangle
			 *     needs to be shadowWidth wide, so that there is space for the
			 *     shadow to be drawn.
			 *  4) Draw the rounded rect as shadowColor, with an opacity of shadowOpacity
			 *  5) Create the BLUR_KERNEL
			 *  6) Blur the image
			 *  7) copy off the corners, sides, etc into images to be used for
			 *     drawing the Border
			 */
			int rectWidth = cornerSize + 1;
			RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, rectWidth, rectWidth, cornerSize, cornerSize);
			int imageWidth = rectWidth + shadowSize * 2;
			BufferedImage image = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
			Graphics2D buffer = (Graphics2D)image.getGraphics();
			
			try {
				buffer.setPaint(new Color(shadowColor.getRed(), shadowColor.getGreen(),
						shadowColor.getBlue(), (int)(shadowOpacity * 255)));
				//                buffer.setColor(new Color(0.0f, 0.0f, 0.0f, shadowOpacity));
				buffer.translate(shadowSize, shadowSize);
				buffer.fill(rect);
			} finally {
				buffer.dispose();
			}
			
			float blurry = 1.0f / (shadowSize * shadowSize);
			float[] blurKernel = new float[shadowSize * shadowSize];
			for (int i=0; i<blurKernel.length; i++) {
				blurKernel[i] = blurry;
			}
			ConvolveOp blur = new ConvolveOp(new Kernel(shadowSize, shadowSize, blurKernel));
			BufferedImage targetImage = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
			((Graphics2D)targetImage.getGraphics()).drawImage(image, blur, -(shadowSize/2), -(shadowSize/2));
			
			int x = 1;
			int y = 1;
			int w = shadowSize;
			int h = shadowSize;
			images.put(Position.TOP_LEFT, getSubImage(targetImage, x, y, w, h));
			x = 1;
			y = h;
			w = shadowSize;
			h = 1;
			images.put(Position.LEFT, getSubImage(targetImage, x, y, w, h));
			x = 1;
			y = rectWidth;
			w = shadowSize;
			h = shadowSize;
			images.put(Position.BOTTOM_LEFT, getSubImage(targetImage, x, y, w, h));
			x = cornerSize + 1;
			y = rectWidth;
			w = 1;
			h = shadowSize;
			images.put(Position.BOTTOM, getSubImage(targetImage, x, y, w, h));
			x = rectWidth;
			y = x;
			w = shadowSize;
			h = shadowSize;
			images.put(Position.BOTTOM_RIGHT, getSubImage(targetImage, x, y, w, h));
			x = rectWidth;
			y = cornerSize + 1;
			w = shadowSize;
			h = 1;
			images.put(Position.RIGHT, getSubImage(targetImage, x, y, w, h));
			x = rectWidth;
			y = 1;
			w = shadowSize;
			h = shadowSize;
			images.put(Position.TOP_RIGHT, getSubImage(targetImage, x, y, w, h));
			x = shadowSize;
			y = 1;
			w = 1;
			h = shadowSize;
			images.put(Position.TOP, getSubImage(targetImage, x, y, w, h));
			
			image.flush();
			CACHE.put(shadowSize + (shadowColor.hashCode() * .3) + (shadowOpacity * .12), images); //TODO do a real hash
		}
		return images;
	}
	
	/**
	 * Returns a new BufferedImage that represents a subregion of the given
	 * BufferedImage.  (Note that this method does not use
	 * BufferedImage.getSubimage(), which will defeat image acceleration
	 * strategies on later JDKs.)
	 */
	private BufferedImage getSubImage(BufferedImage img,
			int x, int y, int w, int h) {
		BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = ret.createGraphics();
		
		try {
			g2.drawImage(img,
					0, 0, w, h,
					x, y, x+w, y+h,
					null);
		} finally {
			g2.dispose();
		}
		
		return ret;
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBorderOpaque() {
		return false;
	}
	
	public int getShadowSize() {
		return shadowSize;
	}
	
	public Color getShadowColor() {
		return shadowColor;
	}
	
	public float getShadowOpacity() {
		return shadowOpacity;
	}
	
	public int getCornerSize() {
		return cornerSize;
	}
	
	public void setShadowColor(Color shadowColor) {
		this.shadowColor = shadowColor;
	}
	
	public void setShadowSize(int shadowSize) {
		this.shadowSize = shadowSize;
	}
	
	public void setShadowOpacity(float shadowOpacity) {
		this.shadowOpacity = shadowOpacity;
	}
	
	public void setCornerSize(int cornerSize) {
		this.cornerSize = cornerSize;
	}
}
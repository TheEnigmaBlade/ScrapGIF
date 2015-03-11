package net.enigmablade.gif.ui.components.item;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;
import com.alee.log.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.ui.*;
import net.enigmablade.gif.ui.components.web.*;
import net.enigmablade.gif.ui.layouts.*;

public class ItemPanel extends CustomWebPanel
{
	private UIController controller;
	
	//Components
	
	/*private Component dragComponent;
	private int dragComponentIndex;
	
	private Point mousePoint;*/
	private DraggableFlowLayout layout;
	
	//Listeners
	
	private MouseListener itemMouseListener;
	private MouseMotionListener itemMouseMotionListener;
	//private AtomicBoolean dragging = new AtomicBoolean(false);
	
	//Data
	
	private Map<String, ItemImage> itemsById;
	private ItemSize itemSize = ItemSize.NORMAL;
	
	//Initialization
	
	public ItemPanel(UIController controller)
	{
		this.controller = controller;
		
		setFocusable(true);
		setMinimumWidth(0);
		setPreferredWidth(0);
		setLayout(layout = new DraggableFlowLayout(itemSize.getGap(), itemSize.getGap()));
		
		initData();
		initListeners();
	}
	
	private void initData()
	{
		itemsById = new HashMap<>();
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
		
		/*addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt)
			{
				mousePoint = evt.getPoint();
				//Log.info("Mouse pressed: "+mousePoint);
				
				if(evt.getButton() == MouseEvent.BUTTON1)
				{
					dragComponent = getComponentAt(mousePoint);
					for(dragComponentIndex = getComponentCount()-1; dragComponentIndex >= 0; dragComponentIndex--)
						if(getComponent(dragComponentIndex) == dragComponent)
							break;
					
					Log.info("Selected index: "+dragComponentIndex);
					layout.setDraggedComponent(dragComponentIndex);
					dragging.set(true);
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent evt)
			{
				Point releaseMousePoint = evt.getPoint();
				//Log.info("Mouse released: "+releaseMousePoint);
				
				if(dragComponentIndex >= 0)
				{
					layout.setDraggedComponent(dragComponentIndex = -1);
					layout.layoutContainer(ItemPanel.this);
					
					//if(!releaseMousePoint.equals(mousePoint))
					{
						Component releaseComponent = getComponentAt(releaseMousePoint);
						int releaseComponentIndex;
						for(releaseComponentIndex = getComponentCount()-1; releaseComponentIndex >= 0; releaseComponentIndex--)
							if(getComponent(releaseComponentIndex) == releaseComponent)
								break;
						
						Log.info("Released index: "+releaseComponentIndex);
						if(releaseComponentIndex >= 0)
						{
							//remove(dragComponentIndex);
							add(dragComponent, releaseComponentIndex);
							layout.layoutContainer(ItemPanel.this);
						}
					}
					
					dragging.set(false);
				}
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt)
			{
				//Log.info("asdf");
				if(dragComponentIndex >= 0)
				{
					Point newMousePoint = evt.getPoint();
					int dx = newMousePoint.x - mousePoint.x;
					int dy = newMousePoint.y - mousePoint.y;
					mousePoint = newMousePoint;
					
					if(dragComponentIndex >= 0)
						dragComponent.setLocation(dragComponent.getX()+dx, dragComponent.getY()+dy);
				}
			}
		});
		
		itemMouseListener = new MouseListener() {
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				if(!dragging.get())
				{
					ItemImage item = (ItemImage)evt.getSource();
					item.startAnimation(null);
				}
				//ItemPanel.this.dispatchEvent(evt);
			}
			
			@Override
			public void mouseExited(MouseEvent evt)
			{
				if(!dragging.get())
				{
					ItemImage item = (ItemImage)evt.getSource();
					item.stopAnimation();
				}
				//ItemPanel.this.dispatchEvent(evt);
			}

			@Override
			public void mouseClicked(MouseEvent evt)
			{
				Component source = (Component)evt.getSource();
				MouseEvent evt2 = new MouseEvent(ItemPanel.this, evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX()+source.getX(), evt.getY()+source.getY(), evt.getXOnScreen(), evt.getYOnScreen(), evt.getClickCount(), evt.isPopupTrigger(), evt.getButton());
				ItemPanel.this.dispatchEvent(evt2);
			}

			@Override
			public void mousePressed(MouseEvent evt)
			{
				Component source = (Component)evt.getSource();
				MouseEvent evt2 = new MouseEvent(ItemPanel.this, evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX()+source.getX(), evt.getY()+source.getY(), evt.getXOnScreen(), evt.getYOnScreen(), evt.getClickCount(), evt.isPopupTrigger(), evt.getButton());
				ItemPanel.this.dispatchEvent(evt2);
			}

			@Override
			public void mouseReleased(MouseEvent evt)
			{
				Component source = (Component)evt.getSource();
				MouseEvent evt2 = new MouseEvent(ItemPanel.this, evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX()+source.getX(), evt.getY()+source.getY(), evt.getXOnScreen(), evt.getYOnScreen(), evt.getClickCount(), evt.isPopupTrigger(), evt.getButton());
				ItemPanel.this.dispatchEvent(evt2);
			}
		};*/
	}
	
	public ItemImage addImage(ImageData data)
	{
		ItemImage image = new ItemImage(controller, data, itemSize);
		image.addMouseListener(itemMouseListener);
		image.addMouseMotionListener(itemMouseMotionListener);
		
		itemsById.put(image.getData().getId(), image);
		return (ItemImage)super.add(image);
	}
	
	public ItemImage removeImage(String id)
	{
		ItemImage image = itemsById.get(id);
		image.removeMouseListener(itemMouseListener);
		image.removeMouseMotionListener(itemMouseMotionListener);
		
		itemsById.remove(image.getData().getId());
		super.remove(image);
		return image;
	}
	
	public void updatedThumbnail(ImageData data)
	{
		ItemImage image = itemsById.get(data.getId());
		if(image != null)
			image.setSize(itemSize);
	}
	
	public void clearImages()
	{
		itemsById.clear();
		super.removeAll();
	}
	
	@Override
	public Insets getInsets()
	{
		return new Insets(10, 10, 10, 10);
	}
	
	//Functionality
	
	public void setItemLoading(String id)
	{
		ItemImage image = itemsById.get(id);
		if(image != null)
			image.setLoading();
	}
	
	public void stopItemLoading(String id)
	{
		ItemImage image = itemsById.get(id);
		if(image != null)
			image.stopLoading(false);
	}
	
	public void setItemAnimated(String id, ImageFrame[] frames)
	{
		ItemImage image = itemsById.get(id);
		if(image != null)
			image.startAnimation(frames);
	}
	
	public void stopItemAnimations()
	{
		for(String id : itemsById.keySet())
			itemsById.get(id).stopAnimation();
	}
	
	public Set<ItemImage> getNonVisible(Set<ItemImage> visible)
	{
		return itemsById.values().stream().filter(i -> !visible.contains(i)).collect(Collectors.toSet());
	}
	
	//Accessor methods
	
	public int getNumImages()
	{
		return itemsById.size();
	}
	
	public void setItemSize(ItemSize itemSize)
	{
		Objects.requireNonNull(itemSize);
		
		if(this.itemSize != itemSize)
		{
			Log.debug("Updating items' size");
			this.itemSize = itemSize;
			// Update layout
			layout.setGap(itemSize.getGap(), itemSize.getGap());
			// Update individual items
			for(ItemImage img : itemsById.values())
				img.setSize(itemSize);
		}
	}
	
	public void setItemMouseListener(MouseListener listener)
	{
		itemMouseListener = listener;
	}
	
	public void setItemMouseMotionListener(MouseMotionListener listener)
	{
		itemMouseMotionListener = listener;
	}
}

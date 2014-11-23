package net.enigmablade.gif.ui.components.item;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.ui.components.web.*;
import net.enigmablade.gif.ui.layouts.*;

public class ItemPanel extends CustomWebPanel
{
	//Components
	
	/*private Component dragComponent;
	private int dragComponentIndex;
	
	private DraggableFlowLayout layout;
	private Point mousePoint;*/
	
	//Listeners
	
	private MouseListener itemMouseListener;
	private MouseMotionListener itemMouseMotionListener;
	//private AtomicBoolean dragging = new AtomicBoolean(false);
	
	//Data
	
	private Map<String, ItemImage> itemsById;
	
	//Initialization
	
	public ItemPanel()
	{
		setFocusable(true);
		setMinimumWidth(0);
		setPreferredWidth(0);
		setLayout(/*layout = */new DraggableFlowLayout(20, 20));
		
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
	
	public Component addImage(ImageData data)
	{
		ItemImage image = new ItemImage(data);
		image.addMouseListener(itemMouseListener);
		image.addMouseMotionListener(itemMouseMotionListener);
		
		itemsById.put(image.getData().getId(), image);
		return super.add(image);
	}
	
	public void removeImage(String id)
	{
		ItemImage image = itemsById.get(id);
		image.removeMouseListener(itemMouseListener);
		image.removeMouseMotionListener(itemMouseMotionListener);
		
		itemsById.remove(image.getData().getId());
		super.remove(image);
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
	
	//Accessor methods
	
	public int getNumImages()
	{
		return itemsById.size();
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
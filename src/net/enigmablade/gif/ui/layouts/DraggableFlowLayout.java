package net.enigmablade.gif.ui.layouts;

import java.awt.*;
import java.util.*;
import com.alee.extended.layout.*;

public class DraggableFlowLayout extends AbstractLayoutManager
{
	private int gapH, gapV;
	
	private int maxWidth, maxHeight;
	
	private int dragIndex = -1;
	
	public DraggableFlowLayout()
	{
		this(0, 0);
	}
	
	public DraggableFlowLayout(int gapH, int gapV)
	{
		this.gapH = gapH;
		this.gapV = gapV;
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		if(parent.getComponentCount() == 0)
		{
			maxWidth = maxHeight = 0;
			return;
		}
		
		Insets insets = parent.getInsets();
		int parentWidth = parent.getWidth() - insets.left - insets.right;
		
		maxWidth = maxHeight = 0;
		
		//Calculating row layout
		
		ArrayList<Row> rows = new ArrayList<>();
		Row currentRow = new Row();
		rows.add(currentRow);
		
		for(int n = 0; n < parent.getComponentCount(); n++)
		{
			Component c = parent.getComponent(n);
			Dimension dim = c.getPreferredSize();
			
			if(n > 0 && currentRow.width + gapH + dim.width > parentWidth)
			{
				//Update maxes
				maxWidth = Math.max(maxWidth, currentRow.width);
				maxHeight += (rows.isEmpty() ? 0 : gapV) + currentRow.height;
				
				//Reset row
				currentRow = new Row();
				rows.add(currentRow);
			}
			
			currentRow.add(c, dim);
		}
		
		maxWidth = Math.max(maxWidth, currentRow.width);
		maxHeight += currentRow.height + insets.top + insets.bottom;
		
		//Layout components
		
		int y = insets.top;
		
		for(Row row : rows)
		{
			int flexGap = (parentWidth - row.width)/(row.size() + 1);
			int x = insets.left + flexGap;
			
			for(int n = 0; n < row.size(); n++)
			{
				Component c = row.components.get(n);
				Dimension dim = c.getPreferredSize();
				
				c.setBounds(x, y, dim.width, dim.height);
				c.setLocation(x, y);
				
				x += c.getWidth() + gapH + flexGap;
			}
			
			y += row.height + gapV;
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		//layoutContainer(parent);
		return new Dimension(maxWidth, maxHeight);
	}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		//layoutContainer(parent);
		return new Dimension(0, maxHeight);
	}
	
	private class Row
	{
		public int width, height;
		public ArrayList<Component> components;
		
		public Row()
		{
			height = 0;
			components = new ArrayList<>();
		}
		
		public void add(Component c, Dimension cDim)
		{
			height = Math.max(height, cDim.height);
			width += (components.size() == 0 ? 0 : gapH) + cDim.width;
			components.add(c);
		}
		
		public int size()
		{
			return components.size();
		}
	}
	
	//Accessor methods
	
	public void setDraggedComponent(int index)
	{
		dragIndex = index;
	}
}

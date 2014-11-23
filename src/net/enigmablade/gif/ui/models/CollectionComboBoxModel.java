package net.enigmablade.gif.ui.models;

import java.util.*;
import javax.swing.*;

public class CollectionComboBoxModel<T> extends DefaultComboBoxModel<T>
{
	public void addAllElements(Collection<T> items)
	{
		for(T item : items)
			addElement(item);
	}
}

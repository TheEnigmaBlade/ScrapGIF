package net.enigmablade.gif.ui.components.web;

import com.alee.laf.label.*;

public class WebValueLabel extends WebLabel
{
	private String baseText;
	private String value;
	
	public WebValueLabel()
	{
		baseText = value = "";
	}
	
	public WebValueLabel(String baseText)
	{
		this.baseText = baseText;
		setValue("");
	}
	
	public WebValueLabel(String baseText, String value)
	{
		this(baseText);
		setValue(value);
	}
	
	public WebValueLabel(String baseText, int value)
	{
		this(baseText);
		setValue(value);
	}
	
	public WebValueLabel(String baseText, long value)
	{
		this(baseText);
		setValue(value);
	}
	
	public WebValueLabel(String baseText, double value)
	{
		this(baseText);
		setValue(value);
	}
	
	public WebValueLabel(String baseText, float value)
	{
		this(baseText);
		setValue(value);
	}
	
	public WebValueLabel(String baseText, boolean value)
	{
		this(baseText);
		setValue(value);
	}
	
	public void setValue(String value)
	{
		this.value = value;
		setText(baseText+value);
	}
	
	public void setValue(int value)
	{
		setValue(String.valueOf(value));
	}
	
	public void setValue(long value)
	{
		setValue(String.valueOf(value));
	}
	
	public void setValue(double value)
	{
		setValue(String.valueOf(value));
	}
	
	public void setValue(float value)
	{
		setValue(String.valueOf(value));
	}
	
	public void setValue(boolean value)
	{
		setValue(String.valueOf(value));
	}
	
	public String getValue()
	{
		return value;
	}
}

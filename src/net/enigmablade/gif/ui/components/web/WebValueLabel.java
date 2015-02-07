package net.enigmablade.gif.ui.components.web;

import com.alee.laf.label.*;

public class WebValueLabel extends WebLabel
{
	private String baseText, endText;
	private String value;
	
	public WebValueLabel()
	{
		baseText = endText = value = "";
	}
	
	public WebValueLabel(String baseText)
	{
		this(baseText, "");
	}
	
	public WebValueLabel(String baseText, String endText)
	{
		this.baseText = baseText;
		this.endText = endText;
		setValue("");
	}
	
	public WebValueLabel(String baseText, Object value)
	{
		this(baseText);
		setValue(value.toString());
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
		setText(baseText+value+endText);
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

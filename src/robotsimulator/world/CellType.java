package robotsimulator.world;

import java.awt.Color;

public class CellType 
{
	private String id;
	private String label;
	private int width, height;
	private Color color;
	
	public CellType(String i, String n, int w, int h, Color c)
	{
		id = i;
		label = n;
		width = w;
		height = h;
		color = c;
	}
	
	public String getID()
	{
		return id;
	}

	public String getLabel() 
	{
		return label;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}

	public Color getColor() 
	{
		return color;
	}
	
}

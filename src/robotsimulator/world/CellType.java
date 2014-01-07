package robotsimulator.world;

import java.awt.Color;
import java.io.BufferedWriter;

import robotsimulator.Simulator;

public class CellType 
{
	private String id;
	private String label;
	private int width, height;
	private Color color;
	private boolean clip;
	
	public CellType(String i, String n, int w, int h, boolean cl, Color c)
	{
		id = i;
		label = n;
		width = w;
		height = h;
		clip = cl;
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
	
	public boolean doesClip()
	{
		return clip;
	}

	public Color getColor() 
	{
		return color;
	}
}

package robotsimulator.world;

import java.awt.Color;

public class CellType 
{
	String id;
	int width, height;
	Color color;
	
	public CellType(String i, int w, int h, Color c)
	{
		id = i;
		width = w;
		height = h;
		color = c;
	}
	
}

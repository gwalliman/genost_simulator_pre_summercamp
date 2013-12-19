package robotsimulator.world;

import java.util.ArrayList;

import robotsimulator.RobotSimulator;
import robotsimulator.worldobject.Block;

public class World 
{
	private int width;
	private int height;
	
	private Point[][] grid;
	private ArrayList<Object> objects = new ArrayList<Object>();
	
	public World(int w, int h)
	{
		width = w;
		height = h;
		
		if(width > 0 && height > 0)
		{
			grid = new Point[width][height];
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					grid[x][y] = new Point(x, y);
				}
			}
		}
		else
		{
			RobotSimulator.println("World must have a positive width and height!");
			RobotSimulator.halt();
		}
	}
	
	public void addBlock(int w, int h, int x, int y)
	{
		Block b = new Block(w, h);
		objects.add(b);
		
	}
}

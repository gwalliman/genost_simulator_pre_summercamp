package robotsimulator.world;

import java.util.ArrayList;

import robotsimulator.RobotSimulator;
import robotsimulator.worldobject.Block;

public class World 
{
	private int width;
	private int height;
	int gridWidth = 20;
	int gridHeight = 20;
	
	private Point[][] points;
	private Cell[][] grid;
	private ArrayList<Block> blocks = new ArrayList<Block>();
	
	public World(int w, int h)
	{
		width = w;
		height = h;
		
		if(width > 0 && height > 0)
		{
			points = new Point[width][height];
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					points[x][y] = new Point();
				}
			}
			
			grid = new Cell[width / gridWidth][height / gridHeight];
			for(int x = 0; x < width / gridWidth; x++)
			{
				for(int y = 0; y < height / gridHeight; y++)
				{
					grid[x][y] = new Cell(x * gridWidth, y * gridHeight, gridWidth, gridHeight, 0);
				}
			}
		}
		else
		{
			RobotSimulator.println("World must have a positive width and height!");
			RobotSimulator.halt();
		}
	}
	
	public ArrayList<Block> getBlocks()
	{
		return blocks;
	}
	
	public void addBlock(Block b)
	{
		blocks.add(b);
		int x0 = (int) ((b.getCenterX()) - (b.getWidth() / 2));
		int y0 = (int) ((b.getCenterY()) - (b.getHeight() / 2));
		int x1 = (int) (x0 + b.getWidth());
		int y1 = (int) (y0 + b.getHeight());
		
		for(int x = x0; x <= x1; x++)
		{
			for(int y = y0; y <= y1; y++)
			{
				points[x][y].occupy(b);
			}
		}
	}
	
	public void removeBlock(Block b)
	{
		blocks.remove(b);
		int x0 = (int) ((b.getCenterX()) - (b.getWidth() / 2));
		int y0 = (int) ((b.getCenterY()) - (b.getHeight() / 2));
		int x1 = (int) (x0 + b.getWidth());
		int y1 = (int) (y0 + b.getHeight());
		
		for(int x = x0; x <= x1; x++)
		{
			for(int y = y0; y <= y1; y++)
			{
				points[x][y].unOccupy();
			}
		}
	}
	
	public void toggleCell(int x, int y)
	{
		try
		{
			int cellX = x / gridWidth;
			int cellY = y / gridHeight;
			
			Cell c = grid[cellX][cellY];
			
			if(!c.isOccupied())
			{
				Block b = new Block(c.getWidth(), c.getHeight(), c.getCenterX(), c.getCenterY(), c.getAngle());
				c.occupy(b);
			
				addBlock(b);
			}
			else
			{
				Block b = c.getBlock();
				c.unOccupy();
				
				removeBlock(b);
			}
		}
		catch(Exception e)
		{
			
		}
	}
}

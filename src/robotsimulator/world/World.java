package robotsimulator.world;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.worldobject.Block;

public class World 
{
	private Simulator sim;
	private int width;
	private int height;
	int gridWidth = 20;
	int gridHeight = 20;
	
	private Point[][] points;
	private Cell[][] grid;
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private Rectangle2D boundary;
	
	public World(int w, int h, Simulator s)
	{
		width = w;
		height = h;
		sim = s;
		
		boundary = new Rectangle2D.Double(0, 0, width, height);
		
		if(width > 0 && height > 0)
		{
			points = new Point[width][height];
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					points[x][y] = new Point(x, y);
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
	
	public Point[][] getWorldPoints()
	{
		return points;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public Rectangle2D getBoundary() 
	{
		return boundary;
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
				Block b = new Block(c.getWidth(), c.getHeight(), c.getCenterX(), c.getCenterY(), c.getAngle(), sim);
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
	
	/* This code adapted from Garrett Drown's MazeNav simulator */
	public static ArrayList<Point> getLine(double x1, double y1, double x2, double y2)
    {
		ArrayList<Point> points = new ArrayList<Point>();
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double step = 1.0 / distance;

        for (double progress = 0; progress <= 1; progress += step)
        {
            int posX = (int)(x1 * (1 - progress) + x2 * (progress));
            int posY = (int)(y1 * (1 - progress) + y2 * (progress));
            
            boolean found = false;

            for(Point p : points)
            {
            	if(p.compare(posX, posY))
            	{
            		found = true;
            		break;
            	}
            }
            
            if(!found)
            {
            	points.add(new Point(posX, posY));
            }
        }
        
        return points;
    }
}

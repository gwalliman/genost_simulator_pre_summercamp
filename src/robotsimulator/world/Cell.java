package robotsimulator.world;

import robotsimulator.worldobject.Block;

public class Cell 
{
	int x0, y0, width, height, angle;
	Block b;
	
	public Cell(int x, int y, int w, int h, int a)
	{
		x0 = x;
		y0 = y;
		width = w;
		height = h;
		angle = a;
	}
	
	public boolean isOccupied()
	{
		if(b != null)
			return true;
		else return false;
	}
	
	public void occupy(Block b0)
	{
		b = b0;
	}
	
	public void unOccupy()
	{
		b = null;
	}

	public int getWidth() 
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getAngle()
	{
		return angle;
	}
	
	public int getCenterX()
	{
		return x0 + width / 2;
	}
	
	public int getCenterY()
	{
		return y0 + width / 2;
	}
	
	public Block getBlock()
	{
		return b;
	}
}

package robotsimulator.worldobject;

public class Block 
{
	private int width, height, centerX, centerY;
	
	public Block(int w, int h, int x, int y)
	{
		width = w;
		height = h;
		centerX = x;
		centerY = y;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getCenterX()
	{
		return centerX;
	}
	
	public int getCenterY()
	{
		return centerY;
	}
	
	public int getX0()
	{
		return centerX - (width / 2);
	}
	
	public int getY0()
	{
		return centerY - (height / 2);
	}
}

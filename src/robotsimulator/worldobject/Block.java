package robotsimulator.worldobject;

public class Block extends WorldObject
{
	private int x0, x1, x2, x3;
	private int y0, y1, y2, y3;
	
	private int centerX, centerY;
	
	private int width;
	private int height;
	
	public Block(int w, int h)
	{
		width = w;
		height = h;
	}

	public int[] getCenter() 
	{
	}
}

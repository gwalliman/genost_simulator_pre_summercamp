package robotsimulator.world;

public class Point 
{
	private int x;
	private int y;
	
	private Object block;
	
	public Point(int x0, int y0)
	{
		x = x0;
		y = y0;
	}
	
	public void occupy(Object b)
	{
		block = b;
	}
	
	public void unOccupy()
	{
		block = null;
	}
	
	public boolean isOccupied()
	{
		if(block == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public Object getOccupier()
	{
		return block;
	}
}

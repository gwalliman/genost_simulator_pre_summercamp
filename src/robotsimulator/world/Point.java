package robotsimulator.world;

import robotsimulator.worldobject.Block;

public class Point 
{
	private Block obj;
	
	public Point()
	{
	}
	
	public void occupy(Block b)
	{
		obj = b;
	}
	
	public void unOccupy()
	{
		obj = null;
	}
	
	public boolean isOccupied()
	{
		if(obj == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public Block getOccupier()
	{
		return obj;
	}
}

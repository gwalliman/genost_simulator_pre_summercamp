package robotsimulator.world;

import robotsimulator.worldobject.Block;

public class Cell 
{
	CellType cellType;
	Block b;
	
	public Cell(int x, int y, int w, int h, int a, CellType c)
	{
		b = new Block(a, a, a, a, a, null);
		cellType = c;
	}
	
	public Block getBlock()
	{
		return b;
	}
}

package robotsimulator;

import java.util.ArrayList;

import robotsimulator.gui.GUI;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

public class Simulator 
{
	private GUI gui;
	private World world;
	private ArrayList<Block> blocks = new ArrayList<Block>();
	
	public Simulator(int width, int height)
	{
		world = new World(width, height);
		gui = new GUI(width, height, this);
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public void addBlock(int w, int h, int x, int y)
	{
		Block b = new Block(w, h, x, y);
		world.addBlock(b);
	}
}

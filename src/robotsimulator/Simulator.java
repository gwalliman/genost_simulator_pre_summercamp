package robotsimulator;

import java.util.ArrayList;

import robotsimulator.gui.GUI;
import robotsimulator.robot.Robot;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

public class Simulator 
{
	private GUI gui;
	private World world;
	private Robot robot;
	
	private ArrayList<Block> blocks = new ArrayList<Block>();
	
	public Simulator(int width, int height, int fps)
	{
		world = new World(width, height, this);
		robot = new Robot(this);
		gui = new GUI(width, height, fps, this);
	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public void addBlock(int w, int h, int x, int y, int a)
	{
		Block b = new Block(w, h, x, y, a, this);
		world.addBlock(b);
	}
}

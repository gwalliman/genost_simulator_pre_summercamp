package robotsimulator;

import robotsimulator.gui.GUI;
import robotsimulator.world.World;

public class RobotSimulator 
{
	private static GUI g;
	private static World w;
	
	public static void println(String m) 
	{
		System.out.println(m);
	}

	public static void halt() 
	{
		System.exit(0);
	}
	
	public static void addToGUI()

	public static void main(String[] args) 
	{
		int width = 700;
		int height = 500;
		g = new GUI(width, height);
		w = new World(width, height);
	}
}

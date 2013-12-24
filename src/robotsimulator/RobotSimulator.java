package robotsimulator;

import robotinterpreter.RobotInterpreter;

public class RobotSimulator
{
	public static void println(String m) 
	{
		System.out.println(m);
	}

	public static void halt() 
	{
		System.exit(0);
	}
	
	public static void main(String[] args) 
	{
		int guiWidth = 500;
		int guiHeight = 500;
		int guiFPS = 30;
		
		Simulator s = new Simulator(guiWidth, guiHeight, guiFPS);
	}
}

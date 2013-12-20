package robotsimulator;

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
		Simulator s = new Simulator(500, 500);
	}
}

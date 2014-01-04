package robotsimulator;

import robotinterpreter.RobotInterpreter;
import robotinterpreter.RobotListener;
import robotsimulator.gui.GUI;
import robotsimulator.robot.Robot;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

public class Simulator implements RobotListener 
{
	private GUI gui;
	private World world;
	private Robot robot;
	
	public Simulator(int width, int height, int fps)
	{
		world = new World(width, height, this);
		robot = new Robot(this);
		gui = new GUI(width, height, fps, this);
		
		RobotInterpreter interpreter = new RobotInterpreter();
		interpreter.addRobotListener(this);
	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public GUI getGUI() 
	{
		return gui;
	}
	
	public void addBlock(int w, int h, int x, int y, int a)
	{
		Block b = new Block(w, h, x, y, a, this);
		world.addBlock(b);
	}

	public void driveForward() 
	{
		robot.stop();
		robot.drive('f');
	}

	public void driveBackwards() 
	{
		robot.stop();
		robot.drive('b');		
	}

	public void turnLeft() 
	{
		robot.stop();
		robot.turn('l');
	}

	public void turnRight() 
	{
		robot.stop();
		robot.turn('r');
	}

	public void stop() 
	{
		robot.stop();
	}

	public int getSonarData(int num) 
	{
		return (int) Math.round(robot.getSonarSensor(num).getSensorValue());
	}

	public int getBearing() 
	{
		int angle = ((int) robot.getAngle()) + 90;
		if(angle > 360)
			angle -= 360;
		else if(angle < 0)
			angle += 360;
		return angle;
	}
	
	public void driveDistance(int dist)
	{
		robot.stop();
		robot.drive(dist);
		while(robot.getStatus() != 's') 
		{ 
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{

			}
		}
	}
	
	public void turnAngle(int angle) 
	{
		robot.stop();
		robot.turn(angle);
		while(robot.getStatus() != 's') 
		{ 
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{

			}
		}
	}

	public void turnToBearing(int bearing) 
	{
		robot.stop();

		int curBearing = getBearing();
		
		if(bearing > curBearing)
		{
			turnAngle(bearing - curBearing);
		}
		else if(bearing < curBearing)
		{
			turnAngle(curBearing - bearing);
		}
	}

	@Override
	public void print(String s) 
	{
		System.out.print(s);		
	}

	@Override
	public void println(String s) {
		System.out.println(s);		
	
	}

	@Override
	public void error(String var, String e) {
		System.out.println(e);		
	}
}

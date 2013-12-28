package robotsimulator;

import java.util.ArrayList;

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
	
	private ArrayList<Block> blocks = new ArrayList<Block>();
	
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
		/*double x0 = robot.getCenterX();
		double y0 = robot.getCenterY();
		
		if(dist > 0)
		{
			robot.drive('f');
		}
		else if(dist < 0)
		{
			robot.drive('b');
			dist = dist * -1;
		}
		
		double x1 = x0;
		double y1 = y0;
		
		while(Math.hypot(x1 - x0, y1 - y0) < dist)
		{
			x1 = robot.getCenterX();
			y1 = robot.getCenterY();
		}
		
		robot.stop();*/
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
		
		/*double prevAngle = robot.getAngle();
		double total = 0;
		
		double goalAngle = prevAngle + angle;
		if(goalAngle > 360)
		{
			goalAngle -= 360;
		}
		else if(goalAngle < 0)
		{
			goalAngle += 360;
		}
		
		double curAngle;
		
		if(angle > 0)
		{
			robot.turn('r');
			while(total < angle)
			{
				curAngle = robot.getAngle();
				
				if(curAngle < prevAngle)
				{
					total += curAngle + 360 - prevAngle;
				}
				else
				{
					total += curAngle - prevAngle;
				}
				prevAngle = curAngle;
			}
		}
		else if(angle < 0)
		{
			robot.turn('l');
			while(total > angle)
			{
				curAngle = robot.getAngle();

				if(curAngle > prevAngle)
				{
					total += curAngle - 360 - prevAngle;
				}
				else
				{
					total += curAngle - prevAngle;
				}
				prevAngle = curAngle;
			}
		}
		robot.stop();
		
		if((int) robot.getAngle() != (int) goalAngle)
		{
			curAngle = robot.getAngle();
			
			turnAngle((int)(goalAngle - curAngle));
			System.out.println(goalAngle - robot.getAngle());
		}*/
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
}

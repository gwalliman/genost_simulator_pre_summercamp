package robotsimulator;

import java.awt.Color;
import java.net.URL;

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

		world.setGridWidth(32);
		world.setGridHeight(32);
		
		world.setCellType("loz_wall1", "Wall 1", 1, 1, Color.blue);
		world.setCellType("loz_wall2", "Wall 2", 1, 1, Color.green);
		world.setCellType("loz_wall3", "Wall 3", 1, 1, Color.red);
		world.setCellType("loz_floor1", "Floor 1", 1, 1, Color.blue);
		world.setCellType("loz_floor2", "Floor 2", 1, 1, Color.black);
		
		URL path = Simulator.class.getResource("/robotsimulator/themes/loz/wall1.png");	
	    
	    world.setCellTheme("loz_wall1", Simulator.class.getResource("/robotsimulator/themes/loz/wall1.png"));
		world.setCellTheme("loz_wall2", Simulator.class.getResource("/robotsimulator/themes/loz/wall2.png"));
		world.setCellTheme("loz_wall3", Simulator.class.getResource("/robotsimulator/themes/loz/wall3.png"));
		world.setCellTheme("loz_floor1", Simulator.class.getResource("/robotsimulator/themes/loz/floor1.png"));
		world.setCellTheme("loz_floor2", Simulator.class.getResource("/robotsimulator/themes/loz/floor2.png"));

		
		int sonarLen = 750;
		int fov = 25;

		//THESE SHOULD BE ADDED IN CLOCKWISE STARTING FROM FRONT-LEFT
		robot.addSonar(this, "Front-Left", robot.getX0(), robot.getY0(), sonarLen, 315, fov);
		robot.addSonar(this, "Front", robot.getCenterFrontX(), robot.getCenterFrontY(), sonarLen, 0, fov);
		robot.addSonar(this, "Front-Right", robot.getX1(), robot.getY1(), sonarLen, 45, fov);
		robot.addSonar(this, "Right", robot.getCenterRightX(), robot.getCenterRightY(), sonarLen, 90, fov);
		robot.addSonar(this, "Rear", robot.getCenterRearX(), robot.getCenterRearY(), sonarLen, 180, fov);
		robot.addSonar(this, "Left", robot.getCenterLeftX(), robot.getCenterLeftY(), sonarLen, 270, fov);
		
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

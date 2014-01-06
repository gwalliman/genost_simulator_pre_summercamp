package robotsimulator;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	private static String newline = "\n";
	
	public Simulator()
	{
		/*
		 * SETTING ROBOT PARAMS
		 */
		int centerX = 100;
		int centerY = 100;
		int angle = 0;
		robot = new Robot(centerX, centerY, angle, this);

		int sonarLen = 750;
		int fov = 25;

		//THESE SHOULD BE ADDED IN CLOCKWISE STARTING FROM FRONT-LEFT
		robot.addSonar(this, "Front-Left", robot.getX0(), robot.getY0(), sonarLen, 315, fov);
		robot.addSonar(this, "Front", robot.getCenterFrontX(), robot.getCenterFrontY(), sonarLen, 0, fov);
		robot.addSonar(this, "Front-Right", robot.getX1(), robot.getY1(), sonarLen, 45, fov);
		robot.addSonar(this, "Right", robot.getCenterRightX(), robot.getCenterRightY(), sonarLen, 90, fov);
		robot.addSonar(this, "Rear", robot.getCenterRearX(), robot.getCenterRearY(), sonarLen, 180, fov);
		robot.addSonar(this, "Left", robot.getCenterLeftX(), robot.getCenterLeftY(), sonarLen, 270, fov);
		
		/*
		world.setCellType("loz_wall1", "Wall 1", 1, 1, true, Color.blue);
		world.setCellType("loz_wall2", "Wall 2", 1, 1, true, Color.green);
		world.setCellType("loz_wall3", "Wall 3", 1, 1, true, Color.red);
		world.setCellType("loz_floor1", "Floor 1", 1, 1, false, Color.blue);
		world.setCellType("loz_floor2", "Floor 2", 1, 1, false, Color.black);
		   
	    world.setCellTheme("loz_wall1", Simulator.class.getResource("/robotsimulator/themes/loz/wall1.png"));
		world.setCellTheme("loz_wall2", Simulator.class.getResource("/robotsimulator/themes/loz/wall2.png"));
		world.setCellTheme("loz_wall3", Simulator.class.getResource("/robotsimulator/themes/loz/wall3.png"));
		world.setCellTheme("loz_floor1", Simulator.class.getResource("/robotsimulator/themes/loz/floor1.png"));
		world.setCellTheme("loz_floor2", Simulator.class.getResource("/robotsimulator/themes/loz/floor2.png"));
		*/
		
		/*
		 * SETTING BASIC WORLD PARAMS
		 */
		int gridWidth = 32;
		int gridHeight = 32;
		
		int guiWidth = 640;
		int guiHeight = 320;
		int guiFPS = 30;
		
		
		world = new World(guiWidth, guiHeight, gridWidth, gridHeight, this);
		world.setGridWidth(gridWidth);
		world.setGridHeight(gridHeight);
		
		world.setTheme("loz");
		/*
		 * SETTING WORLD CELL TYPES
		 */
		world.setCellType("pkmn_wall1", "Wall 1", 1, 1, true, Color.blue);
		world.setCellType("pkmn_wall2", "Wall 2", 1, 1, true, Color.green);
		world.setCellType("pkmn_wall3", "Wall 3", 1, 1, true, Color.red);
		world.setCellType("pkmn_floor1", "Floor 1", 1, 1, false, Color.blue);
		world.setCellType("pkmn_floor2", "Floor 2", 1, 1, false, Color.black);
		world.setCellType("pkmn_floor3", "Floor 3", 1, 1, false, Color.black);

	    world.setCellTheme("pkmn_wall1", Simulator.class.getResource("/robotsimulator/themes/pkmn/wall1.png"));
		world.setCellTheme("pkmn_wall2", Simulator.class.getResource("/robotsimulator/themes/pkmn/wall2.png"));
		world.setCellTheme("pkmn_wall3", Simulator.class.getResource("/robotsimulator/themes/pkmn/wall3.png"));
		world.setCellTheme("pkmn_floor1", Simulator.class.getResource("/robotsimulator/themes/pkmn/floor1.png"));
		world.setCellTheme("pkmn_floor2", Simulator.class.getResource("/robotsimulator/themes/pkmn/floor2.png"));
		world.setCellTheme("pkmn_floor3", Simulator.class.getResource("/robotsimulator/themes/pkmn/floor3.png"));
		
		gui = new GUI(guiWidth, guiHeight, guiFPS, this);
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
	
	public static void expLine(String s, BufferedWriter bw)
	{
		try 
		{
			bw.write(s);
			bw.newLine();
		} 
		catch (IOException e) 
		{

		}
	}
	
	public static void expBreak(BufferedWriter bw) 
	{
		try 
		{
			bw.newLine();
		} 
		catch (IOException e) 
		{

		}	
	}
	
	public static void expProp(String k, String v, BufferedWriter bw)
	{
		try 
		{
			bw.write(k + ":" + v);
			bw.newLine();
		} 
		catch (IOException e) 
		{

		}
	}
	
	public static void expProp(String k, int v, BufferedWriter bw)
	{
		try 
		{
			bw.write(k + ":" + v);
			bw.newLine();
		} 
		catch (IOException e) 
		{

		}
	}
	
	public static void expProp(String k, Double v, BufferedWriter bw)
	{
		try 
		{
			bw.write(k + ":" + v);
			bw.newLine();
		} 
		catch (IOException e) 
		{

		}
	}
	
	public static void expProp(String k, boolean v, BufferedWriter bw)
	{
		try 
		{
			bw.write(k + ":" + v);
			bw.newLine();
		} 
		catch (IOException e) 
		{

		}
	}
	
	public void exportWorld(File f) 
	{
		try 
		{
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			robot.export(bw);
			world.export(bw);
			
			bw.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}		
	}
}

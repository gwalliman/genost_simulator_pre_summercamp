package robotsimulator.robot;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import robotsimulator.Simulator;
import robotsimulator.world.Point;
import robotsimulator.worldobject.Block;

public class Robot implements Runnable
{
	private Simulator sim;
	private Block b;
	private double centerX, centerY, angle;
	private volatile Thread robotThread;
	private int delay = 50;
	
	//Forward = f, Backwards = b
	//Left = l, Right = r
	//Strafe Left = h, Strafe Right = i (Hook and slIce)
	//Stop = s
	private char status = 's';
	
	public Robot(Simulator s)
	{
		sim = s;
		centerX = 100;
		centerY = 100;
		
		//Degrees, 0 is direct north, counterclockwise
		angle = 0;

		b = new Block(20, 30, centerX, centerY, angle);
	}

	public Block getBlock() 
	{
		return b;
	}
	
	public double getCenterX() 
	{
		return b.getCenterX();
	}
	
	public double getCenterY() 
	{
		return b.getCenterY();
	}
	
	public double getX0()
	{
		return b.getCenterX() + (b.getHeight() / 2) * Math.cos(b.getRadAngle()) + (b.getWidth() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getY0()
	{
		return b.getCenterY() - (b.getWidth() / 2) * Math.cos(b.getRadAngle()) + (b.getHeight() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getX1()
	{
		return b.getCenterX() + (b.getHeight() / 2) * Math.cos(b.getRadAngle()) - (b.getWidth() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getY1()
	{
		return b.getCenterY() + (b.getWidth() / 2) * Math.cos(b.getRadAngle()) + (b.getHeight() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getX2()
	{
		return b.getCenterX() - (b.getHeight() / 2) * Math.cos(b.getRadAngle()) + (b.getWidth() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getY2()
	{
		return b.getCenterY() - (b.getWidth() / 2) * Math.cos(b.getRadAngle()) - (b.getHeight() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getX3()
	{
		return b.getCenterX() - (b.getHeight() / 2) * Math.cos(b.getRadAngle()) - (b.getWidth() / 2) * Math.sin(b.getRadAngle());
	}
	
	public double getY3()
	{
		return b.getCenterY() + (b.getWidth() / 2) * Math.cos(b.getRadAngle()) - (b.getHeight() / 2) * Math.sin(b.getRadAngle());
	}
	
	public ArrayList<Point> getLine(double x1, double y1, double x2, double y2)
    {
        // X1,Y1 = Robot
        // X2,Y2 = something very far away in a particular direction
        // It needs to be far so we don't stop short of finding an object.

		ArrayList<Point> points = new ArrayList<Point>();
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double step = 1.0 / distance;

        for (double progress = 0; progress <= 1; progress += step)
        {
            int posX = (int)(x1 * (1 - progress) + x2 * (progress));
            int posY = (int)(y1 * (1 - progress) + y2 * (progress));
            
            /*if(posX < 0)
            {
            	posX = 0;
            }
            else if(posX > sim.getWorld().getWidth())
            {
            	posX = sim.getWorld().getWidth();
            }
            
            if(posY < 0)
            {
            	posY = 0;
            }
            else if(posY > sim.getWorld().getHeight())
            {
            	posY = sim.getWorld().getHeight();
            }*/
            
            boolean found = false;

            for(Point p : points)
            {
            	if(p.compare(posX, posY))
            	{
            		found = true;
            		break;
            	}
            }
            
            if(!found)
            {
            	points.add(new Point(posX, posY));
            }
        }
        
        return points;

    }
	
	public void setCenter(int r)
	{
		double oldCenterX = centerX;
		double oldCenterY = centerY;
		
		centerX = b.getCenterX() + (r * Math.cos(b.getRadAngle()));
		centerY = b.getCenterY() + (r * Math.sin(b.getRadAngle()));
		b.setCenter(centerX, centerY);
		
		if(checkRobotCollision())
		{
			centerX = oldCenterX;
			centerY = oldCenterY;
			b.setCenter(centerX, centerY);
		}
	}

	public void setAngle(double d)
	{
		double oldAngle = angle;
		
		b.setAngle(d);
		angle = b.getDegAngle();
		
		if(checkRobotCollision())
		{
			b.setAngle(oldAngle);
			angle = b.getDegAngle();
		}
	}
	
	public boolean checkRobotCollision() 
	{
		Point[][] worldPoints = sim.getWorld().getWorldPoints();
		ArrayList<Point> pfront = getLine(sim.getRobot().getX0(), sim.getRobot().getY0(), sim.getRobot().getX1(), sim.getRobot().getY1());
		ArrayList<Point> pleft = getLine(sim.getRobot().getX0(), sim.getRobot().getY0(), sim.getRobot().getX2(), sim.getRobot().getY2());
		ArrayList<Point> pright  = getLine(sim.getRobot().getX1(), sim.getRobot().getY1(), sim.getRobot().getX3(), sim.getRobot().getY3());
		ArrayList<Point> prear = getLine(sim.getRobot().getX3(), sim.getRobot().getY3(), sim.getRobot().getX2(), sim.getRobot().getY2());
		
		return checkEdgeCollision(pfront, worldPoints) || checkEdgeCollision(pleft, worldPoints) || checkEdgeCollision(pright, worldPoints) || checkEdgeCollision(prear, worldPoints);
	}
	
	private boolean checkEdgeCollision(ArrayList<Point> points, Point[][] worldPoints) 
	{
		for(Point p : points)
		{
			if(p.getX() < 0 || p.getX() >= sim.getWorld().getWidth() || p.getY() < 0 || p.getY() >= sim.getWorld().getHeight())
            {
            	return true;
            }
            
			if(worldPoints[p.getX()][p.getY()].isOccupied())
			{
				return true;
			}
		}
		return false;
	}

	public void drive(char d)
	{
		if(robotThread == null)
		{
			status = d;
			robotThread = new Thread(this);
			robotThread.start();
		}
	}
	
	public void turn(char d)
	{
		if(robotThread == null)
		{
			status = d;
			robotThread = new Thread(this);
			robotThread.start();
		}
	}
	
	public void stop()
	{
		status = 's';
		robotThread = null;
	}

	public void run() 
	{
		Thread me = Thread.currentThread();
		while(robotThread == me)
		{
			long beforeTime, timeDiff, sleep;
	        beforeTime = System.currentTimeMillis();
			
			switch(status)
			{
				case 'f':
					setCenter(1);
				break;
				case 'b':
					setCenter(-1);
				break;
				case 'l':
					setAngle(angle - 1);
				break;
				case 'r':
					setAngle(angle + 1);
				break;
			}
			
			timeDiff = System.currentTimeMillis() - beforeTime;
	        sleep = delay - timeDiff;
	        
	        if(sleep == 0) sleep = 2;

	        try 
			{
				Thread.sleep(sleep);
			} 
			catch (InterruptedException e) 
			{
			}
			
            beforeTime = System.currentTimeMillis();
		}
	}
}

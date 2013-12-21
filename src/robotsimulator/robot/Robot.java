package robotsimulator.robot;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import robotsimulator.Simulator;
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
		angle = 0;

		b = new Block(20, 30, centerX, centerY, angle);
	}

	public Block getBlock() 
	{
		return b;
	}
	
	public void setCenter(int r)
	{
		centerX = b.getCenterX() + (r * Math.cos(b.getRadAngle() - (Math.PI / 2)));
		centerY = b.getCenterY() + (r * Math.sin(b.getRadAngle() - (Math.PI / 2)));
		b.setCenter(centerX, centerY);
	}
	
	public void setAngle(double d)
	{
		b.setAngle(d);
		angle = b.getDegAngle();
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

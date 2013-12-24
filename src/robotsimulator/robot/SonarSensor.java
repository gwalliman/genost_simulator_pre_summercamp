package robotsimulator.robot;

import java.awt.Label;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;

import robotsimulator.Simulator;
import robotsimulator.world.Point;
import robotsimulator.world.World;

public class SonarSensor implements Runnable
{
	private Simulator sim;
	private Robot r;
	private volatile Thread robotThread;
    private int delay = 50;
	
	//'l' if line
	//'c' if cone
	private char type;
	
	private String label;
	private Label t;
	
	private double x0, y0, x1, y1;
	
	//The maximum distance that the sensor can detect items at
	private int length;
	
	//The angle in degrees (with respect to the robot) that the sensor is pointing.
	//0 is right, 90 is forwards, etc.
	//private int angle;
	
	//The field of view. If 60, then the sensor can detect items +- 30 degrees off its angle
	private int fov;
	
	//private Line2D line;
	private Shape shape;
	
	public SonarSensor(Simulator s, Robot rob, String n, double x, double y, int l, int a, char t)
	{
		sim = s;
		r = rob;
		x0 = x;
		y0 = y;
		length = l;
		label = n;

		x1 = getEndpointX(a);
		y1 = getEndpointY(a);
		
		//angle = a;
		type = t;
		
		Line2D line = new Line2D.Double(x0, y0, x1, y1);
		shape = line;
		
		robotThread = new Thread(this);
		robotThread.start();
	}
	
	/*public Line2D getLine()
	{
		return line;
	}*/
	
	public String getLabel()
	{
		return label;
	}
	
	public double getX0()
	{
		return x0;
	}
	
	public double getY0()
	{
		return y0;
	}
	
	public double getX1()
	{
		return x1;
	}
	
	public double getY1()
	{
		return y1;
	}
	
	public Shape getShape()
	{
		return shape;
	}
	
	public double getEndpointX(int a)
	{
		double x = Math.cos(Math.toRadians(a)) * length;
		return x + x0;
	}
	
	public double getEndpointY(int a)
	{
		double y = Math.sin(Math.toRadians(a)) * length;
		return y + y0;
	}
	
	public void translate(double x, double y)
	{
		/*x += x0;
		y += y0;
		
		line.setLine(x, y, getEndpointX(), getEndpointY());
		shape = line;*/
		AffineTransform at = AffineTransform.getTranslateInstance(x, y);
		shape = at.createTransformedShape(shape);
		setEndpoints(shape);	
	}
	
	public void rotate(double a)
	{
		AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(a), r.getCenterX(), r.getCenterY());
		shape = at.createTransformedShape(shape);
		setEndpoints(shape);
	}
	
	public double getSensorValue() 
	{
		Point[][] worldPoints = sim.getWorld().getWorldPoints();
		ArrayList<Point> points = World.getLine(x0, y0, x1, y1);
		int x = -1;
		int y = -1;
		
		for(Point p : points)
		{
			if(p.getX() < 0 || p.getX() >= sim.getWorld().getWidth() || p.getY() < 0 || p.getY() >= sim.getWorld().getHeight())
			{
				if(p.getX() < 0)
				{
					x = 0;
				}
				else if(p.getX() >= sim.getWorld().getWidth())
				{
					x = sim.getWorld().getWidth() - 1;
				}
				else
				{
					x = p.getX();
				}
            
				if(p.getY() < 0)
				{
					y = 0;
				}
				else if(p.getY() >= sim.getWorld().getHeight())
				{
					y = sim.getWorld().getHeight() - 1;
				}
				else
				{
					y = p.getY();
				}
				break;
			}
            
			if(worldPoints[p.getX()][p.getY()].isOccupied())
			{
				x = p.getX();
				y = p.getY();
				break;
			}
		}
		
		if(x == -1 || y == -1)
		{
			x = length;
			y = length;
		}
		
		return Math.hypot(x - x0, y - y0);
	}
	
	private void setEndpoints(Shape shape)
	{
		PathIterator pi = shape.getPathIterator(null);
		
		double[] coords = new double[6];
		pi.currentSegment(coords);
		x0 = coords[0];
		y0 = coords[1];
		pi.next();
		pi.currentSegment(coords);
		x1 = coords[0];
		y1 = coords[1];
	}
	
	public void setTextField(Label text)
	{
		t = text;
	}

	public void run() 
	{
		while(true)
		{
			long beforeTime, timeDiff, sleep;
	        beforeTime = System.currentTimeMillis();
	        
	        if(t != null)
	        {
	        	t.setText(Double.toString(Double.parseDouble(new DecimalFormat("#.##").format(getSensorValue()))));
	        }
	        
			timeDiff = System.currentTimeMillis() - beforeTime;
			sleep = delay - timeDiff;
	         
	        if(sleep <= 0) sleep = 2;
			
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

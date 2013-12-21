package robotsimulator.worldobject;

import java.awt.geom.Rectangle2D;

public class Block 
{
	private double width, height, centerX, centerY, angle;
	private Rectangle2D rect;
	
	public Block(int w, int h, double centerX2, double centerY2, double angle2)
	{
		width = w;
		height = h;
		centerX = centerX2;
		centerY = centerY2;
		angle = angle2;
		
		rect = new Rectangle2D.Float((float)getX0(), (float)getY0(), (float)w, (float)h);
	}
	
	public double getWidth()
	{
		return width;
	}
	
	public double getHeight()
	{
		return height;
	}
	
	public double getCenterX()
	{
		return centerX;
	}
	
	public double getCenterY()
	{
		return centerY;
	}
	
	public double getDegAngle()
	{
		return angle;
	}
	
	public double getRadAngle()
	{
		return Math.toRadians(angle);
	}
	
	public void setCenterX(double c)
	{
		centerX = c;
		rect.setRect(getX0(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	
	public void setCenterY(double c)
	{
		centerY = c;
		rect.setRect(rect.getX(), getY0(), rect.getWidth(), rect.getHeight());
	}
	
	public void setCenter(double x, double y) 
	{
		centerX = x;
		centerY = y;
		
		rect.setRect(getX0(), getY0(), rect.getWidth(), rect.getHeight());
	}
	
	public void setAngle(double c)
	{
		if(c > 360)
		{
			setAngle(c - 360);
		}
		else if(c < 0)
		{
			setAngle(c + 360);
		}
		else
		{
			angle = c;
		}
	}
	
	public double getX0()
	{
		return centerX - (width / 2);
	}
	
	public double getY0()
	{
		return centerY - (height / 2);
	}
	
	public Rectangle2D getRect()
	{
		return rect;
	}
}

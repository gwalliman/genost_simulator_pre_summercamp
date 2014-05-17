package robotsimulator.world;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import robotsimulator.RobotSimulator;

public class CellTheme 
{
	String id;
	BufferedImage image;
	int width, height;
	
	public CellTheme(String i, InputStream in)
	{
		id = i;
		try 
		{
			image = ImageIO.read(in);
			width = image.getWidth(null);
			height = image.getHeight(null);
			
		}
		catch(IOException e)
		{
			RobotSimulator.println("Cannot find image.");
		}
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}

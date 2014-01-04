package robotsimulator.world;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CellTheme 
{
	String id;
	BufferedImage image;
	int width, height;
	
	public CellTheme(String i, String imgsrc)
	{
		id = i;
		try 
		{
			image = ImageIO.read(new File(imgsrc));
			width = image.getWidth(null);
			height = image.getHeight(null);
			
		}
		catch(IOException e)
		{
			System.out.println("Cannot find image at " + imgsrc);
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

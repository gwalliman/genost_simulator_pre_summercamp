package robotsimulator.gui;

import java.awt.Canvas;
import javax.swing.JFrame;

public class GUI extends JFrame 
{
	public GUI(int width, int height)
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		
		Canvas c = new Canvas();
		getContentPane().add(c);
		
		setVisible(true);
	}
}

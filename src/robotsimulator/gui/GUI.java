package robotsimulator.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;

import robotsimulator.Simulator;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

public class GUI extends Canvas implements MouseListener
{
	private Simulator sim;
	private int width;
	private int height;
	
	public GUI(int w, int h, Simulator s)
	{
		sim = s;
		width = w;
		height = h;
		
		this.addMouseListener(this);
		this.setBackground(Color.white);
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.getContentPane().add(this);
		frame.setVisible(true);
	}
	
	public void paint(Graphics graphics)
	{
		graphics.drawRect(0, 0, width, height);
		ArrayList<Block> blocks = sim.getWorld().getBlocks();
		for(Block b : blocks)
		{
			graphics.setColor(Color.blue);
			graphics.fillRect(b.getX0(), b.getY0(), b.getWidth(), b.getHeight());
		}
	}

	public void mouseClicked(MouseEvent click) 
	{

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent click) 
	{
		System.out.println("Click");
		System.out.println(click.getX() + " " + click.getY());
		//sim.addBlock(20, 20, click.getX(), click.getY());
		sim.getWorld().toggleCell(click.getX(), click.getY());
		repaint();		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

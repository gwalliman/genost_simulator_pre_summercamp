package robotsimulator.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.worldobject.Block;

public class Stage extends JPanel implements MouseListener, Runnable
{
	private Simulator sim;
	private Thread animator;
	private int width, height, fps;

	public Stage(int w, int h, int f, Simulator s)
	{
		sim = s;
		width = w;
		height = h;
		fps = f;
		this.addMouseListener(this);
		
		setDoubleBuffered(true);
		setSize(w, h);
		setBackground(Color.white);
	}
	
	public void addNotify() 
	{
        super.addNotify();
        animator = new Thread(this);
        animator.start();
    }
	
	public void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		
		Graphics2D g = (Graphics2D) graphics;
		
		g.drawRect(0, 0, width, height);
		
		ArrayList<Block> blocks = sim.getWorld().getBlocks();
		for(Block b : blocks)
		{
			paintBlock(g, b);
		}
		
		paintBlock(g, sim.getRobot().getBlock());
	}
	
	private void paintBlock(Graphics2D g, Block b)
	{
		g.setColor(Color.blue);
		AffineTransform at = AffineTransform.getRotateInstance(b.getRadAngle(), b.getCenterX(), b.getCenterY());  
		g.fill(at.createTransformedShape(b.getRect()));
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
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
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() 
	{
		while(true)
		{
			long beforeTime, timeDiff, sleep;
	        beforeTime = System.currentTimeMillis();
	        
			repaint();
			
			timeDiff = System.currentTimeMillis() - beforeTime;
	        sleep = (1000 / fps) - timeDiff;
	         
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

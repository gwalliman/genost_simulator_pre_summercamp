package robotsimulator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import robotsimulator.Simulator;
import robotsimulator.robot.SonarSensor;
import robotsimulator.world.CellTheme;
import robotsimulator.world.CellType;
import robotsimulator.world.Point;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

@SuppressWarnings("serial")
public class Stage extends JPanel implements MouseListener, Runnable, Scrollable
{
	private Simulator sim;
	private Thread animator;
	//@SuppressWarnings("unused")
	private int width, height, fps;
	//Whether or not we can edit the maze in this view
	private boolean editable;

	public Stage(int w, int h, int f, Simulator s)
	{
		sim = s;
		width = w;
		height = h;
		fps = f;
		editable = false;
		this.addMouseListener(this);
		
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(w, h));
		setSize(w, h);
		setBackground(Color.white);
	}
	
	public void allowEditing()
	{
		editable = true;
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
		
		g.draw(sim.getWorld().getBoundary());
		
		ArrayList<Block> blocks = sim.getWorld().getBlocks();
		for(Block b : blocks)
		{
			paintBlock(g, b, b.getColor());
		}
		
		paintBlock(g, sim.getRobot().getBlock(), Color.green);
		
		//paintRobotEdges(g);
		//paintSonarSensors(g);
		
		/*Point[][] worldPoints = sim.getWorld().getWorldPoints();
		for(int x = 0; x < worldPoints.length; x++)
		{
			for(int y = 0; y < worldPoints[x].length; y++)
			{
				if(worldPoints[x][y].isOccupied())
				{
					g.fill(new Ellipse2D.Double(x - (5 / 2), y - (5 / 2), 5, 5));
				}
			}
		}*/
	}

	private void paintBlock(Graphics2D g, Block b, Color c)
	{
		g.setColor(c);
		AffineTransform at = AffineTransform.getRotateInstance(b.getRadAngle() - (Math.PI / 2), b.getCenterX(), b.getCenterY());  
		g.fill(at.createTransformedShape(b.getRect()));
		
		CellType ct = b.getCellType();
		if(ct != null)
		{
			String cellTypeID = ct.getID();
			if(sim.getWorld().getCellThemes().containsKey(cellTypeID))
			{
				CellTheme cellTheme = sim.getWorld().getCellThemes().get(cellTypeID);
				g.drawImage(cellTheme.getImage(), (int)b.getX0(), (int)b.getY0(), (int)b.getX3(), (int)b.getY3(), cellTheme.getWidth(), cellTheme.getHeight(), 0, 0, null);
			}
		}
	}
	
	//@SuppressWarnings("unused")
	private void paintRobotEdges(Graphics2D g) 
	{
		g.setColor(Color.red);

		ArrayList<Point> points = World.getLine(sim.getRobot().getX0(), sim.getRobot().getY0(), sim.getRobot().getX1(), sim.getRobot().getY1());
		for(Point p : points)
		{
			g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
		}
		
		points = World.getLine(sim.getRobot().getX0(), sim.getRobot().getY0(), sim.getRobot().getX2(), sim.getRobot().getY2());
		for(Point p : points)
		{
			g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
		}
		
		points = World.getLine(sim.getRobot().getX1(), sim.getRobot().getY1(), sim.getRobot().getX3(), sim.getRobot().getY3());
		for(Point p : points)
		{
			g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
		}
		
		points = World.getLine(sim.getRobot().getX3(), sim.getRobot().getY3(), sim.getRobot().getX2(), sim.getRobot().getY2());
		for(Point p : points)
		{
			g.fill(new Ellipse2D.Double(p.getX() - (5 / 2), p.getY() - (5 / 2), 5, 5));
		}
		
		g.setColor(Color.black);

		g.fill(new Ellipse2D.Double(sim.getRobot().getCenterX() - (5 / 2), sim.getRobot().getCenterY() - (5 / 2), 5, 5));
		g.fill(new Ellipse2D.Double(sim.getRobot().getX0() - (5 / 2), sim.getRobot().getY0() - (5 / 2), 5, 5));
		g.fill(new Ellipse2D.Double(sim.getRobot().getX1() - (5 / 2), sim.getRobot().getY1() - (5 / 2), 5, 5));
		g.fill(new Ellipse2D.Double(sim.getRobot().getX2() - (5 / 2), sim.getRobot().getY2() - (5 / 2), 5, 5));
		g.fill(new Ellipse2D.Double(sim.getRobot().getX3() - (5 / 2), sim.getRobot().getY3() - (5 / 2), 5, 5));		
	}
	
	//@SuppressWarnings("unused")
	private void paintSonarSensors(Graphics2D g)
	{
		for(SonarSensor s : sim.getRobot().getSonarSensors())
		{
			if(s.getType() == 'l')
			{
				g.draw(s.getShape());
				g.fill(new Ellipse2D.Double(s.getX0() - (5 / 2), s.getY0() - (5 / 2), 5, 5));
				g.fill(new Ellipse2D.Double(s.getX1() - (5 / 2), s.getY1() - (5 / 2), 5, 5));
			}
			else if(s.getType() == 'c')
			{
				g.draw(s.getShape1());
				g.fill(new Ellipse2D.Double(s.getX0() - (5 / 2), s.getY0() - (5 / 2), 5, 5));
				g.fill(new Ellipse2D.Double(s.getX1() - (5 / 2), s.getY1() - (5 / 2), 5, 5));
				
				g.draw(s.getShape2());
				g.fill(new Ellipse2D.Double(s.getX2() - (5 / 2), s.getY2() - (5 / 2), 5, 5));
			}
		}
	}

	public void mousePressed(MouseEvent click) 
	{
		//Give focus back to the main applet in order for keyboard controls to work
		MainApplet.m_instance.getFocus();
		if (editable)
		{
			//sim.addBlock(20, 20, click.getX(), click.getY());
			sim.getWorld().toggleCell(click.getX(), click.getY());
			repaint();			
		}
	}
	
	public void run() 
	{
		while(true)
		{
			long beforeTime, timeDiff, sleep;
	        beforeTime = System.currentTimeMillis();
	        
			repaint();
			
			timeDiff = System.currentTimeMillis() - beforeTime;
	        sleep = (1000 / fps) - timeDiff;
	         
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
	
	public void mouseClicked(MouseEvent arg0) { }

	public void mouseEntered(MouseEvent arg0) {	}

	public void mouseExited(MouseEvent arg0) { }
	
	public void mouseReleased(MouseEvent arg0) { }

	//Needed to allow for variable size maps and scrolling
	@Override
	public Dimension getPreferredScrollableViewportSize() 
	{
		return new Dimension(520, 400);
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) 
	{
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() 
	{
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) 
	{
		return 0;
	}
	

}

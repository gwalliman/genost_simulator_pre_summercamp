package robotsimulator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import robotsimulator.MainEntry;
import robotsimulator.Simulator;
import robotsimulator.robot.SonarSensor;
import robotsimulator.world.CellTheme;
import robotsimulator.world.CellType;
import robotsimulator.world.Point;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

@SuppressWarnings("serial")
//In charge of rendering the stage, and the robot on top of it
public class Stage extends JPanel implements MouseListener, Runnable, Scrollable
{
	private Simulator sim;
	private Thread animator;
	//@SuppressWarnings("unused")
	private int width, height, fps;
	//Whether or not we can edit the maze in this view
	private boolean editable = false;

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
	
	public void disableEditing()
	{
		editable = false;
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
		
		//paintBlock(g, sim.getRobot().getBlock(), Color.RED);
		paintRobot(g, sim.getRobot().getBlock(), Color.RED);
		
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
	
	private void paintRobot(Graphics2D g, Block b, Color c)
	{
		//g.setColor(c);
		//AffineTransform at = AffineTransform.getRotateInstance(b.getRadAngle() - (Math.PI / 2), b.getCenterX(), b.getCenterY());  
		//g.fill(at.createTransformedShape(b.getRect()));
		
		AffineTransform at1 = new AffineTransform();
		AffineTransform at2 = new AffineTransform();
		
		//Need to adjust this to hit the exact center of 
		double sWidth = MainEntry.robotSprite.getIconWidth();
		double sHeight = MainEntry.robotSprite.getIconHeight();
		double tx = b.getCenterX() - sWidth / 2;
		double ty = b.getCenterY() - sHeight / 2;
		
		//Create the translation
		at1.translate(tx, ty);
		//Create the rotation
		at2.rotate(b.getRadAngle() - (Math.PI / 2), sWidth / 2, sHeight / 2);
		
		//Apply the rotation to the translation
		at1.concatenate(at2);
		
		//Draw robot sprite
		g.drawImage(MainEntry.robotSprite.getImage(), at1, this);
		
		
		//Draw debug box
		//g.setColor(c);
		//AffineTransform at = AffineTransform.getRotateInstance(b.getRadAngle() - (Math.PI / 2), b.getCenterX(), b.getCenterY());  
		//g.fill(at.createTransformedShape(b.getRect()));
		
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
			g.setColor(Color.black);
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
				System.out.println("Interrupted Exception");
			}
			
            beforeTime = System.currentTimeMillis();
		}
	}
	
	public void mouseClicked(MouseEvent arg0) { }

	public void mouseEntered(MouseEvent arg0) {	}

	public void mouseExited(MouseEvent arg0) { }
	
	public void mouseReleased(MouseEvent arg0) { }

	//Creates a standard scrollable stage
	public static JPanel createStagePanel(int mazeWidth, int mazeHeight, int fps, Simulator sim, boolean editable)
	{
		JPanel stagePanel = new JPanel();
		stagePanel.setSize(520, 400);
		Stage simStage = new Stage(mazeWidth * 2, mazeHeight * 2, fps, sim);
		if (editable)
			simStage.allowEditing();

		JScrollPane stageScroll = new JScrollPane(simStage);
		stageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		stageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		stageScroll.setSize(mazeWidth, mazeHeight);
		
		stagePanel.add(stageScroll);
		return stagePanel;
	}
		
	
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

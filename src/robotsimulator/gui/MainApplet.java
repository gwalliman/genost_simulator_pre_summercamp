package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import robotsimulator.MainEntry;
import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;

//This needs to duplicate the functionality of GUI
public class MainApplet extends JApplet implements ChangeListener {

	public static MainApplet m_instance;
	
	//GUI variables
	private int width = 800;
	private int height = 600;
	private int fps = 60;
	
	//Structural variables
	private JTabbedPane tabPane;
	
	private Simulator sim;
	public SimulatorPanel simPanel;
	private MazeBuilderPanel mazePanel;
	
	//IO variables
	public File codeFile;
	public File configFile;
	public File mapFile;
	
	//If true, this is a student build, and we should disable the maze builder, etc.
	private static final boolean studentBuild = true;
		
	//This needs to be the main entry point into the program
	public void init()
	{
		//Create a static reference to the applet if none exists
		if (m_instance == null)
			m_instance = this;
		
		sim = new Simulator(this);
		setKeyBindings();
			
		try 
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					buildGUI();
				}
			});
		}
		catch (Exception e)
		{
			System.err.println("couldn't construct the GUI");
			e.printStackTrace();
		}
		

		//Load some resources
		Image img = null;
		try
		{
			//img = ImageIO.read(new File(MainEntry.resourcePath + "/magictree.png"));
			img = ImageIO.read(new File(MainEntry.resourcePath + "/robot.png"));
			MainEntry.robotSprite = img;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	private void buildGUI()
	{
		//setSize(new Dimension(800, 600));
		setSize(new Dimension(width, height));
		
		tabPane = new JTabbedPane();
		tabPane.setSize(width, height);
		//Remove the keyboard shortcuts-- 
		//Want to control robot with arrow keys, not switch tabs
		tabPane.setActionMap(null);
		tabPane.addChangeListener(this);
		
		simPanel = new SimulatorPanel(width, height, fps, sim, this);
		tabPane.addTab("Simulator", simPanel);
		
		mazePanel = new MazeBuilderPanel(fps, sim, this);
		if (!studentBuild)
			tabPane.addTab("Maze Builder", mazePanel);
				
		add (tabPane);
	}
	
	//Returns focus back to the GUI and re-enables keyboard controls for the robot
	public void getFocus()
	{
		tabPane.grabFocus();
	}
	
	//Methods copied directly from GUI.java
	public int getFPS()
	{
		return fps;
	}

	private void setKeyBindings()
	{
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "debug");
		
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "stop");
		
		getRootPane().getActionMap().put("debug", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("[DEBUG]");
				Robot b = sim.getRobot();
				for (SonarSensor s : b.getSonarSensors())
				{
					System.out.println("[" + s.getLabel() + "]: " + s.getConeSensorValue());
				}
				/*
				System.out.println("[DEBUG2]");
				for (SonarSensor s2 : simPanel.sonars)
				{
					System.out.println("[" + s2.getLabel() + "]: " + s2.getConeSensorValue());
				}
				*/
				debug_refreshSensorLabels();
			}
		});
		
		getRootPane().getActionMap().put("up", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().drive('f');
	    	}
	    });
	    
		getRootPane().getActionMap().put("down", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().drive('b');
	    	}
	    });
	    
		getRootPane().getActionMap().put("left", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().turn('l');
	    	}
	    });
	    
		getRootPane().getActionMap().put("right", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().turn('r');
	    	}
	    });
	    
		getRootPane().getActionMap().put("stop", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView() && !isExecuting())
	    			sim.getRobot().stop();
	    	}
	    });
	}
	
	private void debug_refreshSensorLabels()
	{
		System.out.println("[RefreshSensors]");
		//simPanel;
	}
	
	//Returns true if the simulator tab is currently showing
	private boolean inSimulatorView()
	{
		return (tabPane.getSelectedIndex() == 0);
	}
	
	//Returns true if the maze builder tab is currently showing
	private boolean inMazeView()
	{
		return (tabPane.getSelectedIndex() == 1);
	}
	
	//Returns true if the simulator tab is currently running code
	private boolean isExecuting()
	{
		return sim.running;
	}
		
	@Override
	//Fires whenever a tab is changed. Used to save/close/stop execution when changing focus
	public void stateChanged(ChangeEvent e) 
	{
		System.out.println("Changed tab.");
		
		if (inSimulatorView())
		{
			System.out.println("In sim view");
			simPanel.resumeSensorThread();
		}
		if (inMazeView())
		{
			System.out.println("In maze view");
			//Stop execution
			simPanel.stopExecution();
			simPanel.stopSensorThread();
			//sim.stop();
		}
	}

	
}

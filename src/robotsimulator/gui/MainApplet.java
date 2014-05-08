package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;

/*
 * Main container class. Holds simulator panel and mazebuilder panel in tabbed panes.
 * Handles keybinds for the robot, and switching between tabs. 
*/
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
	public MazeBuilderPanel mazePanel;
	
	//IO variables
	public File codeFile;
	public File configFile;
	public File mapFile;
	
	//If true, this is a student build, and we should disable the maze builder, arrow keys, etc.
	public static final boolean studentBuild = false;
    
    //Robot image
	public static ImageIcon robotSprite;
		
	public void init()
	{
		//Create a static reference to the applet if none exists
		if (m_instance == null)
			m_instance = this;
		
		sim = new Simulator(this);
        
		if (!studentBuild)
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
			System.err.println("[ERROR]: Couldn't construct the GUI.");
			e.printStackTrace();
		}
		

		ClassLoader cl = this.getClass().getClassLoader();
		loadRobotSprite("robot.png", cl);		
	}
	
	public static void loadRobotSprite(String filename, ClassLoader cl)
	{
		//Load the sprite from the resources folder in the jar
		try
		{
			robotSprite = new ImageIcon(cl.getResource("Resources/" + filename));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void buildGUI()
	{
		setSize(new Dimension(width, height));
		
		tabPane = new JTabbedPane();
		tabPane.setSize(width, height);
        
		//Remove the default keyboard shortcuts for tabs-- conflicts with robot manual control
		tabPane.setActionMap(null);
		tabPane.addChangeListener(this);
		
		simPanel = new SimulatorPanel(width, height, fps, sim, this);
        tabPane.addTab("Simulator", simPanel);
		
		mazePanel = new MazeBuilderPanel(fps, sim, this);
        //Add in the maze builder tab if we're not using a student build
		if (!studentBuild)
			tabPane.addTab("Maze Builder", mazePanel);
				
		add(tabPane);
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

    //Defines the keyboard shortcuts for driving the robot. 
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
                //Debug command-- change this for easy on-demand console printing, debugging, etc.
				RobotSimulator.println("[DEBUG]");
				Robot b = sim.getRobot();
				for (SonarSensor s : b.getSonarSensors())
				{
					RobotSimulator.println("[" + s.getLabel() + "]: " + s.getConeSensorValue());
				}
			}
		});
        
        /*
         * The following all allow manual driving of the robot
         */
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
	//TODO: Reinitialize robot sensor display when switching back from builder view
	public void stateChanged(ChangeEvent e) 
	{
		if (inSimulatorView())
		{
			simPanel.resumeSensorThread();
		}
		if (inMazeView())
		{
			//Stop execution
			simPanel.stopExecution();
			simPanel.stopSensorThread();
			//sim.stop();
		}
	}	
}

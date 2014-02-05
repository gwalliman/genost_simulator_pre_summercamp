package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import robotsimulator.Simulator;

//This needs to duplicate the functionality of GUI
public class MainApplet extends JApplet {

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
		
		simPanel = new SimulatorPanel(width, height, fps, sim, this);
		tabPane.addTab("Simulator", simPanel);
		
		mazePanel = new MazeBuilderPanel();
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
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "stop");
		getRootPane().getActionMap().put("up", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView())
	    			sim.getRobot().drive('f');
	    	}
	    });
	    
		getRootPane().getActionMap().put("down", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView())
	    			sim.getRobot().drive('b');
	    	}
	    });
	    
		getRootPane().getActionMap().put("left", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView())
	    			sim.getRobot().turn('l');
	    	}
	    });
	    
		getRootPane().getActionMap().put("right", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView())
	    			sim.getRobot().turn('r');
	    	}
	    });
	    
		getRootPane().getActionMap().put("stop", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		if (inSimulatorView())
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
	
	//Listener that fires whenever a tab is changed. Used to save/close/stop execution when changing focus
	private class tabListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent arg0) 
		{
			//TODO: Add code to detect the old tab and take action accordingly
			//e.g. stop simulation/interpreter when switching to maze view
		}
	}

	
}

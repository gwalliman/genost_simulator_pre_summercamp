package robotsimulator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import robotsimulator.Simulator;

//Used to organize simulator components. Held by the main applet class in a JTabbedPane
public class SimulatorPanel extends JPanel {
	
	//Components
	//ButtonGrid
	private JButton openCodeBtn;			//Button for loading code from a file
	private JLabel codeNameLbl;				//Label to display loaded code file name
	private JButton openLoadoutBtn;			//Button for loading robot loadouts from a file
	private JLabel loadoutNameLbl;			//Label to display loaded loadout file name
	private JButton openMazeBtn;			//Button for loading maze from a file
	private JLabel mazeNameLbl;				//Label to display loaded maze file name
	private JButton runBtn;					//Button to begin executing the simulation
	private JButton stopBtn;				//Button to stop executing the simulation
	
	//Right Panel
	private JTextArea outputTextArea;		//Holds output from running code, errors, etc.
	private JTextArea sensorOutputArea;		//Refreshed with sensor data
	
	//Variables
	private Simulator sim;
	private int width, height, fps;
	
	
	public SimulatorPanel(int w, int h, int f, Simulator s)
	{
		
		width = w;
		height = h;
		fps = f;
		sim = s;
		
		JPanel simPane = new JPanel(new GridBagLayout());		
		//Controls size of left side-- input buttons, stage, etc. 3/4 of panel
		GridBagConstraints leftSideConstraints = new GridBagConstraints();
		leftSideConstraints.gridx = 0;
		leftSideConstraints.gridy = 0;
		leftSideConstraints.gridwidth = 3;
		
		//Controls size of right side-- status, output, sensor data, etc. 1/4 of panel
		GridBagConstraints rightSideConstraints = new GridBagConstraints();
		rightSideConstraints.gridx = GridBagConstraints.RELATIVE;
		rightSideConstraints.gridy = 0;
		rightSideConstraints.gridwidth = 1;
	
		JPanel leftPanel = createLeftPanel(w, h, f, s);
		simPane.add(leftPanel, leftSideConstraints);
		
		JPanel rightPanel = createRightPanel(w, h, f, s);
		simPane.add(rightPanel, rightSideConstraints);
		
	}
	
	//Builds the left side of the window-- input buttons, stage, etc.
	public JPanel createLeftPanel(int w, int h, int f, Simulator s)
	{
		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints topConstraints = new GridBagConstraints();
		topConstraints.gridx = 0;
		topConstraints.gridy = 0;
		topConstraints.gridheight = 1;
		//Create button grid panel and add it with these constraints
		leftPanel.add(createButtonGridPanel(w, h, f, s), topConstraints);
		
		GridBagConstraints bottomConstraints = new GridBagConstraints();
		bottomConstraints.gridx = 0;
		bottomConstraints.gridy = GridBagConstraints.RELATIVE;
		bottomConstraints.gridheight = 2;
		//Create stage and add it with these constraints
		leftPanel.add(new Stage(w, h, f, s), bottomConstraints);
		
		return leftPanel;
	}
	
	public JPanel createButtonGridPanel(int w, int h, int f, Simulator s)
	{
		//define 4x2 grid
		JPanel bGridPanel = new JPanel(new GridLayout(2, 4));
		//Add buttons and labels to it as needed
		openCodeBtn = new JButton("Load Program");
		bGridPanel.add(openCodeBtn);
		
		codeNameLbl = new JLabel("Current program: ");
		bGridPanel.add(codeNameLbl);
		
		openLoadoutBtn = new JButton("Load Config");
		bGridPanel.add(openLoadoutBtn);
		
		loadoutNameLbl = new JLabel("Current Config: ");
		bGridPanel.add(loadoutNameLbl);
		
		openMazeBtn = new JButton("Load Maze");
		bGridPanel.add(openMazeBtn);
		
		mazeNameLbl = new JLabel("Current Maze: ");
		bGridPanel.add(mazeNameLbl);
		
		runBtn = new JButton("Execute!");
		bGridPanel.add(runBtn);
		
		stopBtn = new JButton("Stop");
		bGridPanel.add(stopBtn);
		
		return bGridPanel;
	}
	
	//Builds the right side of the window-- status, output, sensor data, etc.
	public JPanel createRightPanel(int w, int h, int f, Simulator s)
	{
		JPanel rightPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridheight = 1;
		//Add 'robot status' label
		JLabel statusLbl = new JLabel("Robot Status");
		rightPanel.add(statusLbl, c);
		
		c.gridheight = 1;
		//add 'running/not running' label
		JLabel runningLbl = new JLabel("Not Running");
		rightPanel.add(runningLbl, c);
		
		c.gridheight = 2;
		//add output textarea
		outputTextArea = new JTextArea();
		rightPanel.add(outputTextArea, c);
		
		c.gridheight = 1;
		//add 'sensor data' label
		JLabel sensorLbl = new JLabel("Sensor Data");
		rightPanel.add(sensorLbl, c);
		
		c.gridheight = GridBagConstraints.RELATIVE;
		//add sensor data textarea/panel 
		sensorOutputArea = new JTextArea();
		rightPanel.add(sensorOutputArea, c);
		
		return rightPanel;
	}
	
	
}

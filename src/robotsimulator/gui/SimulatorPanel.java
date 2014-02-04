package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

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
		leftSideConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		leftSideConstraints.insets = new Insets(4, 4, 4, 4);
		
		//Controls size of right side-- status, output, sensor data, etc. 1/4 of panel
		GridBagConstraints rightSideConstraints = new GridBagConstraints();
		rightSideConstraints.gridx = GridBagConstraints.RELATIVE;
		//rightSideConstraints.gridx = 1;
		rightSideConstraints.gridy = 0;
		rightSideConstraints.gridwidth = 1;
		rightSideConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		rightSideConstraints.insets = new Insets(4, 4, 4, 4);
		
		JPanel leftPanel = createLeftPanel(w, h, f, s);
		simPane.add(leftPanel, leftSideConstraints);
		
		JPanel rightPanel = createRightPanel(w, h, f, s);
		simPane.add(rightPanel, rightSideConstraints);

		add(simPane);
		
	}
	
	//Builds the left side of the window-- input buttons, stage, etc.
	public JPanel createLeftPanel(int w, int h, int f, Simulator s)
	{
		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints topConstraints = new GridBagConstraints();
		topConstraints.gridx = 0;
		topConstraints.gridy = 0;
		topConstraints.gridheight = 1;
		topConstraints.insets = new Insets(4, 4, 4, 4);
		//Create button grid panel and add it with these constraints
		leftPanel.add(createButtonGridPanel(w, h, f, s), topConstraints);
		
		GridBagConstraints bottomConstraints = new GridBagConstraints();
		bottomConstraints.gridx = 0;
		bottomConstraints.gridy = GridBagConstraints.RELATIVE;
		bottomConstraints.gridheight = 2;
		bottomConstraints.insets = new Insets(4, 4, 4, 4);
		//Create stage and add it with these constraints
		//leftPanel.add(new Stage(w, h, f, s), bottomConstraints);
		//For now, create a text area to fill in the space
		JTextArea tempStage = new JTextArea(27, 50);
		leftPanel.add(tempStage, bottomConstraints);
		
		return leftPanel;
	}
	
	public JPanel createButtonGridPanel(int w, int h, int f, Simulator s)
	{
		//define 4x2 grid
		GridLayout g = new GridLayout(4, 2, 20, 4);
		JPanel bGridPanel = new JPanel(g);
		bGridPanel.setSize(new Dimension(600, 100));
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
		rightPanel.setSize(200, 600);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.PAGE_START;
		
		c.gridheight = 1;
		//Add 'robot status' label
		JLabel statusLbl = new JLabel("Robot Status");
		//statusLbl.setHorizontalAlignment(SwingConstants.LEFT);
		rightPanel.add(statusLbl, c);
		
		c.gridheight = 1;
		//add 'running/not running' label
		JLabel runningLbl = new JLabel("Not Running");
		rightPanel.add(runningLbl, c);
		
		c.gridheight = 2;
		c.insets = new Insets(4, 4, 4, 4);
		//add output textarea
		outputTextArea = new JTextArea(4, 19);
		outputTextArea.append("output test text");
		outputTextArea.setEditable(false);
		outputTextArea.setLineWrap(false);
		JScrollPane outputScroll = new JScrollPane(outputTextArea);
		outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		rightPanel.add(outputScroll, c);
		//rightPanel.add(outputTextArea, c);
		
		c.gridheight = 1;
		c.insets = new Insets(1, 1, 1, 1);
		//add 'sensor data' label
		JLabel sensorLbl = new JLabel("Sensor Data");
		rightPanel.add(sensorLbl, c);
		
		c.gridheight = GridBagConstraints.RELATIVE;
		c.insets = new Insets(4, 4, 4, 4);
		//add sensor data textarea/panel 
		sensorOutputArea = new JTextArea(25, 19);
		sensorOutputArea.append("sensor output test text");
		sensorOutputArea.setEditable(false);
		sensorOutputArea.setLineWrap(false);
		JScrollPane sensorScroll = new JScrollPane(sensorOutputArea);
		sensorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sensorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		rightPanel.add(sensorScroll, c);
		//rightPanel.add(sensorOutputArea, c);
		
		return rightPanel;
		
	}
	
	
}

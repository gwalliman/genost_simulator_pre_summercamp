package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import robotinterpreter.RobotInterpreter;
import robotsimulator.MainEntry;
import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.robot.SonarSensor;


//Used to organize simulator components. Held by the main applet class in a JTabbedPane
public class SimulatorPanel extends JPanel implements ActionListener {
	
	//Components
	//ButtonGrid
	private JPanel leftPanel;
	private JButton openCodeBtn;			//Button for loading code from a file
	private JLabel codeNameLbl;				//Label to display loaded code file name
	private JButton openLoadoutBtn;			//Button for loading robot loadouts from a file
	private JLabel loadoutNameLbl;			//Label to display loaded loadout file name
	private JButton openMazeBtn;			//Button for loading maze from a file
	private JLabel mazeNameLbl;				//Label to display loaded maze file name
	private JButton runBtn;					//Button to begin executing the simulation
	private JButton stopBtn;				//Button to stop executing the simulation
	private JButton reloadCodeBtn;			//Button to reload the code from the output area
	private JButton resetBtn;				//Button to reset the maze, robot position, and stop execution. 
	
	//Right Panel
	private JPanel rightPanel;
	private JLabel runningLbl;				//Robot status-- running or not
	private JTextArea outputTextArea;		//Holds output from running code, errors, etc.
	private JTextArea sensorText;			//Refreshed with sensor data
	private JPanel sensorPanel;				//Holds labels for sensor data
	
	//Variables
	private int width, height, fps;
	
	//Reference to the main containing class & simulator
	private MainApplet main;
	private Simulator sim;
		
	//Simulator stage
	private int stageWidth = 520;
	private int stageHeight = 400;
	//Simulator variables
	private RobotInterpreter r;
	SwingWorker<Void, Void> executor;
	
	//Thread for updating sensor values
	sensorThread sThread;
	
	//Whether all necessary files are loaded in-- i.e. can we execute?
	private boolean readyToRun = false;
	
	private JPanel stagePanel;
	private JScrollPane stageScroll;
		
	//File IO
	private JFileChooser fileChooser;
	private FileNameExtensionFilter txtFilter;
	private FileNameExtensionFilter xmlFilter;

	DecimalFormat df = new DecimalFormat("#.0");
	
	public SimulatorPanel(int w, int h, int f, Simulator s, MainApplet m)
	{
		
		width = w;
		height = h;
		fps = f;
		
		sim = s;
		main = m;
		//sonars = sim.getRobot().getSonarSensors();
		
		
		
		fileChooser = new JFileChooser(MainEntry.resourcePath);
		
		
		txtFilter = new FileNameExtensionFilter("Text Files ('.txt')", "txt");
		xmlFilter = new FileNameExtensionFilter("XML Files ('.xml')", "xml");
		
		
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
		
		
		leftPanel = createLeftPanel();
		simPane.add(leftPanel, leftSideConstraints);
		
		rightPanel = createRightPanel();
		simPane.add(rightPanel, rightSideConstraints);

		add(simPane);
		
		loadDefaults();		
		//Start up the sensor thread
        sThread = new sensorThread();
        (new Thread(sThread)).start();
	}
	
	//Load in default options-- default map, sensor loadout, and program
	private void loadDefaults()
	{
		//Load loadout config
		main.configFile = new File(MainEntry.loadoutPath + "/DefaultLoadout.xml");
		loadoutNameLbl.setText("Current Config: " + main.configFile.getName());
		updateRunningStatus();
		
		//Update the robot's sensor and loadout configuration
		sim.importLoadout(main.configFile);
	}
	
	//Builds the left side of the window-- input buttons, stage, etc.
	public JPanel createLeftPanel()
	{
		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints topConstraints = new GridBagConstraints();
		topConstraints.gridx = 0;
		topConstraints.gridy = 0;
		topConstraints.gridheight = 1;
		topConstraints.insets = new Insets(4, 4, 4, 4);
		//Create button grid panel and add it with these constraints
		leftPanel.add(createButtonGridPanel(), topConstraints);
		
		GridBagConstraints bottomConstraints = new GridBagConstraints();
		bottomConstraints.gridx = 0;
		bottomConstraints.gridy = GridBagConstraints.RELATIVE;
		bottomConstraints.gridheight = 2;
		bottomConstraints.insets = new Insets(4, 4, 4, 4);
		//Create stage and add it with these constraints
		stagePanel = Stage.createStagePanel(stageWidth, stageHeight, fps, sim, false);
		leftPanel.add(stagePanel, bottomConstraints);
		
		//For now, create a text area to fill in the space
		//JTextArea tempStage = new JTextArea(27, 50);
		//leftPanel.add(tempStage, bottomConstraints);
		
		return leftPanel;
	}
	
	public JPanel createButtonGridPanel()
	{
		//define 4x2 grid
		GridLayout g = new GridLayout(4, 2, 20, 4);
		JPanel bGridPanel = new JPanel(g);
		bGridPanel.setSize(new Dimension(600, 100));
		//Add buttons and labels to it as needed
		openCodeBtn = new JButton("Load Program");
		openCodeBtn.addActionListener(this);
		bGridPanel.add(openCodeBtn);
		
		codeNameLbl = new JLabel("Current Program: ");
		bGridPanel.add(codeNameLbl);
			
		openMazeBtn = new JButton("Load Maze");
		openMazeBtn.addActionListener(this);
		bGridPanel.add(openMazeBtn);
		
		mazeNameLbl = new JLabel("Current Maze: ");
		bGridPanel.add(mazeNameLbl);
		
		openLoadoutBtn = new JButton("Load Config");
		openLoadoutBtn.addActionListener(this);
		openLoadoutBtn.setEnabled(false);
		bGridPanel.add(openLoadoutBtn);
		
		loadoutNameLbl = new JLabel("Current Config: ");
		bGridPanel.add(loadoutNameLbl);
		
		JPanel commandPanel = new JPanel();
		
		runBtn = new JButton("Execute!");
		runBtn.addActionListener(this);
		runBtn.setEnabled(false);
		//bGridPanel.add(runBtn);
		commandPanel.add(runBtn);
		
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(this);
		stopBtn.setEnabled(false);
		//bGridPanel.add(stopBtn);
		commandPanel.add(stopBtn);
		
		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(this);
		//resetBtn.setEnabled(false);
		//bGridPanel.add(resetBtn);
		commandPanel.add(resetBtn);
		
		bGridPanel.add(commandPanel);
		
		return bGridPanel;
	}
	
	//Builds the right side of the window-- status, output, sensor data, etc.
	public JPanel createRightPanel()
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
		runningLbl = new JLabel("Waiting for Program...");
		rightPanel.add(runningLbl, c);
		
		c.gridheight = 2;
		c.insets = new Insets(4, 4, 4, 4);
		//add output textarea
		outputTextArea = new JTextArea(12, 19);
		
		/*
		TODO: Make output text interactable-- highlighting? 
		Highlighting might require a custom component or using a JEditorPane
		-First approach is most flexible. Would likely be a series of 
		 separate textfield-like components that we adjust the font of on the fly
		
		-Second part causes issues, because it uses HTML markup, which we'd need 
		 to modify on the fly-- add <b> tags to executing lines, remove all 
		 formatting for re-execution, but is the simplest approach. 
		
		-Third approach-- some kind of ~adorner~ that just moves position based on
		 currently executing code and overlays a highlight box or something.
		*/
		outputTextArea.setLineWrap(false);
		
		JScrollPane outputScroll = new JScrollPane(outputTextArea);
		outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		rightPanel.add(outputScroll, c);
		
		reloadCodeBtn = new JButton("Reload Code from Text");
		reloadCodeBtn.addActionListener(this);		
		rightPanel.add(reloadCodeBtn, c);
		
		
		c.gridheight = 1;
		c.insets = new Insets(1, 1, 1, 1);
		//add 'sensor data' label
		JLabel sensorLbl = new JLabel("Sensor Data");
		rightPanel.add(sensorLbl, c);
		
		c.gridheight = GridBagConstraints.RELATIVE;
		c.insets = new Insets(4, 4, 4, 4);
		//add sensor data panel 
		sensorPanel = createSensorPanel();
		rightPanel.add(sensorPanel, c);
		
		return rightPanel;
		
	}
	
	private JPanel createSensorPanel()
	{
		System.out.println("Creating Sensor Panel...");
		JPanel rtn = new JPanel(new GridLayout(2,1));
		rtn.setPreferredSize(new Dimension(200, 400));

		sensorText = new JTextArea(24, 40);
		sensorText.setEditable(false);
		rtn.add(sensorText);
		
		//Can add any other mission critical data here as well
		
		return rtn;
	}
		
	private ArrayList<SonarSensor> getRobotSonars()
	{
		return sim.getRobot().getSonarSensors();
	}
	/*
	Use the static method in Stage instead!
	private JPanel createStagePanel()
	{
		stagePanel = new JPanel();
		stagePanel.setSize(520, 400);
		simStage = new Stage(stageWidth * 2, stageHeight * 2, fps, sim);
		//simStage.allowEditing();

		stageScroll = new JScrollPane(simStage);
		stageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		stageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		stageScroll.setSize(stageWidth, stageHeight);
		
		stagePanel.add(stageScroll);
		//rtn.add(simStage);
		return stagePanel;		
	}
	*/
	
	//Updates the runningLbl with what it's waiting on (code, maze, etc.) and its current status
	private void updateRunningStatus()
	{
		readyToRun = false;
		runBtn.setEnabled(false);
		openLoadoutBtn.setEnabled(false);
		
		if (main.codeFile == null)
			{
				runningLbl.setText("Waiting for Program...");
			}
		else if (main.mapFile == null)
			{
				runningLbl.setText("Waiting for maze file...");
			}
		else if (main.configFile == null)
			{
				openLoadoutBtn.setEnabled(true);
				runningLbl.setText("Waiting for robot configuration...");
			}
		else
			{
				runningLbl.setText("Ready!");	
				readyToRun = true;
				openLoadoutBtn.setEnabled(true);
    			runBtn.setEnabled(true);
    			resetBtn.setEnabled(true);
			}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == openCodeBtn)
		{
			//Open a file chooser dialog to load in code.
			//Restrict filechooser to the correct datatype
			fileChooser.setFileFilter(txtFilter);
			fileChooser.setCurrentDirectory(new File(MainEntry.codePath));
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				main.codeFile = fileChooser.getSelectedFile();
				codeNameLbl.setText("Current Program: " + main.codeFile.getName());
				
				runBtn.setEnabled(true);
				updateRunningStatus();
				
				loadCodeFile();
			}
		}
		else if (e.getSource() == openLoadoutBtn)
		{
			//Open a file chooser dialog to load in robot loadouts
			fileChooser.setFileFilter(xmlFilter);
			fileChooser.setCurrentDirectory(new File(MainEntry.loadoutPath));
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				main.configFile = fileChooser.getSelectedFile();
				loadoutNameLbl.setText("Current Config: " + main.configFile.getName());
				updateRunningStatus();
				
				//Update the robot's sensor and loadout configuration
				reinitializeSensors();
			}
		}
		else if (e.getSource() == openMazeBtn)
		{
			//Open a file chooser dialog to load in maze layouts
			fileChooser.setFileFilter(xmlFilter);
			fileChooser.setCurrentDirectory(new File(MainEntry.mazePath));
			
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				main.mapFile = fileChooser.getSelectedFile();
				mazeNameLbl.setText("Current Maze: " + main.mapFile.getName());
				updateRunningStatus();
				
				//Update the maze here
				sim.importStage(main.mapFile);
				reinitializeSensors();
			}
		}
		else if (e.getSource() == runBtn)
		{
			if (readyToRun)
			{
				//Begin execution, enable the stopBtn, and disable ourselves
				runBtn.setEnabled(false);
				stopBtn.setEnabled(true);
				runningLbl.setText("Running!");
				
				sim.running = true;
	
				//Begin running the simulation
	            executor = new SwingWorker<Void, Void>()
	            {
	            	@Override
	            	public Void doInBackground()
	            	{
	            		System.out.println("doInBackground: " + this.hashCode());
	            		
	            		r = new RobotInterpreter();
	            		r.addRobotListener(sim);
	            		String code = outputTextArea.getText();
	            		r.load(code);
	
	        			sim.getRobot().start();
	        			sim.running = true;
	        			
	            		if(r.isReady())
	            		{
	            			r.execute();
	            		}
						return null;
	            	}
	            	
	            	public void done()
	            	{
	            		System.out.println("done: " + this.hashCode());
	            		
	        			runBtn.setEnabled(true);
	        			stopBtn.setEnabled(false);
	        			runningLbl.setText("Stopped.");
	        			r.stop();
	        			r.removeRobotListener(sim);		//--Need to tell it to stop listening to the interpreter
	        			sim.stop();
	        			
	            	}
	            };
	            executor.execute();
	        }
			else
			{
				//Not ready to run!
				
			}
		}
		else if (e.getSource() == stopBtn)
		{
			//Stop execution, enable the runBtn, and disable ourselves
			stopExecution();
			//updateRunningStatus();
		}
		else if (e.getSource() == resetBtn)
		{
			//Stop execution, reload the maze
			stopExecution();
			
			if (main.mapFile != null)
			{
				sim.importStage(main.mapFile);
				reinitializeSensors();
			}
		}
		else if (e.getSource() == reloadCodeBtn)
		{
			//Loads the program from the edited text area
			loadCodefromText(outputTextArea.getText());			
		}
	}
	
	public void stopExecution()
	{
		if(executor != null)
    	{
    		executor.cancel(true);
			
			if (r != null)
				r.stop();
    	}
		
        sim.stop();
		executor = null;
		
		updateRunningStatus();
		//runBtn.setEnabled(true);
		//stopBtn.setEnabled(false);
		//runningLbl.setText("Stopped.");
		
	}
	
	public void loadCodefromText(String code)
	{
		//Convert the text in the textarea to a file and set the codeFile in main to be this file
		BufferedWriter w = null;
		try
		{
		File textCode = new File("ModifiedCode");
		w = new BufferedWriter(new FileWriter(textCode));
		w.write(code);
		w.close();
		
		main.codeFile = textCode;
		codeNameLbl.setText("Current Program: " + "Modified from Text*");
		
		runBtn.setEnabled(true);
		updateRunningStatus();
		
		loadCodeFile();
		
		}
		catch (Exception writerE)
		{
			writerE.printStackTrace();
		}
		finally
		{
			try 
			{
				w.close();
			} 
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}

	}
	
	public void loadCodeFile()
    {
		try 
		{
			FileReader fr = new FileReader(main.codeFile);
		    BufferedReader br = new BufferedReader(fr);
		    String line = "";
            String code = "";
            
            while((line = br.readLine()) != null)
            {
                 code += line + "\n";
            }
             
            br.close();
            fr.close();
             
            outputTextArea.setText(null);
            outputTextArea.append(code);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
	
	public void updateStage(int width, int height)
	{
		stageWidth = width;
		stageHeight = height;
		stagePanel = Stage.createStagePanel(width, height, fps, sim, false);
		sim.getWorld().setGridWidth(width);
		sim.getWorld().setGridHeight(height);
	}
	
	//Hacky method to reinitialize sensors and have them work again after the maze has changed
	private void reinitializeSensors()
	{
		if (main.configFile != null)
			sim.importLoadout(main.configFile);	
		
		//Pause the sensor thread
		stopSensorThread();
		
		//Also update the sensor panel
		rightPanel.remove(sensorPanel);
		sensorPanel = null;
		sensorPanel = createSensorPanel();
		rightPanel.add(sensorPanel);
		rightPanel.revalidate();
		
		//Resume the sensor thread
		resumeSensorThread();
	}

	
	private void updateSensors()
	{
		try
		{
			String newSensorData = "[Sonar Sensors]\n";
			ArrayList<SonarSensor> sensorList = sim.getRobot().getSonarSensors();
			
			for (SonarSensor s : sensorList)
			{
				String t = "" + s.getLabel() + ": " + df.format(s.getConeSensorValue());
				t +=  "\n";
				newSensorData += t;
			}
	
			sensorText.setText(newSensorData);
		}
		catch (ConcurrentModificationException e)
		{
			System.out.println("CONCMOD");
			//e.printStackTrace();
		}
	}
	
	public void resumeSensorThread()
	{
		sThread.running = true;
	}
	
	public void stopSensorThread()
	{
		sThread.running = false;
	}
	
	private class sensorThread implements Runnable
	{
		SimulatorPanel s;
		long sleepInterval = 100L;
		//int ticks = 0;
		boolean running = true;
		
		public void run() 
		{
			while (running)
			{
				//System.out.println("[sensorThread]: " + ticks++);
				//call 'updateSensors', then sleep
				updateSensors();
				try 
				{
					Thread.sleep(sleepInterval);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
}



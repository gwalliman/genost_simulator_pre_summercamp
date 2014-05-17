package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import robotinterpreter.RobotInterpreter;
import robotsimulator.RobotSimulator;
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
	//private JButton openMazeBtn;			//Button for loading maze from a file
	private JComboBox openMazeList;
        private JLabel mazeNameLbl;				//Label to display loaded maze file name
	private JButton runBtn;					//Button to begin executing the simulation
	private JButton stopBtn;				//Button to stop executing the simulation
	private JButton reloadCodeBtn;			//Button to reload the code from the output area
	private JButton resetBtn;				//Button to reset the maze, robot position, and stop execution.
	private JButton webloadCodeBtn;			//Button to load code from the web service
	
	private JButton speedBtn;				//Button to toggle speeds. Can later extend to a slider
	private JLabel speedLbl;                //Label to display current speed
	
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
			
	//File IO
	private JFileChooser fileChooser;           //Call this to let users load files
	private FileNameExtensionFilter txtFilter;  //Use this to restrict to text files (code)
	private FileNameExtensionFilter xmlFilter;  //Use this to restrict to xml files (loadouts & mazes)

	DecimalFormat df = new DecimalFormat("#.0");    //Used by sensor output to make display not horrible
	
	private boolean highSpeed = false;
	
	public SimulatorPanel(int w, int h, int f, Simulator s, MainApplet m)
	{	
		width = w;
		height = h;
		fps = f;
		
		sim = s;
		main = m;
        
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
		
            //Gross procedure to serialize the resource file into a File object
            String loadoutXml = getLoadoutData("DefaultLoadout");
            loadoutNameLbl.setText("Current Config: " + "DefaultLoadout.xml");
            main.configFile = loadoutXml;
		
            //Update the robot's sensor and loadout configuration
            sim.importLoadout(main.configFile);
            updateRunningStatus();
            
            //Load the initial code from the web service
            loadCodeFromWeb();
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
			
		/*openMazeBtn = new JButton("Load Maze");
		openMazeBtn.addActionListener(this);
		bGridPanel.add(openMazeBtn);*/
                
                openMazeList = new JComboBox(getMazesFromWeb());
                openMazeList.addActionListener(this);
                bGridPanel.add(openMazeList);
		
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
		commandPanel.add(runBtn);
		
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(this);
		stopBtn.setEnabled(false);
		commandPanel.add(stopBtn);
		
		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(this);
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
		rightPanel.add(statusLbl, c);
		
		c.gridheight = 1;
		//add 'running/not running' label
		runningLbl = new JLabel("Waiting for Program...");
		rightPanel.add(runningLbl, c);
		
		c.gridheight = 2;
		c.insets = new Insets(4, 4, 4, 4);
		
		//add output textarea
		outputTextArea = new JTextArea(12, 19);
		outputTextArea.setLineWrap(false);
		outputTextArea.setTabSize(2);
		
		JScrollPane outputScroll = new JScrollPane(outputTextArea);
		outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		rightPanel.add(outputScroll, c);
		
		JPanel actionPanel = new JPanel(new GridLayout(2, 1));
		
		reloadCodeBtn = new JButton("Reload Code from Text");
		reloadCodeBtn.addActionListener(this);		
		actionPanel.add(reloadCodeBtn);
		
		webloadCodeBtn = new JButton("Load Code from Web");
		webloadCodeBtn.addActionListener(this);
		actionPanel.add(webloadCodeBtn);
		
		rightPanel.add(actionPanel, c);

		//Experimental speed toggle!
                //Can later replace this with a slider
		c.gridheight = 1;
		c.insets = new Insets(4, 4, 4, 4);
		JPanel speedPanel = new JPanel();

		speedBtn = new JButton("Toggle Speed");
		speedBtn.addActionListener(this);
		speedPanel.add(speedBtn);
		
		speedLbl = new JLabel("Speed: Slow");
		speedPanel.add(speedLbl);
		
		rightPanel.add(speedPanel, c);

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
		RobotSimulator.println("Creating Sensor Panel...");
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
		else if (main.mapData == null)
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
			
		}
		else if (e.getSource() == openLoadoutBtn)
		{

		}
		else if (e.getSource() == openMazeList)
		{
                    openNewMaze(null);
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
	            		RobotSimulator.println("doInBackground: " + this.hashCode());
	            		
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
	            	
                    @Override
	            	public void done()
	            	{
	            		RobotSimulator.println("done: " + this.hashCode());
	            		
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
		}
		else if (e.getSource() == resetBtn)
		{
			//Stop execution, and reload the maze
			stopExecution();
			
			if (main.mapData != null)
			{
				sim.importStage(main.mapData);
				reinitializeSensors();
			}
		}
		else if (e.getSource() == reloadCodeBtn)
		{
			//Loads the program from the edited text area
			loadCodefromText(outputTextArea.getText(), "Modified from Text*");			
		}
		else if (e.getSource() == webloadCodeBtn)
		{
			//Loads the program from the web service
                    loadCodeFromWeb();
                    openNewMaze(main.mapData);
		}
		else if (e.getSource() == speedBtn)
		{
			//Toggle between high and low speeds for robot
            //Speed is a multiplier-- e.g. sM=2 is twice as fast as sM=1
			if (highSpeed)
			{
				highSpeed = false;
				Robot.speedModifier = 1;
				speedLbl.setText("Speed: Slow");
			}
			else
			{
				highSpeed = true;
				Robot.speedModifier = 2;
				speedLbl.setText("Speed: Fast");
			}
		}
	}
	
    //Calls the web service and loads in the code file from the web
    public void loadCodeFromWeb()
    {
        String webdata = getCode();
        String[] splitWebData = webdata.split("%", 2);

        String code;
        if(splitWebData.length == 2)
        {
            String mazeID = splitWebData[0];
            code = splitWebData[1];
            
            List<String> validMazes = Arrays.asList(getMazesFromWeb());
            if(validMazes.contains(mazeID))
            {
                main.mapData = mazeID;
            }
        }
        else
        {
            code = splitWebData[0];
        }
        loadCodefromText(code, "Loaded from Web*");
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
	}
	
	public void loadCodefromText(String code, String newCodeName)
	{
		
            //Convert 'code' to a file and load the code in from there
            	main.codeFile = code;
		codeNameLbl.setText("Current Program: " + newCodeName);
		
		runBtn.setEnabled(true);
		updateRunningStatus();
		
		loadCodeFile();
	}
	
    //Loads the code into the program from the codeFile in main
    public void loadCodeFile()
    {
		try 
		{
                    outputTextArea.setText(null);
                    outputTextArea.append(main.codeFile);
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
	public void reinitializeSensors()
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

    //Updates the sensor output text
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
                    e.printStackTrace();
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
	
    //The sensor update happens on a timer in this thread
	private class sensorThread implements Runnable
	{
		SimulatorPanel s;
		long sleepInterval = 100L;
		boolean running = true;
		
        @Override
		public void run() 
		{
			while (running)
            {
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

    //Autogenerated by Netbeans to call the code service
    private String getCode() 
    {
        try
        {
            /*org.tempuri.Service service = new org.tempuri.Service();            //* Autogen'd
            org.tempuri.IService port = service.getBasicHttpBindingIService();  //* Autogen'd
            return port.getCode();                                              //* Autogen'd*/
            
            String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/codeRestSvc/Service.svc/GetCode/";
            if(main.codeId != null && main.codeId != "")
            {
                uri = uri + main.codeId;
            }
            else
            {
                uri = uri + "asdf123";
            }
            
            try
            {
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept","application/xml");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document document = builder.parse(conn.getInputStream());
                Element root = document.getDocumentElement();
                
                Node child = root.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    return cd.getData();
                }
            }
            catch(Exception e2)
            {
                e2.printStackTrace();
            }

            return null;
        }
        catch (Exception e)
        {
            RobotSimulator.println("Couldn't load code from web. ");
            return "Couldn't load code from web. ";
        }
    }

    public String[] getMazesFromWeb()
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/listMazes";
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/json");
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(isr);
            ArrayList<String> mazeList = new ArrayList<String>();
            for(int x = 0; x < jsonArray.size(); x++)
            {
                mazeList.add((String) jsonArray.get(x));
            }
            
            return mazeList.toArray(new String[mazeList.size()]);
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public void openNewMaze(String mazeId)
    {
        if(mazeId == null)
        {
            mazeId = getSelectedMaze();
        }
        String mazeXml = getMazeData(mazeId);

        if (mazeXml != null)
        {
            main.mapData = mazeXml;
            mazeNameLbl.setText("Current Maze: " + mazeId);
            updateRunningStatus();

            //Update the maze here
            sim.importStage(main.mapData);
            reinitializeSensors();

            //Also signal to mazebuilder to update its displays
            if (!MainApplet.studentBuild)
            {
                main.mazePanel.refreshMazeSettings();
            }
        }
    }
    
    public String getMazeData(String mazeId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getMaze/" + mazeId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(conn.getInputStream());
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", ""); 
            
            return output;
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public static String getLoadoutData(String loadoutId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getLoadout/" + loadoutId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(conn.getInputStream());
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", ""); 
            
            return output;
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public static Document getThemeData(String themeId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getTheme/" + themeId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(conn.getInputStream());
            return document;
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    public static InputStream getThemeImage(String themeId, String imageId)
    {
        String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/getThemeImage/" + themeId + "/" + imageId;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/xml");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            return conn.getInputStream();
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
        
        return null;
    }
    
    private String getSelectedMaze()
    {
        return (String) openMazeList.getSelectedItem();
    }
}



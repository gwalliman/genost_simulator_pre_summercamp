package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import robotsimulator.Simulator;
import robotsimulator.world.CellTheme;
import robotsimulator.world.CellType;
import robotsimulator.world.World;


//Used to organize maze components. Held by the main applet class in a JTabbedPane
public class MazeBuilderPanel extends JPanel implements ActionListener {
	
	//Reference to the main containing class & simulator
	private MainApplet main;
	private Simulator sim;
	private int fps;
		
    //Default attributes of maze elements
	private int mazeWidth = 16;
	private int mazeHeight = 12;
	private int gridWidth = 16;
	private int gridHeight = 16;
	
	JButton newMazeBtn;         //On click: Creates a new maze
	JButton loadMazeBtn;        //On click: opens a dialog to load a maze
	JButton saveMazeBtn;        //On click: opens a dialog to save current maze
	
	JLabel currentThemeLbl;     //Displays the name of the current theme
	
	JSpinner widthSpinner;      //Allows changing maze width
	JSpinner heightSpinner;     //Allows changing maze height
    //Spinner bounds
	private final int wMin = 5;
	private final int wMax = 30;
	private final int hMin = 5;
	private final int hMax = 20;
	SpinnerModel widthModel = new SpinnerNumberModel(16, wMin, wMax, 1);    //Defines maze width spinner bounds		
	SpinnerModel heightModel = new SpinnerNumberModel(12, hMin, hMax, 1);   //Defines maze height spinner bounds
	
	JPanel leftPanel;       //Contains buttons and the stage on the left
	JPanel palettePanel;    //Contains the maze object inventory/palette on the right
	JPanel stagePanel;      //Holds just the stage being edited
	
	//File IO
	private JFileChooser fileChooser;               //Call this to open a file dialog
	private FileNameExtensionFilter xmlFilter;      //Use this when restricting to just xml files
	
	public MazeBuilderPanel(int f, Simulator s, MainApplet m)
	{
		fps = f;
		sim = s;
		main = m;
		
		World simWorld = sim.getWorld();
		gridWidth = simWorld.getGridWidth();
		gridHeight = simWorld.getGridHeight();
		
		//fileChooser = new JFileChooser("");
		xmlFilter = new FileNameExtensionFilter("XML Files ('.xml')", "xml");
        
		leftPanel = createLeftPanel();
		palettePanel = createPalettePanel();
	
		add(leftPanel);
		add(palettePanel);	
        
        //Lazy way to set up the initial world size
		simWorld.adjustWorld(mazeWidth, mazeHeight);
		updateStagePanel();
	}
	
	private JPanel createLeftPanel()
	{
		//Grid of buttons in one row. Directly below we'll have the current editable maze.
		JPanel rtn = new JPanel(new GridBagLayout());
		rtn.setSize(600, 100);
		GridBagConstraints g1 = new GridBagConstraints();
		g1.insets = new Insets(4, 4, 4, 4);
		
		GridLayout g2 = new GridLayout(1, 0);
		g2.setHgap(8);
		
        //Add interface buttons
		newMazeBtn = new JButton("New Maze");
		newMazeBtn.addActionListener(this);
		
		loadMazeBtn = new JButton("Load Maze");
		loadMazeBtn.addActionListener(this);
		
		saveMazeBtn = new JButton("Save Maze");
		saveMazeBtn.addActionListener(this);
		
		widthSpinner = new JSpinner(widthModel);
		heightSpinner = new JSpinner(heightModel);
		
		//Listener to detect changes in the spinners and update height/width
		ChangeListener c = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				mazeWidth = (Integer) widthModel.getValue();
				mazeHeight = (Integer) heightModel.getValue();
				
				World simWorld = sim.getWorld();
				simWorld.adjustWorld(mazeWidth, mazeHeight);
				//Adjust the drawn stage, as well
				updateStagePanel();
			}		
		};
		
		widthSpinner.addChangeListener(c);
		heightSpinner.addChangeListener(c);
		
		JPanel buttonPanel = new JPanel(g2);
		
		buttonPanel.add(newMazeBtn, g2);
		buttonPanel.add(loadMazeBtn, g2);
		buttonPanel.add(saveMazeBtn, g2);
		buttonPanel.add(widthSpinner, g2);
		buttonPanel.add(heightSpinner, g2);
		
		rtn.add(buttonPanel, g1);
		
		//Add the stage and scroll below
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.WEST;
		stagePanel = Stage.createStagePanel(mazeWidth * gridWidth, mazeHeight * gridHeight, fps, sim, true);
		rtn.add(stagePanel, g1);
		
		return rtn;
	}
	
	//Resizes the stage in StagePanel based on new width and/or height
	private void updateStagePanel()
	{
		World simWorld = sim.getWorld();
		gridWidth = simWorld.getGridWidth();
		gridHeight = simWorld.getGridHeight();
		stagePanel = Stage.createStagePanel(mazeWidth * gridWidth, mazeHeight * gridHeight, fps, sim, true);		
	}
		
	private JPanel createPalettePanel()
	{
		JPanel rtn = new JPanel();
		rtn.setSize(200, 600);
		
		currentThemeLbl = new JLabel("Current Theme: " + sim.themeid);
				
		rtn.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		
		JPanel buttonPanel = new JPanel(new GridLayout(3, 0));
		buttonPanel.add(currentThemeLbl);
		
		JSeparator jsep = new JSeparator();
		jsep.setSize(200, 20);
		jsep.setPreferredSize(new Dimension(200, 20));
		buttonPanel.add(jsep);
		
		c.gridy = 0;
		rtn.add(buttonPanel, c);
		
		//Dynamically fill a panel with tile buttons
		palettePanel = createPaletteButtons();
				
		//Wrap it all in a scroll pane
		JScrollPane paletteScroll = new JScrollPane(palettePanel);
		paletteScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		paletteScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		paletteScroll.setSize(200, 500);
		
		c.gridy = 1;
		rtn.add(paletteScroll, c);
		return rtn;
	}
	
	private JPanel createPaletteButtons()
	{
		JPanel rtn = new JPanel(new GridLayout(0, 1));
		rtn.setSize(200, 400);
		rtn.setPreferredSize(new Dimension(200, 400));
				
        //Create buttons for each cell type in the current theme
		for(CellType ctype : sim.getWorld().getCellTypes())
		{
			String cellTypeID = ctype.getID();	
			
			JButton b; 
			if(sim.getWorld().getCellThemes().containsKey(cellTypeID))
			{
				CellTheme cellTheme = sim.getWorld().getCellThemes().get(cellTypeID);
				b = new JButton(new ImageIcon(cellTheme.getImage()));
			}
			else
			{
				b = new JButton(ctype.getLabel());
				b.setBackground(ctype.getColor());
				b.setOpaque(true);
			}
			
			b.setName(ctype.getID());
			b.setSize(150, 50);
			b.setPreferredSize(new Dimension(150, 50));
            //Adds an action listener to each button
            //When clicked, sets the current cell type added when clicking to this
			ActionListener a = new ActionListener() 
			{
                @Override
				public void actionPerformed(ActionEvent a) 
				{
					JButton b = (JButton)a.getSource();
					ArrayList<CellType> types = sim.getWorld().getCellTypes();
					String id = b.getName();
					for(CellType ct : types)
					{
						if(ct.getID().equals(id))
						{
							sim.getWorld().setCurrentCellType(ct);
							break;
						}
					}
				}
			}; 
			b.addActionListener(a);
			rtn.add(b);
		}
		return rtn;	
	}
	

	@Override
	//When a button is pressed, send that event to the proper method
	//Palette / Block buttons are handled by an internal listener elsewhere (see createPaletteButtons)
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == newMazeBtn)
		{
			newMaze();
		}
		else if (e.getSource() == loadMazeBtn)
		{
			loadMaze();
		}
		else if (e.getSource() == saveMazeBtn)
		{
			saveMaze();
		}
	}
	
	//When the 'New Maze' button is clicked, creates a new, empty maze
    //Also prompt the user to select a theme for the maze
	private void newMaze()
	{
		//Collect available themes
		//No way to do this easily with the classloader-- hardcoded for now, since themes are hardcoded anyway
		String[] themeNames = { "default", "loz", "minecraft", "pkmn" };
		
		
		//Open the dialog
		String userChoice = (String)JOptionPane.showInputDialog(new Frame(), "Select a theme: ", "New Maze", JOptionPane.PLAIN_MESSAGE, null, themeNames, themeNames[0]);
		
		if (userChoice != null && userChoice.length() > 0)
		{
			//User has made a choice
			sim.themeid = userChoice;
			sim.getWorld().setTheme(sim.themeid);
			
			//Reset the theme palette
			refreshPalette();
			sim.resetStage();
		}
		else
		{
			//Cancelled, or otherwise wrong
		}
		
	}
	
	//When the 'Load Maze' button is clicked, loads a maze in from a file for editing. 
	//Extended from the SimPanel method.
	public void loadMaze()
	{
		/*fileChooser.setFileFilter(xmlFilter);
		
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File loadedMaze = fileChooser.getSelectedFile();	
			//Update the maze here
			sim.importStage(loadedMaze);
			refreshMazeSettings();
		}*/
                String uri = "http://venus.eas.asu.edu/WSRepository/eRobotic2/mazeSvc/Service.svc/listMazes";
                try
                {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept","application/xml");
                    InputStream xml = conn.getInputStream();
                    String xmlString = xml.toString();
                    System.out.println(xmlString);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
	}
    
    //Refreshes spinner values, theme palettes, etc. based on the current maze
    public void refreshMazeSettings()
    {
        //Update the width and height spinners
        World simWorld = sim.getWorld();
        int newW = simWorld.getWidth() / simWorld.getGridWidth();
        int newH = simWorld.getHeight() / simWorld.getGridHeight();

        //Constrain new width and height to spinner boundaries
        if (newW > wMax)
            newW = wMax;
        else if (newW < wMin)
            newW = wMin;
        if (newH > hMax)
            newH = hMax;
        else if (newH < hMin)
            newH = hMin;

        widthSpinner.setValue(newW);
        heightSpinner.setValue(newH);
        simWorld.adjustWorld(newW, newH);

        //Update the block palette and theme
        currentThemeLbl.setText("Current Theme: " + sim.themeid);

        refreshPalette();
    }

	//Hacky method to redraw the block palette
	private void refreshPalette()
	{
		remove(palettePanel);
		palettePanel = null;
		palettePanel = createPalettePanel();
		add(palettePanel);
		revalidate();
		
	}
	
	//When the 'Save Maze' button is clicked, saves the maze on the user's local file system.
	//Similar to load maze (but in reverse, naturally)
	private void saveMaze()
	{
		//Open a file dialog and write the file to XML
		
		int returnVal = fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			//Serialize the file into an xml file 
			File saveFile = fileChooser.getSelectedFile();
			sim.exportStage(saveFile);	
		}
	}
}

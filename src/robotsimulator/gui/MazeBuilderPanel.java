package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
		
	private int mazeWidth = 16;
	private int mazeHeight = 12;
	private int gridWidth = 16;
	private int gridHeight = 16;
	
	JButton newMazeBtn;
	JButton loadMazeBtn;
	JButton saveMazeBtn;
	
	JButton loadThemeBtn;
	JLabel currentThemeLbl;
	
	JSpinner widthSpinner;
	JSpinner heightSpinner;
	SpinnerModel widthModel = new SpinnerNumberModel(16, 5, 30, 1);		//Defaults to 10, min 5, max 15, step 1
	SpinnerModel heightModel = new SpinnerNumberModel(12, 5, 20, 1);		//Defaults to 10, min 5, max 15, step 1
	
	JPanel leftPanel;
	JPanel palettePanel;
	JPanel stagePanel;
	
	//File IO
	private JFileChooser fileChooser;
	private FileNameExtensionFilter xmlFilter;
	
		
	public MazeBuilderPanel(int f, Simulator s, MainApplet m)
	{
		fps = f;
		sim = s;
		main = m;
		
		World simWorld = sim.getWorld();
		gridWidth = simWorld.getGridWidth();
		gridHeight = simWorld.getGridHeight();
		
		//TODO: Defaults to Ian's simulator folder. Use the commented out version (default path) in live releases. 
		fileChooser = new JFileChooser("C:/Users/IAN/Documents/GitHub/robotsimulator/Resources");
		//fileChooser = new JFileChooser();		
		xmlFilter = new FileNameExtensionFilter("XML Files ('.xml')", "xml");
				
		
		leftPanel = createLeftPanel();
		palettePanel = createPalettePanel();
	
		add(leftPanel);
		
		add(palettePanel);	
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
				//System.out.println("Spinners: [" + newW + ", " + newH + "]");
				
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
		stagePanel = Stage.createStagePanel(mazeWidth * gridWidth, mazeHeight * gridHeight, fps, sim);
		rtn.add(stagePanel, g1);
		
		return rtn;
	}
	
	//Resizes the stage in StagePanel based on new width and/or height
	private void updateStagePanel()
	{
		World simWorld = sim.getWorld();
		gridWidth = simWorld.getGridWidth();
		gridHeight = simWorld.getGridHeight();
		stagePanel = Stage.createStagePanel(mazeWidth * gridWidth, mazeHeight * gridHeight, fps, sim);		
		
	}
	
	/*
	Use the static method in Stage instead!
	private JPanel createStagePanel()
	{
		JPanel stagePanel = new JPanel();
		stagePanel.setSize(520, 400);
		Stage simStage = new Stage(mazeWidth * 2, mazeHeight * 2, fps, sim);
		simStage.allowEditing();

		JScrollPane stageScroll = new JScrollPane(simStage);
		stageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		stageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		stageScroll.setSize(mazeWidth, mazeHeight);
		
		stagePanel.add(stageScroll);
		return stagePanel;
	}
	*/
	
	private JPanel createPalettePanel()
	{
		//Theme button and label, divider, then scroll area with dynamic buttons for each tile type
		JPanel rtn = new JPanel();
		rtn.setSize(200, 600);
		
		loadThemeBtn = new JButton("Load Theme");
		loadThemeBtn.addActionListener(this);
		
		currentThemeLbl = new JLabel("Current Theme: none");
		
		rtn.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		
		JPanel buttonPanel = new JPanel(new GridLayout(3, 0));
		buttonPanel.add(loadThemeBtn);
		buttonPanel.add(currentThemeLbl);
		
		buttonPanel.add(new JSeparator());
		
		c.gridy = 0;
		rtn.add(buttonPanel, c);
		
		//Dynamically fill a panel with tile button
		palettePanel = createPaletteButtons();
				
		//Wrap it all in a scroll pane
		JScrollPane paletteScroll = new JScrollPane(palettePanel);
		paletteScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		paletteScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		paletteScroll.setSize(200, 500);
		
		c.gridy = 1;
		rtn.add(paletteScroll, c);
		//rtn.add(palettePanel, c);
		return rtn;
	}
	
	private JPanel createPaletteButtons()
	{
		JPanel rtn = new JPanel(new GridLayout(0, 1));
		rtn.setSize(200, 400);
		rtn.setPreferredSize(new Dimension(200, 400));
				
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
			ActionListener a = new ActionListener() 
			{
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
		else if (e.getSource() == loadThemeBtn)
		{
			loadTheme();
		}
	}
	
	//When the 'New Maze' button is clicked, creates a new, empty maze
	private void newMaze()
	{
		sim.resetStage();
	}
	
	//When the 'Load Maze' button is clicked, loads a maze in from a file for editing. 
	//Can copy from the SimPanel method.
	private void loadMaze()
	{
		fileChooser.setFileFilter(xmlFilter);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File loadedMaze = fileChooser.getSelectedFile();	
			//Update the maze here
			sim.importStage(loadedMaze);
			
			//Update the width and height spinners
			World simWorld = sim.getWorld();
			int newW = simWorld.getWidth() / simWorld.getGridWidth();
			int newH = simWorld.getHeight() / simWorld.getGridHeight();
			widthSpinner.setValue(newW);
			heightSpinner.setValue(newH);
			
			//Update the block palette and theme
			currentThemeLbl.setText("Current Theme: " + sim.themeid);
			
			palettePanel = createPaletteButtons();
			
			
		}
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
	
	//Loads in the theme from a file. Will need to call 'createPaletteButtons' again to recreate the buttons
	private void loadTheme()
	{
		
	}

	
}

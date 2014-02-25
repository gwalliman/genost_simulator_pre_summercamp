package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import robotsimulator.Simulator;
import robotsimulator.world.CellTheme;
import robotsimulator.world.CellType;

//Used to organize maze components. Held by the main applet class in a JTabbedPane
public class MazeBuilderPanel extends JPanel {
	
	//Reference to the main containing class & simulator
	private MainApplet main;
	private Simulator sim;
	private int fps;
		
	private Stage maze;
	private int mazeWidth = 480;
	private int mazeHeight = 320;
	
	JButton newMazeBtn;
	JButton loadMazeBtn;
	JButton saveMazeBtn;
	
	JButton loadThemeBtn;
	JLabel currentThemeLbl;
	
	JPanel leftPanel;
	JPanel palettePanel;
	
	public MazeBuilderPanel(int f, Simulator s, MainApplet m)
	{
		fps = f;
		sim = s;
		main = m;
		
		
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
		loadMazeBtn = new JButton("Load Maze");
		saveMazeBtn = new JButton("Save Maze");
		
		JPanel buttonPanel = new JPanel(g2);
		
		buttonPanel.add(newMazeBtn, g2);
		buttonPanel.add(loadMazeBtn, g2);
		buttonPanel.add(saveMazeBtn, g2);
		
		rtn.add(buttonPanel, g1);
		
		//Add the stage and scroll below
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.WEST;
		rtn.add(createStagePanel(), g1);
		
		return rtn;
	}
	
	private JPanel createStagePanel()
	{
		JPanel stagePanel = new JPanel();
		stagePanel.setSize(520, 400);
		Stage simStage = new Stage(mazeWidth, mazeHeight, fps, sim);
		simStage.allowEditing();

		JScrollPane stageScroll = new JScrollPane(simStage);
		stageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		stageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		stageScroll.setSize(mazeWidth, mazeHeight);
		
		stagePanel.add(stageScroll);
		return stagePanel;
	}
	
	private JPanel createPalettePanel()
	{
		//Theme button and label, divider, then scroll area with dynamic buttons for each tile type
		JPanel rtn = new JPanel();
		rtn.setSize(200, 600);
		
		loadThemeBtn = new JButton("Load Theme");
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
		JPanel palettePanel = createPaletteButtons();
				
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
		rtn.setSize(200, 500);
		
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
}

package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.world.CellTheme;
import robotsimulator.world.CellType;

@SuppressWarnings("serial")
public class WorldBuilderPanel extends JPanel
{
	Simulator sim;
	JFileChooser fc = new JFileChooser();
	
	public WorldBuilderPanel(int w, Simulator s) 
	{
		sim = s;
		setUpGUI(w);
	}
	
	private void setUpGUI(int w)
	{
		setPreferredSize(new Dimension(200, w));
		setLayout(new GridLayout(sim.getWorld().getCellTypes().size(), 1));
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
			add(b);
		}
		
		JButton importStage = new JButton("Import Stage");
		ActionListener importA = new ActionListener() 
		{
			public void actionPerformed(ActionEvent a) 
			{
				int returnVal = fc.showOpenDialog(sim.getGUI());
		        if (returnVal == JFileChooser.APPROVE_OPTION) 
		        {
		        	File file = fc.getSelectedFile();
			        sim.importStage(file);
		        } 
		        else 
		        {
		        	System.out.println("Could not output file.");
		        }
			}
		}; 
		importStage.addActionListener(importA);
		add(importStage);
		
		JButton exportStage = new JButton("Export Stage");
		ActionListener export = new ActionListener() 
		{
			public void actionPerformed(ActionEvent a) 
			{
				int returnVal = fc.showSaveDialog(sim.getGUI());
		        if (returnVal == JFileChooser.APPROVE_OPTION) 
		        {
		        	File file = fc.getSelectedFile();
			        sim.exportStage(file);
		        } 
		        else 
		        {
		        	System.out.println("Could not output file.");
		        }
			}
		}; 
		exportStage.addActionListener(export);
		add(exportStage);
	}
}

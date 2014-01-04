package robotsimulator.gui;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.world.CellType;

@SuppressWarnings("serial")
public class WorldBuilderPanel extends JPanel
{
	Simulator sim;
	
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
			Canvas c = new Canvas();
			c.setSize(sim.getWorld().getGridWidth() * ctype.getWidth(), sim.getWorld().getGridHeight() * ctype.getHeight());
			c.setBackground(ctype.getColor());
			add(c);
			
			Button b = new Button(ctype.getLabel());
			b.setName(ctype.getID());
			ActionListener a = new ActionListener() 
			{
				public void actionPerformed(ActionEvent a) 
				{
					Button b = (Button)a.getSource();
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
	}
}

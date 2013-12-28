package robotsimulator.gui;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.robot.SonarSensor;
import robotsimulator.world.CellType;

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
			add(new Button(ctype.getLabel()));
		}
	}
}

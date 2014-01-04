package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.util.ArrayList;

import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.robot.SonarSensor;

@SuppressWarnings("serial")
public class SensorPanel extends JPanel 
{
	Simulator sim;
	ArrayList<SonarSensor> sonars;
	
	public SensorPanel(int h, Simulator s) 
	{
		sim = s;
		sonars = sim.getRobot().getSonarSensors();

		setUpGUI(h);
	}
	
	private void setUpGUI(int h)
	{
		setPreferredSize(new Dimension(h, 200));
		setLayout(new GridLayout(sonars.size(), 1));
		for(SonarSensor son : sonars)
		{
			add(new Label(son.getLabel()));
			Label t = new Label();
	        //t.setEditable(false);
			add(t);
			son.setTextField(t);
		}
	}
}

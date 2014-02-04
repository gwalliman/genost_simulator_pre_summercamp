package robotsimulator.gui;

import java.awt.Dimension;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class TestApplet extends JApplet {
	
	public void init()
	{
		try 
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					buildGUI();
				}
			});
		}
		catch (Exception e)
		{
			System.err.println("couldn't construct the GUI");
			e.printStackTrace();
		}
		
		
	}
	
	private void buildGUI()
	{
		setSize(new Dimension(800, 600));
		
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.setSize(800, 600);
		
		SimulatorPanel s = new SimulatorPanel(240, 120, 60, null);
		tabPane.addTab("Simulator", s);
		
		MazeBuilderPanel m = new MazeBuilderPanel();
		tabPane.addTab("Maze Builder", m);
		
		add (tabPane);
	}
}

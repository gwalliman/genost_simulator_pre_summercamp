package robotsimulator.gui;

import javax.swing.JApplet;

public class TestApplet extends JApplet {
	public TestApplet()
	{
		this.add(new SimulatorPanel(0, 0, 0, null));
	}
}

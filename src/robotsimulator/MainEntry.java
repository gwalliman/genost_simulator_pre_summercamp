package robotsimulator;

import robotsimulator.gui.MainApplet;
import javax.swing.JFrame;

public class MainEntry {

	/**
	 * Main entry point for the program- added to allow creating runnable .jar files. 
	 * Just creates and runs MainApplet.
	 * Props to: http://stackoverflow.com/questions/14506704/how-to-generate-jar-file-with-no-main-method
	 * @param args
	 */
	public static void main(String[] args) 
	{
		MainApplet m = new MainApplet();
		m.init();
		m.start();
		
		JFrame window = new JFrame("Robot Simulator");
		window.setContentPane(m);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		
	}

}

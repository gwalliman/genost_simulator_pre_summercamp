package robotsimulator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.robot.Robot;

@SuppressWarnings("serial")
public class GUI extends JFrame implements KeyListener
{
	private Simulator sim;
	private int fps;
	
	public GUI(int w, int h, int fps, Simulator s)
	{
		sim = s;
		
		this.addKeyListener(this);
		
		setBackground(Color.black);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel codeArea = new CodePanel(h, sim);
		JPanel stage = new Stage(w, h, fps, sim);
		JPanel sensorPanel = new SensorPanel(h, sim);
		JPanel worldBuilderPanel = new WorldBuilderPanel(w, sim);
		add(codeArea, BorderLayout.NORTH);
		add(stage, BorderLayout.CENTER);
		add(sensorPanel, BorderLayout.SOUTH);
		add(worldBuilderPanel, BorderLayout.EAST);
		
		stage.requestFocus();

		pack();
		setVisible(true);
	}
	
	public int getFPS()
	{
		return fps;
	}

	public void keyPressed(KeyEvent e) 
	{
		int keyCode = e.getKeyCode();
		Robot r = sim.getRobot();
		
		switch(keyCode)
		{
			case KeyEvent.VK_UP:
				r.drive('f');
				break;
			case KeyEvent.VK_DOWN:
				r.drive('b');
				break;
			case KeyEvent.VK_LEFT:
				r.turn('l');
				break;
			case KeyEvent.VK_RIGHT:
				r.turn('r');
				break;
		}
	}

	public void keyReleased(KeyEvent e) 
	{
		Robot r = sim.getRobot();
		r.stop();
	}

	public void keyTyped(KeyEvent e) 
	{
	}
}

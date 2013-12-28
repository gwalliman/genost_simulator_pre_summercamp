package robotsimulator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.worldobject.Block;

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
		
		JPanel stage = new Stage(w, h, fps, sim);
		JPanel sensorPanel = new SensorPanel(h, sim);
		JPanel worldBuilderPanel = new WorldBuilderPanel(w, sim);
		add(stage, BorderLayout.CENTER);
		add(sensorPanel, BorderLayout.EAST);
		add(worldBuilderPanel, BorderLayout.SOUTH);
		
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
				//b.setAngle(b.getDegAngle() + 1);
				r.turn('l');
				break;
			case KeyEvent.VK_RIGHT:
				r.turn('r');
				//b.setAngle(b.getDegAngle() - 1);
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		Robot r = sim.getRobot();
		r.stop();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}

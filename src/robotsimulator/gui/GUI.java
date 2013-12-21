package robotsimulator.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;

import robotsimulator.Simulator;
import robotsimulator.robot.Robot;
import robotsimulator.worldobject.Block;

public class GUI extends JFrame implements KeyListener
{
	private Simulator sim;
	private int width;
	private int height;
	private int fps;
	
	public GUI(int w, int h, int fps, Simulator s)
	{
		sim = s;
		width = w;
		height = h;
		
		this.addKeyListener(this);
		
		add(new Stage(w, h, fps, s));
		
		setBackground(Color.black);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
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

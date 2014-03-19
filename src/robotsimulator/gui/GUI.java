package robotsimulator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import robotsimulator.Simulator;

@SuppressWarnings("serial")
public class GUI extends JFrame
{
	private Simulator sim;
	private int fps;
	
	public GUI(int w, int h, int fps, Simulator s)
	{
		sim = s;
	
		setBackground(Color.black);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setKeyBindings();
		
		JPanel codeArea = new CodePanel(h, sim);
		JPanel stage = new Stage(w, h, fps, sim);
		JPanel sensorPanel = new SensorPanel(h, sim);
		JPanel worldBuilderPanel = new WorldBuilderPanel(w, sim);
		
		add(codeArea, BorderLayout.NORTH);
		add(sensorPanel, BorderLayout.SOUTH);
		add(worldBuilderPanel, BorderLayout.EAST);
		add(stage, BorderLayout.CENTER);
		
		
		pack();
		setVisible(true);
	}
	
	public int getFPS()
	{
		return fps;
	}
	
	private void setKeyBindings()
	{
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "stop");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "stop");
	    
		getRootPane().getActionMap().put("up", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		sim.getRobot().drive('f');
	    	}
	    });
	    
		getRootPane().getActionMap().put("down", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		sim.getRobot().drive('b');
	    	}
	    });
	    
		getRootPane().getActionMap().put("left", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		sim.getRobot().turn('l');
	    	}
	    });
	    
		getRootPane().getActionMap().put("right", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		sim.getRobot().turn('r');
	    	}
	    });
	    
		getRootPane().getActionMap().put("stop", new AbstractAction() {
	    	public void actionPerformed(ActionEvent e) 
	    	{
	    		sim.getRobot().stop();
	    	}
	    });
	}

	/*public void keyPressed(KeyEvent e) 
	{
		System.out.println("ASDFG");
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
	}*/
}

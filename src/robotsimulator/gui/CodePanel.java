package robotsimulator.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

import robotinterpreter.RobotInterpreter;
import robotsimulator.Simulator;

@SuppressWarnings("serial")
public class CodePanel extends JPanel implements ActionListener
{
	Simulator sim;
	JTextArea t = new JTextArea(5, 50);
    JButton openButton, clearButton, executeButton, stopButton;
    SwingWorker<Void, Void> executor;
    JFileChooser fc = new JFileChooser();
    private File file;
    private RobotInterpreter r;
    static public final String newline = "\n";
	
	public CodePanel(int h, Simulator s) 
	{
		sim = s;
		setUpGUI(h);
	}
	
	private void setUpGUI(int h)
	{
		openButton = new JButton("Open a File");
		openButton.addActionListener(this);
		
		clearButton = new JButton("Clear Log");
		clearButton.addActionListener(this);
		
		executeButton = new JButton("Execute");
		executeButton.addActionListener(this);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		
		JPanel buttonPanel = new JPanel(); 
		buttonPanel.add(openButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(executeButton);
		buttonPanel.add(stopButton);
		
		t.setMargin(new Insets(5,5,5,5));
        DefaultCaret caret = (DefaultCaret)t.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    JScrollPane logScrollPane = new JScrollPane(t);
	    
	    add(buttonPanel);
		add(logScrollPane);
		setPreferredSize(new Dimension(h, 200));
	}
	
	public void writeLog(String s)
    {
    	t.append(s);
    }

    public void loadFile()
    {
		try 
		{
			FileReader fr = new FileReader(file);
		    BufferedReader br = new BufferedReader(fr);
		    String line = "";
            String code = "";
            
            while((line = br.readLine()) != null)
            {
                 code += line + newline;
            }
             
            br.close();
            fr.close();
             
            t.setText(null);
            t.append(code);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }

	public void actionPerformed(ActionEvent e) 
    {
		//Handle open button action.
        if (e.getSource() == openButton) 
        {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                file = fc.getSelectedFile();
                //This is where a real application would open the file.
                loadFile();
            } 
        } 
        else if (e.getSource() == clearButton) 
        {
                t.setText(null);
        }
        else if(e.getSource() == executeButton)
        {
 		
            executor = new SwingWorker<Void, Void>()
            {
            	@Override
            	public Void doInBackground()
            	{
            		r = new RobotInterpreter();
            		r.addRobotListener(sim);
            		String code = t.getText();
            		r.load(code);
            		
            		if(r.isReady())
            		{
            			r.execute();
            		}
					return null;
            	}
            	
            	public void done()
            	{
            	}
            };
            executor.execute();

        }
        else if(e.getSource() == stopButton)
        {
        	if(executor != null)
        	{
        		executor.cancel(true);
        	}
            sim.stop();
        }
    }
}

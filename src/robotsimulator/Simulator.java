package robotsimulator;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import robotinterpreter.RobotListener;
import robotsimulator.gui.GUI;
import robotsimulator.gui.MainApplet;
import robotsimulator.gui.SimulatorPanel;
import robotsimulator.robot.Robot;
import robotsimulator.world.World;
import robotsimulator.worldobject.Block;

public class Simulator implements RobotListener 
{
	private GUI gui;
	private MainApplet mainApp;
	
	private World world;
	private Robot robot;
	//@SuppressWarnings("unused")
	private static String newline = "\n";
	
	public int guiWidth = 520 * 2;
	public int guiHeight = 400 * 2;
	int guiFPS = 60;
	public String themeid = "default";
	
	public boolean running = false;
	
	
	public Simulator(MainApplet m)
	{
		/*
		 * SETTING ROBOT PARAMS
		 */
		
		int centerX = 100;
		int centerY = 100;
		int angle = 0;
		robot = new Robot(centerX, centerY, angle, this);

		int sonarLen = 750;
		int fov = 25;

		//THESE SHOULD BE ADDED IN CLOCKWISE STARTING FROM FRONT-LEFT
		robot.addSonar(this, "Front-Left", robot.getX0(), robot.getY0(), sonarLen, 315, fov);
		robot.addSonar(this, "Front", robot.getCenterFrontX(), robot.getCenterFrontY(), sonarLen, 0, fov);
		robot.addSonar(this, "Front-Right", robot.getX1(), robot.getY1(), sonarLen, 45, fov);
		robot.addSonar(this, "Right", robot.getCenterRightX(), robot.getCenterRightY(), sonarLen, 90, fov);
		robot.addSonar(this, "Rear", robot.getCenterRearX(), robot.getCenterRearY(), sonarLen, 180, fov);
		robot.addSonar(this, "Left", robot.getCenterLeftX(), robot.getCenterLeftY(), sonarLen, 270, fov);
			
		/*
		 * SETTING BASIC WORLD PARAMS
		 */
			
		world = new World(guiWidth, guiHeight, this);
		world.setTheme(themeid);
		
		/*
		 * SETTING WORLD CELL TYPES
		 */
	
		//gui = new GUI(guiWidth, guiHeight, guiFPS, this);
		mainApp = m;
		
		//running = true?

	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public GUI getGUI() 
	{
		return gui;
	}
	
	public void addBlock(int w, int h, int x, int y, int a)
	{
		Block b = new Block(w, h, x, y, a, this);
		world.addBlock(b);
	}

	public void driveForward() 
	{
		robot.stop();
		robot.drive('f');
	}

	public void driveBackwards() 
	{
		robot.stop();
		robot.drive('b');		
	}

	public void turnLeft() 
	{
		robot.stop();
		robot.turn('l');
	}

	public void turnRight() 
	{
		robot.stop();
		robot.turn('r');
	}

	public void stop() 
	{
		running = false;
		robot.stop();
		robot.abort();
		//Need to tell interpreter to stop as well
		
	}

	public int getSonarData(int num) 
	{
		return (int) Math.round(robot.getSonarSensor(num).getSensorValue());
	}

	public int getBearing() 
	{
		int angle = ((int) robot.getAngle()) + 90;
		if(angle > 360)
			angle -= 360;
		else if(angle < 0)
			angle += 360;
		return angle;
	}
	
	public void driveDistance(int dist)
	{
		robot.stop();
		robot.drive(dist);
		while(robot.getStatus() != 's') 
		{ 
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{

			}
		}
	}
	
	public void turnAngle(int angle) 
	{
		robot.stop();
		robot.turn(angle);
		while(robot.getStatus() != 's') 
		{ 
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{

			}
		}
	}

	public void turnToBearing(int bearing) 
	{
			robot.stop();

			int curBearing = getBearing();
			int turnAngle = bearing - curBearing;
			
			if(turnAngle > 180)
			{
				turnAngle -= 360;
			}
			else if(turnAngle < -180)
			{
				turnAngle += 360;
			}
			
			turnAngle(turnAngle);
	}

	@Override
	public void print(String s) 
	{
		System.out.print(s);		
	}

	@Override
	public void println(String s) {
		System.out.println(s);		
	
	}

	@Override
	public void error(String var, String e) {
		System.out.println(e);		
	}
	
	//This does not change the theme-- only the loaded maze
	public void importStage(File f)
	{
		try
		{
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(f);
			
			Node root = document.getDocumentElement();
			
			XPathFactory xPathFactory = XPathFactory.newInstance();
		    XPath xpath = xPathFactory.newXPath();
		    
	    	Node guiWidthNode = root.getAttributes().getNamedItem("guiwidth");
	    	Node guiHeightNode = root.getAttributes().getNamedItem("guiheight");
	    	Node themeIDNode = root.getAttributes().getNamedItem("theme");
	    	themeid = themeIDNode.getNodeValue();
		    
		    Node robotNode = ((NodeList)xpath.compile("robot").evaluate(root, XPathConstants.NODESET)).item(0);
		    Node robotXNode = (((NodeList)xpath.compile("x").evaluate(robotNode, XPathConstants.NODESET))).item(0);
		    Node robotYNode = (((NodeList)xpath.compile("y").evaluate(robotNode, XPathConstants.NODESET))).item(0);
		    Node robotANode = (((NodeList)xpath.compile("a").evaluate(robotNode, XPathConstants.NODESET))).item(0);
	
			robot = new Robot(
					(int)Math.round(Double.parseDouble(robotXNode.getTextContent())), 
					(int)Math.round(Double.parseDouble(robotYNode.getTextContent())), 
					(int)Math.round(Double.parseDouble(robotANode.getTextContent())), 
					this
				);
			
				
			//This is now part of the loadout import
		    /*NodeList sonarNodes = ((NodeList)xpath.compile("sonars/sonar").evaluate(robotNode, XPathConstants.NODESET));

		    for(int i = 0; i < sonarNodes.getLength(); i++)
		    {
			    Node sonarTypeNode = (((NodeList)xpath.compile("type").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarNameNode = (((NodeList)xpath.compile("name").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarXNode = (((NodeList)xpath.compile("x").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarYNode = (((NodeList)xpath.compile("y").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarAngleNode = (((NodeList)xpath.compile("angle").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarLengthNode = (((NodeList)xpath.compile("length").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    
			    char sonarType = sonarTypeNode.getTextContent().charAt(0);
			    if(sonarType == 'l')
			    {
					robot.addSonar(
							this, 
							sonarNameNode.getTextContent(), 
							Double.parseDouble(sonarXNode.getTextContent()),
							Double.parseDouble(sonarYNode.getTextContent()),
							Integer.parseInt(sonarLengthNode.getTextContent()),
							Integer.parseInt(sonarAngleNode.getTextContent())
						);

			    }
			    else if(sonarType == 'c')
			    {
				    Node sonarFOVNode = (((NodeList)xpath.compile("fov").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
				    robot.addSonar(
							this, 
							sonarNameNode.getTextContent(), 
							Double.parseDouble(sonarXNode.getTextContent()),
							Double.parseDouble(sonarYNode.getTextContent()),
							Integer.parseInt(sonarLengthNode.getTextContent()),
							Integer.parseInt(sonarAngleNode.getTextContent()),
							Integer.parseInt(sonarFOVNode.getTextContent())
						);
			    }

		    }
		    */
		    
		    Node worldNode = ((NodeList)xpath.compile("world").evaluate(root, XPathConstants.NODESET)).item(0);
		    Node worldGridWidthNode = (((NodeList)xpath.compile("gridwidth").evaluate(worldNode, XPathConstants.NODESET))).item(0);
		    Node worldGridHeighthNode = (((NodeList)xpath.compile("gridheight").evaluate(worldNode, XPathConstants.NODESET))).item(0);
		    
		    world = new World(Integer.parseInt(guiWidthNode.getNodeValue()), Integer.parseInt(guiHeightNode.getNodeValue()), this);
		    world.setGridWidth(Integer.parseInt(worldGridWidthNode.getTextContent()));
		    world.setGridHeight(Integer.parseInt(worldGridHeighthNode.getTextContent()));
			world.setTheme(themeIDNode.getNodeValue());	
			
			NodeList cellNodes = ((NodeList)xpath.compile("cells/cell").evaluate(worldNode, XPathConstants.NODESET));
			for(int i = 0; i < cellNodes.getLength(); i++)
			{
				Node cellXNode = (((NodeList)xpath.compile("x").evaluate(cellNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node cellYNode = (((NodeList)xpath.compile("y").evaluate(cellNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node cellTypeNode = (((NodeList)xpath.compile("celltype").evaluate(cellNodes.item(i), XPathConstants.NODESET))).item(0);
			    
			    world.toggleCell(
			    		(int)Math.floor(Double.parseDouble(cellXNode.getTextContent())),
			    		(int)Math.floor(Double.parseDouble(cellYNode.getTextContent())),
			    		cellTypeNode.getTextContent()
			    	);
			}
			
			//Update the simPanel stage -- might not even be needed. Should auto-update
			//mainApp.simPanel.updateStage(world.getWidth(), world.getHeight());
			
			
			//gui.dispose();
			//gui = new GUI(Integer.parseInt(guiWidthNode.getNodeValue()), Integer.parseInt(guiHeightNode.getNodeValue()), guiFPS, this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void exportStage(File f) 
	{
		try 
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("stage");
			doc.appendChild(rootElement);
			rootElement.setAttribute("guiwidth", Integer.toString(guiWidth));
			rootElement.setAttribute("guiheight", Integer.toString(guiHeight));
			rootElement.setAttribute("theme", themeid);
			
			robot.export(doc);
			world.export(doc);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
	 
			transformer.transform(source, result);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}		
	}
	
	//Resets the stage back to the default, featureless void of white space
	public void resetStage()
	{
		world = new World(guiWidth, guiHeight, this);
		world.setTheme(themeid);		
	}
	
	//Changes the robot's sensor loadout based on the file
	//Doesn't change x/y and angle-- that's stored in the maze
	public void importLoadout(File f)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(f);
			
			Node root = document.getDocumentElement();
			
			XPathFactory xPathFactory = XPathFactory.newInstance();
		    XPath xpath = xPathFactory.newXPath();
		    
		    //Reset the robot, but keep it in the same place and orientation
		    robot = new Robot((int)robot.getCenterX(), (int)robot.getCenterY(), (int)robot.getAngle(), this);
		    
		    NodeList sonarNodes = ((NodeList)xpath.compile("sonars/sonar").evaluate(root, XPathConstants.NODESET));

		    System.out.println("sonar node length: " + sonarNodes.getLength());
		    for(int i = 0; i < sonarNodes.getLength(); i++)
		    {
			    Node sonarTypeNode = (((NodeList)xpath.compile("type").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarNameNode = (((NodeList)xpath.compile("name").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarXNode = (((NodeList)xpath.compile("x").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarYNode = (((NodeList)xpath.compile("y").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarAngleNode = (((NodeList)xpath.compile("angle").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    Node sonarLengthNode = (((NodeList)xpath.compile("length").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
			    
			    char sonarType = sonarTypeNode.getTextContent().charAt(0);
			    if(sonarType == 'l')
			    {
					robot.addSonar(
							this, 
							sonarNameNode.getTextContent(), 
							Double.parseDouble(sonarXNode.getTextContent()),
							Double.parseDouble(sonarYNode.getTextContent()),
							Integer.parseInt(sonarLengthNode.getTextContent()),
							Integer.parseInt(sonarAngleNode.getTextContent())
						);

			    }
			    else if(sonarType == 'c')
			    {
				    Node sonarFOVNode = (((NodeList)xpath.compile("fov").evaluate(sonarNodes.item(i), XPathConstants.NODESET))).item(0);
				    robot.addSonar(
							this, 
							sonarNameNode.getTextContent(), 
							Double.parseDouble(sonarXNode.getTextContent()),
							Double.parseDouble(sonarYNode.getTextContent()),
							Integer.parseInt(sonarLengthNode.getTextContent()),
							Integer.parseInt(sonarAngleNode.getTextContent()),
							Integer.parseInt(sonarFOVNode.getTextContent())
						);
			    }
		    }

		    
		   robot.printSensors();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}

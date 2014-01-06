package robotsimulator.world;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import robotsimulator.RobotSimulator;
import robotsimulator.Simulator;
import robotsimulator.worldobject.Block;

public class World 
{
	private Simulator sim;
	private int width, height, gridWidth, gridHeight;
	
	private Point[][] points;
	private ArrayList<CellType> cellTypes = new ArrayList<CellType>();
	private Map<String, CellTheme> cellThemes = new HashMap<String, CellTheme>();
	private GridSquare[][] grid;
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private Rectangle2D boundary;
	private CellType curCellType;
	
	public World(int w, int h, Simulator s)
	{
		width = w;
		height = h;
		sim = s;
		
		boundary = new Rectangle2D.Double(0, 0, width, height);
		
		if(width <= 0 && height <= 0)
		{
			RobotSimulator.println("World must have a positive width and height!");
			RobotSimulator.halt();
		}
		points = new Point[width][height];
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				points[x][y] = new Point(x, y);
			}
		}
	}
	

	public void setTheme(String themeid) 
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(Simulator.class.getResource("/robotsimulator/themes/" + themeid + "/theme.xml").toString());
			Node root = document.getDocumentElement();
			
			XPathFactory xPathFactory = XPathFactory.newInstance();
		    XPath xpath = xPathFactory.newXPath();
		    
		    XPathExpression gridWidthExp = xpath.compile("gridwidth");
		    Node gridWidthNode = ((NodeList)gridWidthExp.evaluate(root, XPathConstants.NODESET)).item(0);
		    gridWidth = Integer.parseInt(gridWidthNode.getTextContent());
		    
		    XPathExpression gridHeightExp = xpath.compile("gridheight");
		    Node gridHeightNode = ((NodeList)gridHeightExp.evaluate(root, XPathConstants.NODESET)).item(0);
		    gridHeight = Integer.parseInt(gridHeightNode.getTextContent());
		    
		    NodeList celltypes = ((NodeList)xpath.compile("celltypes/celltype").evaluate(root, XPathConstants.NODESET));
		    for(int i = 0; i < celltypes.getLength(); i++)
		    {
		    	Node idNode = celltypes.item(i).getAttributes().getNamedItem("id");
			    Node nameNode = (((NodeList)xpath.compile("name").evaluate(celltypes.item(i), XPathConstants.NODESET))).item(0);
			    Node widthNode = (((NodeList)xpath.compile("width").evaluate(celltypes.item(i), XPathConstants.NODESET))).item(0);
			    Node heightNode = (((NodeList)xpath.compile("height").evaluate(celltypes.item(i), XPathConstants.NODESET))).item(0);
			    Node clipNode = (((NodeList)xpath.compile("clip").evaluate(celltypes.item(i), XPathConstants.NODESET))).item(0);
			    Node colorNode = (((NodeList)xpath.compile("color").evaluate(celltypes.item(i), XPathConstants.NODESET))).item(0);
			    Node imageNode = (((NodeList)xpath.compile("image").evaluate(celltypes.item(i), XPathConstants.NODESET))).item(0);
			    setCellType(
			    		idNode.getNodeValue(), 
						nameNode.getTextContent(), 
						Integer.parseInt(widthNode.getTextContent()), 
						Integer.parseInt(heightNode.getTextContent()), 
						Boolean.parseBoolean(clipNode.getTextContent()),
						Color.decode(colorNode.getTextContent())
					);
			    String path = "/robotsimulator/themes/" + idNode.getNodeValue() + "/" + imageNode.getTextContent();
			    setCellTheme(
			    		idNode.getNodeValue(), 
			    		Simulator.class.getResource("/robotsimulator/themes/" + themeid + "/" + imageNode.getTextContent())
			    	);
		    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		grid = new GridSquare[width / gridWidth][height / gridHeight];
		for(int x = 0; x < width / gridWidth; x++)
		{
			for(int y = 0; y < height / gridHeight; y++)
			{
				grid[x][y] = new GridSquare(x * gridWidth, y * gridHeight, gridWidth, gridHeight, 0);
			}
		}

		/*setCellType("pkmn_wall1", "Wall 1", 1, 1, true, Color.blue);
		setCellType("pkmn_wall2", "Wall 2", 1, 1, true, Color.green);
		world.setCellType("pkmn_wall3", "Wall 3", 1, 1, true, Color.red);
		world.setCellType("pkmn_floor1", "Floor 1", 1, 1, false, Color.blue);
		world.setCellType("pkmn_floor2", "Floor 2", 1, 1, false, Color.black);
		world.setCellType("pkmn_floor3", "Floor 3", 1, 1, false, Color.black);
		
	    world.setCellTheme("pkmn_wall1", Simulator.class.getResource("/robotsimulator/themes/pkmn/wall1.png"));
		world.setCellTheme("pkmn_wall2", Simulator.class.getResource("/robotsimulator/themes/pkmn/wall2.png"));
		world.setCellTheme("pkmn_wall3", Simulator.class.getResource("/robotsimulator/themes/pkmn/wall3.png"));
		world.setCellTheme("pkmn_floor1", Simulator.class.getResource("/robotsimulator/themes/pkmn/floor1.png"));
		world.setCellTheme("pkmn_floor2", Simulator.class.getResource("/robotsimulator/themes/pkmn/floor2.png"));
		world.setCellTheme("pkmn_floor3", Simulator.class.getResource("/robotsimulator/themes/pkmn/floor3.png"));*/
	}
	
	public ArrayList<Block> getBlocks()
	{
		return blocks;
	}
	
	public Point[][] getWorldPoints()
	{
		return points;
	}
	
	public ArrayList<CellType> getCellTypes() 
	{
		return cellTypes;
	}
	
	public void setCellType(String id, String n, int w, int h, boolean cl, Color c) 
	{
		cellTypes.add(new CellType(id, n, w, h, cl, c));
		curCellType = cellTypes.get(0);
	}
	
	public void setCurrentCellType(CellType c) 
	{
		curCellType = c;
	}
	
	public Map<String, CellTheme> getCellThemes() 
	{
		return cellThemes;
	}
	
	public void setCellTheme(String id, URL url) 
	{
		cellThemes.put(id, new CellTheme(id, url));
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getGridWidth()
	{
		return gridWidth;
	}
	
	public int getGridHeight()
	{
		return gridHeight;
	}
	
	public void setGridWidth(int w)
	{
		gridWidth = w;
	}
	
	public void setGridHeight(int h)
	{
		gridHeight = h;
	}
	
	public Rectangle2D getBoundary() 
	{
		return boundary;
	}
	
	public void addBlock(Block b)
	{
		blocks.add(b);
		int x0 = (int) ((b.getCenterX()) - (b.getWidth() / 2));
		int y0 = (int) ((b.getCenterY()) - (b.getHeight() / 2));
		int x1 = (int) (x0 + b.getWidth());
		int y1 = (int) (y0 + b.getHeight());
		
		for(int x = x0; x <= x1; x++)
		{
			for(int y = y0; y <= y1; y++)
			{
				points[x][y].occupy(b);
			}
		}
	}
	
	public void removeBlock(Block b)
	{
		blocks.remove(b);
		int x0 = (int) ((b.getCenterX()) - (b.getWidth() / 2));
		int y0 = (int) ((b.getCenterY()) - (b.getHeight() / 2));
		int x1 = (int) (x0 + b.getWidth());
		int y1 = (int) (y0 + b.getHeight());
		
		for(int x = x0; x <= x1; x++)
		{
			for(int y = y0; y <= y1; y++)
			{
				points[x][y].unOccupy();
			}
		}
	}
	
	public void toggleCell(int x, int y)
	{
		try
		{
			int squareX = x / gridWidth;
			int squareY = y / gridHeight;
			
			boolean occupied = false;
			for(int i = 0; i < curCellType.getWidth(); i++)
			{
				for(int j = 0; j < curCellType.getHeight(); j++)
				{
					if(grid[squareX + i][squareY + j].isOccupied())
						occupied = true;
				}
			}
			
			if(!occupied)
			{
				//Note: we have to add each cell rotated 90 degrees in order for our width / height settings to make sense.
				Cell c = new Cell(squareX * gridWidth, squareY * gridHeight, 90, curCellType, sim);
				for(int i = 0; i < curCellType.getWidth(); i++)
				{
					for(int j = 0; j < curCellType.getHeight(); j++)
					{
						grid[squareX + i][squareY + j].occupy(c);
					}
				}
			
				addBlock(c.getBlock());
			}
			else if(grid[squareX][squareY].isOccupied())
			{
				Cell c = grid[squareX][squareY].getCell();
				Block b = c.getBlock();
				for(int i = 0; i < c.getCellType().getWidth(); i++)
				{
					for(int j = 0; j < c.getCellType().getHeight(); j++)
					{
						grid[squareX + i][squareY + j].unOccupy();
					}
				}
				
				removeBlock(b);
				
				if(c.getCellType().getID() != curCellType.getID())
				{
					toggleCell(x, y);
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	/* This code adapted from Garrett Drown's MazeNav simulator */
	public static ArrayList<Point> getLine(double x1, double y1, double x2, double y2)
    {
		ArrayList<Point> points = new ArrayList<Point>();
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double step = 1.0 / distance;

        for (double progress = 0; progress <= 1; progress += step)
        {
            int posX = (int)(x1 * (1 - progress) + x2 * (progress));
            int posY = (int)(y1 * (1 - progress) + y2 * (progress));
            
            boolean found = false;

            for(Point p : points)
            {
            	if(p.compare(posX, posY))
            	{
            		found = true;
            		break;
            	}
            }
            
            if(!found)
            {
            	points.add(new Point(posX, posY));
            }
        }
        
        return points;
    }

	public void export(BufferedWriter bw)
	{
		int gridWidth = 32;
		int gridHeight = 32;
		
		int guiWidth = 640;
		int guiHeight = 320;
		int guiFPS = 30;
		
		Simulator.expLine("world", bw);
		Simulator.expBreak(bw);
		
		Simulator.expLine("worlddata", bw);
		Simulator.expProp("guiwidth", guiWidth, bw);
		Simulator.expProp("guiheight", guiHeight, bw);
		Simulator.expProp("guifps", guiFPS, bw);

		Simulator.expLine("worlddata end", bw);
		Simulator.expBreak(bw);

		/*exportCellTypes(bw);
		Simulator.expBreak(bw);

		exportCellThemes(bw);
		Simulator.expBreak(bw);*/

		exportCells(bw);
		Simulator.expBreak(bw);
		
		Simulator.expLine("world end", bw);
		Simulator.expBreak(bw);
	}
	
	/*public void exportCellTypes(BufferedWriter bw) 
	{
		Simulator.expLine("celltypes", bw);
		Simulator.expBreak(bw);
		for(CellType c : cellTypes)
		{
			c.export(bw);
			Simulator.expBreak(bw);
		}
		Simulator.expLine("celltypes end", bw);
	}*/
	
	public void exportCells(BufferedWriter bw) 
	{
		Simulator.expLine("celltypes", bw);
		Simulator.expBreak(bw);
		for(CellType c : cellTypes)
		{
			c.export(bw);
			Simulator.expBreak(bw);
		}
		Simulator.expLine("celltypes end", bw);
	}
}

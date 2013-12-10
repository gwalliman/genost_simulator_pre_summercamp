/* List of source files:
 * MainPage.xaml.cs
 * location.cs
 * World.cs 
 * objectInWorld.cs
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.ServiceModel;

using System.Threading;
using SilverlightTest.robotService;

namespace SilverlightTest
{
    //---------------------------------------------------------------------------------------------------------------
    //Class: World
    //This class handles all the data regarding the simulation "World", including it's physical properties, and also
    //handles physics-related calculations as far as boundaries, distances, and blocks are concerned.
    //---------------------------------------------------------------------------------------------------------------
    public class World
    {
        private const int worldSize = 660;
        private static bool[,] theWorld = new bool[worldSize, worldSize];
        private static Rectangle[,] drawingSpace = new Rectangle[worldSize, worldSize];
        private int blockSize = 20;

        public World()
        {
        }

        public int getWorldSize()
        {
            return worldSize;
        }

        public int getBlockSize()
        {
            return blockSize;
        }
        //------------------------------------------------------------------------------------------------------------------------
        //Function: calculatePhysicsLinesForRectangle
        //Parameters:objectInWorld
        //This function calculates the boundaries of the rectangle by calculating the position of the four corners, then calculating
        //the physics lines between each adjacent corner to create the box.
        //------------------------------------------------------------------------------------------------------------------------
        public void calculatePhysicsLinesForRectangle(objectInWorld myObj)
        {
            double startX = Canvas.GetLeft(myObj.rectangle);
            double startY = Canvas.GetTop(myObj.rectangle);
            double height = myObj.rectangle.Height;
            double width = myObj.rectangle.Width;
            double angle = myObj.angle;

            double centerX = startX + (width / 2);
            double centerY = startY + (height / 2);

            double[] x = new double[4];
            double[] y = new double[4];

            // Rectangle:
            // x0,y0    x1,y1
            // x2,y2    x3,y3

            double angleInRadians = Math.PI / 180.0 * angle;

            // Calculate where the corners of the robot are
            x[0] = (startX - centerX) * Math.Cos(angleInRadians) - (startY - centerY) * Math.Sin(angleInRadians) + centerX;
            x[1] = (startX + width - centerX) * Math.Cos(angleInRadians) - (startY - centerY) * Math.Sin(angleInRadians) + centerX;
            x[2] = (startX - centerX) * Math.Cos(angleInRadians) - (startY + height - centerY) * Math.Sin(angleInRadians) + centerX;
            x[3] = (startX + width - centerX) * Math.Cos(angleInRadians) - (startY + height - centerY) * Math.Sin(angleInRadians) + centerX;

            y[0] = (startX - centerX) * Math.Sin(angleInRadians) + (startY - centerY) * Math.Cos(angleInRadians) + centerY;
            y[1] = (startX + width - centerX) * Math.Sin(angleInRadians) + (startY - centerY) * Math.Cos(angleInRadians) + centerY;
            y[2] = (startX - centerX) * Math.Sin(angleInRadians) + (startY + height - centerY) * Math.Cos(angleInRadians) + centerY;
            y[3] = (startX + width - centerX) * Math.Sin(angleInRadians) + (startY + height - centerY) * Math.Cos(angleInRadians) + centerY;

            // These points are to be used in calculating the physics for the world.
            calculatePhysicsLineForTwoPoints(x[0], y[0], x[1], y[1]);
            calculatePhysicsLineForTwoPoints(x[0], y[0], x[2], y[2]);
            calculatePhysicsLineForTwoPoints(x[2], y[2], x[3], y[3]);
            calculatePhysicsLineForTwoPoints(x[1], y[1], x[3], y[3]);
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function:calculatePhysicsLineForTwoPoints
        //Parameters: double, double, double, double (twou points)
        //Calculates the distance between two points, then marks all points in between them (a line) as "occupied".
        //------------------------------------------------------------------------------------------------------------------------
        public void calculatePhysicsLineForTwoPoints(double x1, double y1, double x2, double y2)
        {
            double distance = Math.Sqrt(Math.Pow(x1 - x2, 2.0) + Math.Pow(y1 - y2, 2.0));
            double step = 1.0 / distance;

            // Travel from one point to the other and record that this line is occupied space
            for (double progress = 0; progress <= 1; progress += step)
            {
                int posX = (int)Math.Round((x1 * (1 - progress) + x2 * (progress)));
                int posY = (int)Math.Round((y1 * (1 - progress) + y2 * (progress)));

                // Record that this space/position is occupied
                theWorld[posX, posY] = true;
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function:occupiedBetweenTwoPoints
        //Parameters: double, double, double, double (two points)
        //Checks to see if there exists occupied space between two points, returning true of there is, false if not.
        //------------------------------------------------------------------------------------------------------------------------
        public bool occupiedBetweenTwoPoints(double x1, double y1, double x2, double y2)
        {
            double distance = Math.Sqrt(Math.Pow(x1 - x2, 2.0) + Math.Pow(y1 - y2, 2.0));
            double step = 1.0 / distance;

            // Check between two points for any occupied space.  If it is occupied then return true.
            for (double progress = 0; progress <= 1; progress += step)
            {
                int posX = (int)(x1 * (1 - progress) + x2 * (progress));
                int posY = (int)(y1 * (1 - progress) + y2 * (progress));

                try
                {
                    if (theWorld[posX, posY] == true)
                        // This spot is already occupied
                        return true;
                }
                catch
                {
                    // Probably went outside of the world's boundaries (due to floating point accuracy errors)
                    return true;
                }
            }

            // We didn't find anything occupying this same space.
            return false;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: findNearestObject
        //Parameters: double, double, double, double (two points)
        //This function takes two points (start point and end point, if you will) and travels along the line between the two points
        //checking every space to see if it's occupied. If it is occupied, it returns the location of that occupied space. If not,
        //it will return (-1, -1) by default.
        //------------------------------------------------------------------------------------------------------------------------
        public location findNearestObject(double x1, double y1, double x2, double y2)
        {
            // X1,Y1 = Robot
            // X2,Y2 = something very far away in a particular direction
            // It needs to be far so we don't stop short of finding an object.

            double distance = Math.Sqrt(Math.Pow(x1 - x2, 2.0) + Math.Pow(y1 - y2, 2.0));
            double step = 1.0 / distance;

            for (double progress = 0; progress <= 1; progress += step)
            {
                int posX = (int)(x1 * (1 - progress) + x2 * (progress));
                int posY = (int)(y1 * (1 - progress) + y2 * (progress));

                try
                {
                    if (theWorld[posX, posY] == true)
                    {
                        // This spot is already occupied
                        return new location(posX, posY);
                    }
                }
                catch
                {
                    // Probably went outside of the world's boundaries (due to floating point accuracy errors)

                    // Make sure posX is within the boundaries
                    if (posX < 0)
                        posX = 0;
                    else if (posX >= worldSize)
                        posX = worldSize - 1;

                    // Make sure posY is within the boundaries
                    if (posY < 0)
                        posY = 0;
                    else if (posY >= worldSize)
                        posY = worldSize - 1;

                    return new location(posX, posY);
                }
            }

            return new location(-1, -1);
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: populateWorld
        //This function sets up the physics variables for the world boundary, then updates the UI to display the occupied spaces
        //as colored blue on the screen.
        //------------------------------------------------------------------------------------------------------------------------
        public void populateWorld(Grid LayoutRoot, List<objectInWorld> worldObjects)
        {

            foreach (objectInWorld myObj in worldObjects)
            {
                calculatePhysicsLinesForRectangle(myObj);
            }

            // Fill in rectangles that have been recorded as "populated"
            for (int countX = 0; countX < worldSize; countX++)
                for (int countY = 0; countY < worldSize; countY++)
                    if (theWorld[countX, countY] == true)
                    {
                        drawingSpace[countX, countY] = new Rectangle();

                        drawingSpace[countX, countY].Width = 1;
                        drawingSpace[countX, countY].Height = 1;

                        SolidColorBrush black = new SolidColorBrush(Colors.Blue);
                        drawingSpace[countX, countY].Fill = black;
                        drawingSpace[countX, countY].Margin = new Thickness(countX, countY, 0, 0);

                        drawingSpace[countX, countY].HorizontalAlignment = HorizontalAlignment.Left;
                        drawingSpace[countX, countY].VerticalAlignment = VerticalAlignment.Top;

                        LayoutRoot.Children.Add(drawingSpace[countX, countY]);
                    }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: toggleBlock
        //This function either removes an already-occupied block, or creates a block in an empty space in the UI.
        //------------------------------------------------------------------------------------------------------------------------
        public void toggleBlock(Grid LayoutRoot, int xPos, int yPos)
        {
            if (drawingSpace[xPos, yPos] != null)
            {
                LayoutRoot.Children.Remove(drawingSpace[xPos, yPos]);
                drawingSpace[xPos, yPos] = null;  // Garbage collection will clean this up.


                // It is already occupied.  Toggle the block to be unoccupied.
                for (int x = xPos; x < xPos + blockSize; x++)
                    for (int y = yPos; y < yPos + blockSize; y++)
                        theWorld[x, y] = false;
            }
            else
            {
                drawingSpace[xPos, yPos] = new Rectangle();

                drawingSpace[xPos, yPos].Width = blockSize;
                drawingSpace[xPos, yPos].Height = blockSize;

                SolidColorBrush black = new SolidColorBrush(Colors.Blue);
                drawingSpace[xPos, yPos].Fill = black;
                drawingSpace[xPos, yPos].Margin = new Thickness(xPos, yPos, 0, 0);

                drawingSpace[xPos, yPos].HorizontalAlignment = HorizontalAlignment.Left;
                drawingSpace[xPos, yPos].VerticalAlignment = VerticalAlignment.Top;

                LayoutRoot.Children.Add(drawingSpace[xPos, yPos]);


                // It is unoccupied.  Toggle the block to be occupied.
                for (int x = xPos; x < xPos + blockSize; x++)
                    for (int y = yPos; y < yPos + blockSize; y++)
                        theWorld[x, y] = true;
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: toggleBlockNumber
        //Parameters: int, int (block location)
        //This function either removes an already-occupied block, or creates a block in an empty space in the UI at location
        //(blockX, blockY)
        //------------------------------------------------------------------------------------------------------------------------
        public void toggleBlockNumber(Grid LayoutRoot, int blockX, int blockY)
        {
            toggleBlock(LayoutRoot, blockX * blockSize, blockY * blockSize);
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: defaultTestSetup
        //Initializes the simulation to have a default maze and a default robot location/orientation
        //------------------------------------------------------------------------------------------------------------------------
        public void defaultTestSetup(Grid LayoutRoot, Image theRobot, ref int robotRotation)
        {

            // Start the robot rotated 270 degrees so that it is looking to the left.
            RotateTransform test = new RotateTransform();
            test.CenterX = theRobot.Width / 2;
            test.CenterY = theRobot.Height / 2;
            test.Angle = 270;
            robotRotation = Convert.ToInt32(test.Angle + 0.07); // Add a small arbitrary value to avoid precision-error

            theRobot.RenderTransform = test;

            toggleBlockNumber(LayoutRoot, 5, 4);
            toggleBlockNumber(LayoutRoot, 6, 4);
            toggleBlockNumber(LayoutRoot, 7, 4);
            toggleBlockNumber(LayoutRoot, 8, 4);
            toggleBlockNumber(LayoutRoot, 9, 4);
            //toggleBlockNumber(LayoutRoot,10, 4);
            toggleBlockNumber(LayoutRoot, 5, 5);

            toggleBlockNumber(LayoutRoot, 5, 6);
            toggleBlockNumber(LayoutRoot, 5, 7);
            toggleBlockNumber(LayoutRoot, 5, 8);
            toggleBlockNumber(LayoutRoot, 5, 9);
            toggleBlockNumber(LayoutRoot, 5, 10);

            toggleBlockNumber(LayoutRoot, 6, 10);
            toggleBlockNumber(LayoutRoot, 7, 10);
            toggleBlockNumber(LayoutRoot, 8, 10);
            toggleBlockNumber(LayoutRoot, 9, 10);
            toggleBlockNumber(LayoutRoot, 10, 10);
            toggleBlockNumber(LayoutRoot, 11, 10);
            toggleBlockNumber(LayoutRoot, 12, 10);
            toggleBlockNumber(LayoutRoot, 13, 10);
            toggleBlockNumber(LayoutRoot, 14, 10);
            toggleBlockNumber(LayoutRoot, 15, 10);
            toggleBlockNumber(LayoutRoot, 16, 10);
            toggleBlockNumber(LayoutRoot, 17, 10);
            toggleBlockNumber(LayoutRoot, 18, 10);
            toggleBlockNumber(LayoutRoot, 19, 10);
            toggleBlockNumber(LayoutRoot, 20, 10);
            toggleBlockNumber(LayoutRoot, 21, 10);
            toggleBlockNumber(LayoutRoot, 22, 10);
            toggleBlockNumber(LayoutRoot, 23, 10);
            toggleBlockNumber(LayoutRoot, 24, 10);
            toggleBlockNumber(LayoutRoot, 25, 10);
            toggleBlockNumber(LayoutRoot, 26, 10);

            toggleBlockNumber(LayoutRoot, 15, 5);
            toggleBlockNumber(LayoutRoot, 15, 6);
            toggleBlockNumber(LayoutRoot, 15, 7);
            toggleBlockNumber(LayoutRoot, 15, 8);
            toggleBlockNumber(LayoutRoot, 15, 9);

            toggleBlockNumber(LayoutRoot, 21, 0);
            toggleBlockNumber(LayoutRoot, 21, 0);   // Because of outer wall...
            toggleBlockNumber(LayoutRoot, 21, 1);
            toggleBlockNumber(LayoutRoot, 21, 2);
            toggleBlockNumber(LayoutRoot, 21, 3);
            toggleBlockNumber(LayoutRoot, 21, 4);

            toggleBlockNumber(LayoutRoot, 27, 5);
            toggleBlockNumber(LayoutRoot, 27, 6);
            toggleBlockNumber(LayoutRoot, 27, 7);
            toggleBlockNumber(LayoutRoot, 27, 8);
            toggleBlockNumber(LayoutRoot, 27, 9);
            toggleBlockNumber(LayoutRoot, 27, 10);

            toggleBlockNumber(LayoutRoot, 10, 11);
            toggleBlockNumber(LayoutRoot, 10, 12);
            toggleBlockNumber(LayoutRoot, 10, 13);
            toggleBlockNumber(LayoutRoot, 10, 14);

            toggleBlockNumber(LayoutRoot, 0, 15);
            toggleBlockNumber(LayoutRoot, 0, 15);   // Because of outer wall...
            toggleBlockNumber(LayoutRoot, 1, 15);
            toggleBlockNumber(LayoutRoot, 2, 15);
            toggleBlockNumber(LayoutRoot, 3, 15);
            toggleBlockNumber(LayoutRoot, 4, 15);
            toggleBlockNumber(LayoutRoot, 5, 15);
            toggleBlockNumber(LayoutRoot, 6, 15);
            toggleBlockNumber(LayoutRoot, 7, 15);
            toggleBlockNumber(LayoutRoot, 8, 15);
            toggleBlockNumber(LayoutRoot, 9, 15);
            toggleBlockNumber(LayoutRoot, 10, 15);
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: differenceInAngles
        //Parameters: double, double
        //This function returns the shortest distance between the two given angle parameters.
        //------------------------------------------------------------------------------------------------------------------------
        public double differenceInAngles(double fromAngle, double toAngle)
        {
            // Return shortest distance between angles

            double result = toAngle - fromAngle;
            if (result > 180)
                result -= 360;
            else if (result < -180)
                result += 360;

            return result;
        }
    }
}

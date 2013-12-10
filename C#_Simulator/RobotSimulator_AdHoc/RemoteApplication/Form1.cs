using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

using System.Threading;
using System.Windows;

// CSE459
using System.Diagnostics;
using System.IO;

namespace RemoteApplication
{
    public partial class Form1 : Form
    {
        static robotSimulator.ServiceClient simulator;
        private Thread runner;
        private Thread autonomous;
        //private Thread myCSE459Thread;
        public delegate void invokeDelegate();
        private bool autonomousActive = false;
        string currentHeading = "west";

        public Form1()
        {
            InitializeComponent();

            // Connect to the web service.
            simulator = new robotSimulator.ServiceClient();

            // Start a new thread to regularly check for sensor updates
            runner = new Thread(updateSensors);
            runner.Start();

            //Thread.Sleep(3000);

            // Start by staying stopped.
            simulator.setMotion("stop");

            
        }

        private void updateSensors()
        {
            // Sleep to avoid crashing when starting (the UI must start first)
            System.Threading.Thread.Sleep(500);

            while (true)
            {
                // Wait a period of time between updates
                System.Threading.Thread.Sleep(50);

                // Update the sensor and compass labels.
                try
                {
                    BeginInvoke(new invokeDelegate(delegate()
                    {
                        double[] sensorData = new double[5];
                        sensorData = simulator.getSensorUpdate();

                        frontSensorLabel.Text = sensorData[0].ToString();
                        leftSensorLabel.Text = sensorData[1].ToString();
                        rightSensorLabel.Text = sensorData[2].ToString();
                        rearSensorLabel.Text = sensorData[3].ToString();
                        compassLabel.Text = sensorData[4].ToString();

                    }));
                }
                catch
                {
                    ;
                }
            }
        }

        private void forwardButton_Click(object sender, EventArgs e)
        {
            simulator.setMotion("forward");
            autonomousActive = false;
        }

        private void reverseButton_Click(object sender, EventArgs e)
        {
            simulator.setMotion("reverse");
            autonomousActive = false;
        }

        private void leftButton_Click(object sender, EventArgs e)
        {
            simulator.setMotion("left");
            autonomousActive = false;
        }

        private void rightButton_Click(object sender, EventArgs e)
        {
            simulator.setMotion("right");
            autonomousActive = false;
        }

        private void stopButton_Click(object sender, EventArgs e)
        {
            simulator.setMotion("stop");
            autonomousActive = false;
        }

        private void autonomousButton_Click(object sender, EventArgs e)
        {
            autonomousActive = true;
            autonomous = new Thread(autonomousMotion);
            autonomous.Start();
        }

        double wantedAngle = 270;
        private void autonomousMotion()
        {
            // Right-wall following algorithm.
            //      Foward
            //      if(canTurnRight): turn right
            //      if(cannotGoForward): turn left

            // The robot starts at a 270 degree angle (i.e. facing to the left).
            wantedAngle = 270;
            double frontSensor = -1;
            double rightSensor = -1;
            double compass = -1;
            double difference = -1;

            // Start off by going forward
            simulator.setMotion("forward");
            // Continue doing this until the autonomous mode is disabled
            while (autonomousActive)
            {
                frontSensor = Convert.ToDouble(frontSensorLabel.Text);
                rightSensor = Convert.ToDouble(rightSensorLabel.Text);
                

                
                // Maze algorithm
                if (rightSensor >= 80)
                {
                    simulator.setMotion("forward");
                    Thread.Sleep(1000);
                    turnRight90();
                    Thread.Sleep(500);
                    simulator.setMotion("forward");
                    Thread.Sleep(2000);
                }
                else if (frontSensor <= 45)
                {
                    turnLeft90();
                    Thread.Sleep(500);
                    simulator.setMotion("forward");
                }
                
                compass = Convert.ToDouble(compassLabel.Text);
                difference = differenceInAngles(compass, wantedAngle);

                if (difference < -2)
                {
                    simulator.setMotion("left");
                    Thread.Sleep(50);
                    simulator.setMotion("forward");
                    Thread.Sleep(100);
                }
                else if (difference > 2)
                {
                    simulator.setMotion("right");
                    Thread.Sleep(50);
                    simulator.setMotion("forward");
                    Thread.Sleep(100);
                }

            }
        }

        private void turnLeft90()
        {
            double originalCompassReading = Convert.ToDouble(compassLabel.Text);
            //double newDirection = originalCompassReading - 90;
            double newDirection = wantedAngle - 87;     // Due to the delay, 87 degrees makes it make really nice 90 degree turns.
            if(newDirection < 0)
                newDirection += 360;

            simulator.setMotion("left");

            double newReading = -500;
            while (true)
            {
                try
                {
                    // This command sometimes (very rarely) fails when receiving data from the web service.
                    newReading = Convert.ToDouble(compassLabel.Text);
                }
                catch
                {
                    // Do nothing.  The next udpate should update this.
                    ;
                }

                // Check to make sure we got a good reading.  If not then try again.
                if (newReading == -500)
                    continue;

                if (newReading < 0)
                    newReading += 360;

                if (differenceInAngles(newReading, newDirection) >= -3)
                {
                    simulator.setMotion("stop");
                    break;
                }
            }

            //simulator.setMotion("stop");
            wantedAngle -= 90;
            if (wantedAngle < 0)
                wantedAngle += 360;
        }

        private void turnRight90()
        {
            double originalCompassReading = Convert.ToDouble(compassLabel.Text);
            //double newDirection = originalCompassReading + 90;
            double newDirection = wantedAngle + 87;     // Due to the delay, 87 degrees makes it make really nice 90 degree turns.
            if (newDirection >= 360)
                newDirection -= 360;

            simulator.setMotion("right");

            double newReading = -500;
            while (true)
            {
                try
                {
                    // This command sometimes (very rarely) fails when receiving data from the web service.
                    newReading = Convert.ToDouble(compassLabel.Text);
                }
                catch
                {
                    // Do nothing.  The next udpate should update this.
                    ;
                }

                // Check to make sure we got a good reading.  If not then try again.
                if (newReading == -500)
                    continue;

                if (newReading >= 360)
                    newReading -= 360;

                if (differenceInAngles(newReading, newDirection) <= 3)
                {
                    simulator.setMotion("stop");
                    break;
                }
            }

            //simulator.setMotion("stop");
            wantedAngle += 90;
            if (wantedAngle > 360)
                wantedAngle -= 360;
        }

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

        public void cse459()
        {
            string movesRequired = "N/A";
            for (int x = 0; x < 100; x++)
            {
                movesRequired = runClingo(x);
                if (movesRequired != "N/A")
                    break;
            }

            mazePos[] parsedMoves = parseResults(movesRequired);
            string[] movesForMaze = translateMoves(parsedMoves);

            // Execute the commands
            sendCommandsToRobot(movesForMaze);     
        }

        private void sendCommandsToRobot(string[] movesForMaze)
        {
            foreach (string move in movesForMaze)
            {
                if (move == "forward")
                {
                    simulator.setMotion("forward");
                    Thread.Sleep(1800);
                    simulator.setMotion("stop");
                }
                else if (move == "turnLeft90")
                    turnLeft90();
                else if (move == "turnRight90")
                    turnRight90();
                //else
                // FIXME: Throw an exception here
                // If we get here, then most likely the maze is too large and movesForMaze == "N/A"

                Thread.Sleep(500);
            }
            simulator.setMotion("stop");
        }

        private string runClingo(int moves)
        {
            Process clingo = new Process();
            clingo.StartInfo.UseShellExecute = false;
            clingo.StartInfo.RedirectStandardOutput = true;
            //clingo.StartInfo.RedirectStandardError = true;
            clingo.StartInfo.CreateNoWindow = true;
            clingo.StartInfo.WorkingDirectory = @"C:\Users\Garrett\Documents\My Dropbox\School\clingo-3.0.4-win32";
            clingo.StartInfo.FileName = "\"C:\\Users\\Garrett\\Documents\\My Dropbox\\School\\clingo-3.0.4-win32\\clingo.exe\"";

            clingo.StartInfo.Arguments = "-c m=" + moves.ToString() + " Project.txt 0";

            clingo.Start();
            //string errors = clingo.StandardError.ReadToEnd();
            string output = clingo.StandardOutput.ReadToEnd();
            clingo.WaitForExit();

            if (output.IndexOf("UNSATISFIABLE") == -1)
                // We have found the correct number of moves required
                return output;
            else
                return "N/A";
            
            /*
            int x = 0;
            while (true)
            {
                x++;
            }*/
        }

        private mazePos[] parseResults(string moves)
        {
            string[] parsedMoves;

            parsedMoves = moves.Split('\r', '\n');

            string listOfMoves = "";
            for (int x = 0; x < parsedMoves.Length; x++)
            {
                if (parsedMoves[x].IndexOf("move") != -1)
                    listOfMoves = parsedMoves[x].Trim();
            }

            string[] allMoves = listOfMoves.Split(' ');

            //mazePos[] eachMove = new mazePos[(listOfMoves.Length - 4)/11];  // "\r\n" is 4 chars and each "move(x,y,z)" is 11 chars.
            //mazePos[] eachMove = new mazePos[(listOfMoves.Length + 1) / 12];  // each "move(x,y,z)" is 12 chars.  Added 1 more because there is no space at the end for the last move(...).
            int numOfMoves = 1;  // Start at 1 because the last move does not have a space after it.
            for(int x = 0; x < listOfMoves.Length; x++)
            {
                if(listOfMoves[x] == ' ')
                    numOfMoves++;
            }
            mazePos[] eachMove = new mazePos[numOfMoves];
            int numProcessed = 0;
            foreach (string moveString in allMoves)
            {
                string parsedMove = moveString.Substring(5);
                parsedMove = parsedMove.TrimEnd(')');

                string[] moveComponents = parsedMove.Split(',');
                eachMove[numProcessed] = new mazePos();
                eachMove[numProcessed].x = Int32.Parse(moveComponents[0]);
                eachMove[numProcessed].y = Int32.Parse(moveComponents[1]);
                eachMove[numProcessed].time = Int32.Parse(moveComponents[2]);
                numProcessed++;
            }

            return eachMove;
        }

        private string[] translateMoves(mazePos[] moves)
        {
            mazePos temp = new mazePos();
            bool changed = true;

            while (changed)
            {
                changed = false;

                for (int x = 0; x < moves.Length - 1; x++)
                {
                    if (moves[x].time > moves[x + 1].time)
                    {
                        temp = moves[x + 1];
                        moves[x + 1] = moves[x];
                        moves[x] = temp;

                        changed = true;
                    }
                }
            }

            
            // Convert the "moves" into "commands"
            //string currentHeading = "west";     // IDEA: Have it assume that the first move is already the direction it is heading?  Can use this to get its bearings.
            string newHeading = "";
            List<string> commands = new List<string>();

            for(int x = 0; x < moves.Length - 1; x++)
            {

                // Find in which direction it is moving
                if (moves[x].x == moves[x+1].x)
                {
                    // Moving horizontally (x is constant --> y is changing)
                    if (moves[x + 1].y < moves[x].y)
                        newHeading = "north";
                    else
                        newHeading = "south";
                        
                }
                else
                {
                    // Moving vertically (y is constant --> x is changing)
                    if (moves[x + 1].x < moves[x].x)
                        newHeading = "west";
                    else
                        newHeading = "east";
                    
                }

                // Find what command(s) is needed to carry out this movement
                if(newHeading == currentHeading)
                    commands.Add("forward");
                    // currentHeading = "forward";
                else
                {
                    int currentH = headingToInt(currentHeading);
                    int newH = headingToInt(newHeading);

                    if(currentH == 0 && newH == 3)
                    {
                        commands.Add("turnLeft90");
                        commands.Add("forward");
                        currentHeading = intHeadingToString(3);
                    }
                    else if(currentH == 3 && newH == 0)
                    {
                        commands.Add("turnRight90");
                        commands.Add("forward");
                        currentHeading = intHeadingToString(0);
                    }
                    else if(currentH < newH)
                    {
                        commands.Add("turnRight90");
                        commands.Add("forward");
                        currentHeading = intHeadingToString(newH);
                    }
                    else // newH < currentH
                    {
                        commands.Add("turnLeft90");
                        commands.Add("forward");
                        currentHeading = intHeadingToString(newH);
                    }
                }


            }

            string[] results = new string[commands.Count];
            for(int x = 0; x < commands.Count; x++)
            {
                results[x] = commands[x];
            }

            return results;

        }

        public int headingToInt(string heading)
        {
            if (heading == "north")
                return 0;
            if (heading == "east")
                return 1;
            if (heading == "south")
                return 2;
            else
                return 3; // west
        }

        public string intHeadingToString(int heading)
        {
            if (heading == 0)
                return "north";
            if (heading == 1)
                return "east";
            if (heading == 2)
                return "south";
            else
                return "west";  // 3 == "west"
        }

        private void button1_Click(object sender, EventArgs e)
        {
            // Uncomment the following to get the maze from the GUI and parse it into a new project .txt file.
            
            string graphicsMaze = mazePictureToText();
            string mazeWalls = parsePictureToWalls(graphicsMaze);
            saveProjectFile(mazeWalls, graphicsMaze);

            // Uncomment the following to just execute Clingo on the .txt file and use it
            /*
            myCSE459Thread = new Thread(cse459);
            myCSE459Thread.Start();*/
        }

        private string mazePictureToText()
        {
            string graphicMaze = "N/A";

            while(graphicMaze == "N/A")
                graphicMaze = simulator.receiveMessage();

            return graphicMaze;
        }

        private string parsePictureToWalls(string maze)
        {
            int width = maze.IndexOf('\n');
            string[] rows = maze.Split('\n');
            int height = rows.Length - 1;

            List<string> mazeWalls = new List<string>();

            for (int y = 0; y < height; y++) 
            {
                for (int x = 0; x < width; x++)
                {
                    if(rows[y][x] == '1')
                        // x and y are incremented by 1 because coordinates start at (1,1) and not (0,0).
                        mazeWalls.Add("wall(" + (x+1).ToString() + "," + (y+1).ToString() + ").\n");
                }
            }

            string singleStringOfMazeWalls = "";
            foreach (string x in mazeWalls)
                singleStringOfMazeWalls += x;

            return singleStringOfMazeWalls;
        }

        string startPosition = "move(3, 5, 1).";
        string exitPosition = "exit(11, 4).";
        string textFilePath = @"C:\Users\Garrett\Documents\My Dropbox\School\clingo-3.0.4-win32\silverlightCreatedProject.txt";
        private void saveProjectFile(string mazeWalls, string maze)
        {
            int fileLength = 2 + 2 + mazeWalls.Split('\n').Length + 11;
            string[] eachWall = mazeWalls.Split('\n');

            string[] content = new string[fileLength];

            content[0] = startPosition;
            content[1] = exitPosition;

            int width = maze.IndexOf('\n');
            int height = maze.Split('\n').Length - 1;

            content[2] = "row(1.."+ width.ToString() +").";
            content[3] = "col(1.."+ height.ToString() +").";

            int count = 4;  // We have already added 4 lines to be written to the file
            foreach (string wall in eachWall)
            {
                content[count] = wall;
                count++;
            }

            content[count] = "moves(1..m).";
            content[count+1] = "#domain moves(M).";
            content[count+2] = "1{ move(X, Y, Z) : row(X) : col(Y)}1 :- moves(Z).";
            content[count+3] = ":- move(X1, Y1, Z1), move(X2, Y2, Z2), #abs(X2 - X1) > 1, Z2 == Z1 + 1.  % Horizontal";
            content[count+4] = ":- move(X1, Y1, Z1), move(X2, Y2, Z2), #abs(Y2 - Y1) > 1, Z2 == Z1 + 1.	 % Vertical";
            content[count+5] = ":- move(X1, Y1, Z1), move(X2, Y2, Z2), #abs(Y2 - Y1) == 1, #abs(X2 - X1) == 1, Z2 == Z1 + 1.";
            content[count+6] = ":- move(X, Y, Z), wall(X, Y).";
            content[count+7] = "finished :- move(X1, Y1, Z), exit(X2, Y2), X1 == X2, Y1 == Y2.";
            content[count+8] = ":- not finished.";
            content[count+9] = "#hide.";
            content[count+10] = "#show move/3.";

            System.IO.File.WriteAllLines(textFilePath, content);
        }

    }

    public class mazePos
    {
        public int x;
        public int y;
        public int time;
    }
}

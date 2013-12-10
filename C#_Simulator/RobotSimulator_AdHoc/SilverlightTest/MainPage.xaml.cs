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
using SilverlightTest.motorService;
using SilverlightTest.sonarService;

//using System.Windows.Threading;

// CSE459
//using System.Diagnostics;
//using System.Runtime.InteropServices.Automation;

namespace SilverlightTest
{
    // Ctrl+M -> Ctrl+O to collapse all functions.


    public partial class RobotSimulator : UserControl
    {
        public static string motion = "stop";
        public static ServiceClient robotService;
        public static motorService.MotorServiceClient motor;
        public static sonarService.SonarServiceClient sonar;
        private Thread runner;
        public static List<objectInWorld> worldObjects = new List<objectInWorld>();
        public World simWorld = new World();

        public bool autonomousActive = false;
        private Thread autonomous;

        // Sensor variables
        private int frontDistance = -1;
        private int rearDistance = -1;
        private int leftDistance = -1;
        private int rightDistance = -1;
        private int simRobotRotation = 0;
        // see robotRotation for "compass"

        // Physics-related variables
        //private const int worldSize = 660;
        //private static bool[ , ] theWorld = new bool[worldSize, worldSize];
        //public bool[,] theWorld = new bool[worldSize, worldSize];
        //private static Rectangle[ , ] drawingSpace = new Rectangle[worldSize, worldSize];
        //private const int robotXSize = 2;
        //private const int robotYSize = 2;
        private int robotRotation = 0;
        //private int blockSize = 20;

        // Programming interface related variables
        string[] defaultOptions = new string[] { "Default: Forward", "Default: Reverse", "Default: Left", "Default: Right", "Default: Stop" };
        string[] movementOptions = new string[] { "forward", "reverse", "delayed left 90", "left 90", "delayed right 90", "right 90", "turn 180" };
        string[] ifOptions = new string[] { "if sensor.forward > ", "if sensor.forward < ", "if sensor.reverse > ", "if sensor.reverse < ", 
            "if sensor.left > ", "if sensor.left < ", "if sensor.right > ", "if sensor.right < ", "if sensor.left < sensor.right", 
            "if sensor.left > sensor.right", "[Do nothing]" };
        // string[] comparisonOptions = new string[] { "<", ">", "==" };
        public static List<TextBox> addedTextBoxes = new List<TextBox>();
        public static List<ComboBox> addedDropdowns = new List<ComboBox>();
        const int SPACING = 5;
        const int DROPDOWN_WIDTH = 220;
        const int DROPDOWN_HEIGHT = 23; // Also being used for textbox height
        public bool programmingRoutineActive = false;       // Used to tell the thread that it should or should not be active.
        public bool programmingRoutineShutDown = true;     // Used to determine if the thread has finished stopping itself.
        public bool simulationActive = true;               //Used to determine if the program is in robot simulation or robot control mode
        public bool useActualSensors = false;
        private Thread programmingRoutine;

        public bool robotConnectionEstablished = false;     //Used to record the status of the connection to the physical robot.

        //------------------------------------------------------------------------------------------------------------------------
        //Function: RobotSimulator (constructor)
        //Sets up the simulation, setting up the framerate, web service communication (between robot and simulation), and simulation
        //UI.
        //------------------------------------------------------------------------------------------------------------------------
        public RobotSimulator()
        {
            InitializeComponent();
            // Set the framerate to 30 FPS.
            Application.Current.Host.Settings.MaxFrameRate = 30;

            // Connect to and set up the web service.
            robotService = new ServiceClient();
            robotService.getMotionCompleted += new EventHandler<getMotionCompletedEventArgs>(robotService_getMotionCompleted);
            robotService.getMotionAsync();

            /*broker = new brokerService.Service1SoapClient();
            broker.GetAddressCompleted += new EventHandler<GetAddressCompletedEventArgs>(broker_getAddressCompleted);
            broker.GetAddressAsync(0);*/

            // Create a box around the entire boundary of the simulator area
            Rectangle worldBoundary = new Rectangle();
            worldBoundary.Width = simWorld.getWorldSize() - 2;
            worldBoundary.Height = simWorld.getWorldSize() - 2;
            SolidColorBrush lightGray = new SolidColorBrush(Colors.LightGray);
            worldBoundary.Fill = lightGray;
            worldBoundary.Margin = new Thickness(0, 0, 0, 0);
            worldBoundary.HorizontalAlignment = HorizontalAlignment.Left;
            worldBoundary.VerticalAlignment = VerticalAlignment.Top;
            objectInWorld worldBoundaryObject = new objectInWorld(worldBoundary, 0.0);
            worldObjects.Add(worldBoundaryObject);

            // Calculate all of the physics-related variables for the entire simulated world.
            simWorld.populateWorld(LayoutRoot, worldObjects);  // Now just for the world boundary

            // Start a thread that checks for updates from the web service and sends updates (from the sensors) to the web service.
            runner = new Thread(robotUpdater);
            runner.Start(); 

            // This function initializes the contents of the maze and other pieces in the simulator.
            simWorld.defaultTestSetup(LayoutRoot, theRobot, ref robotRotation);

            // Populate the list of the combobox.
            populateComboBox(defaultActionComboBox, defaultOptions);
            defaultActionComboBox.SelectedIndex = 0;    // By default, the default action is to move forward.
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: populateComboBox
        //Parameters: ComboBox, string[]
        //Initializes the drop-down box (ComboBox) to have the selection options specified in the string array.
        //------------------------------------------------------------------------------------------------------------------------
        private void populateComboBox(ComboBox myComboBox, string[] options)
        {
            for (int count = 0; count < options.Length; count++)
                myComboBox.Items.Add(options[count]);
        }

     

        //------------------------------------------------------------------------------------------------------------------------
        //Function: robotService_getMotionCompleted
        //Parameters: sender (object), getMotionCompletedEventArgs
        //Receives the motion that the web service tells the robot to perform, then executes it on the simulation
        //------------------------------------------------------------------------------------------------------------------------
        void robotService_getMotionCompleted(object sender, getMotionCompletedEventArgs e)
        {
            if (e.Error != null)
            {
                // TODO: Include something like a pop-up to display this error.
                ;
            }
            else
            {
                // Note: The variable "motion" is set in move(X) to X.
                // Once the motion has been received from the web service, execute that motion
                move(e.Result);
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: robotUpdater
        //This function will be called into its own thread, running an infinite loop that constantly asks the web service for the
        //motion to perform (if in autonomous mode). Also constantly updates the sensor data.
        //------------------------------------------------------------------------------------------------------------------------
        private void robotUpdater()
        {
            while (true)
            {
                System.Threading.Thread.Sleep(100);

                Dispatcher.BeginInvoke(delegate()
                {
                    // Only poll Web Service (for "motion") if manual control is disabled
                    if(manualControlCheckBox.IsChecked == false && simulationActive)
                        robotService.getMotionAsync();
                });

                // Always send updates for the sensors
                Dispatcher.BeginInvoke(delegate()
                {       
                    if(!useActualSensors)
                        updateSensors();
                });
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: move
        //Parameters: direction
        //Makes the robot perform the movement in the appropriate "direction", updating the simulation frame with the new location
        //(or actually causing the physical robot to move, when that gets implemented)
        //------------------------------------------------------------------------------------------------------------------------
        public void move(string direction)
        {
            // Configure the animation in Silverlight to do the correct action based on the motion received.

            if (direction == "stop")
            {
                //Check to see if we're controlling the simulated robot or the physical robot
                if (!useActualSensors)
                {
                    CompositionTarget.Rendering -= updateFrame;
                }
                if(!simulationActive) //We're controlling the physical robot
                {
                    try
                    {
                        //ATDL: Make the service call for "stop" from the autobot here
                        motor.motorCommandAsync("Stop", 0, 0);
                    }
                    catch (Exception e)
                    {
                    }
                }
                
                motion = direction; //ATDL WARNING: With this implementation, the simulated robot and physical robot will share this variable,
                                   //           which may cause bugs when switching between the two states.
            }
            else if (motion != direction)  // Check if not already going that same direction
            {
                if (motion != "stop")  // Check if already moving
                {   // It is moving.
                    // Stop moving.
                    //Check to see if we're controlling the simulated or physical robot
                    if (!useActualSensors)
                    {
                        CompositionTarget.Rendering -= updateFrame;
                    }
                    if(!simulationActive) //We're controlling the physical robot
                    {
                        try
                        {
                            //ATDL: Make the service call to move the bot.
                            //Note that if we are turning we need to call the "turn right/left" turn function and not rely on the turn 90 left/right.
                            if (direction.Equals("forward"))
                            {
                                motor.motorCommandAsync("Forwards", 0, 0);
                            }
                            else if (direction.Equals("reverse"))
                            {
                                motor.motorCommandAsync("Backwards", 0, 0);
                            }
                            else if (direction.Equals("right"))
                            {
                                motor.motorCommandAsync("Turn Right", 0, 0);
                            }
                            else if (direction.Equals("left"))
                            {
                                motor.motorCommandAsync("Turn Left", 0, 0);
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }

                    // Move in the new direction.
                    motion = direction;
                    //Check to see if we're controlling the simulated or physical robot
                    if (!useActualSensors)
                    {
                        CompositionTarget.Rendering += updateFrame;
                    }               
                }
                else        // It is already stopped
                {
                    motion = direction;
                    // Move right.
                    if (!useActualSensors)
                    {
                        CompositionTarget.Rendering += updateFrame;
                    }
                    if(!simulationActive) //We're controlling the physical robot
                    {
                        try
                        {
                            //ATDL: Make a function that determines the appropriate call for the robot "move" services.
                            //ATDL: Make the service call to move the bot.
                            //Note that if we are turning we need to call the "turn right/left" turn function and not rely on the turn 90 left/right.
                            if (direction.Equals("forward"))
                            {
                                motor.motorCommandAsync("Forwards", 0, 0);
                            }
                            else if (direction.Equals("reverse"))
                            {
                                motor.motorCommandAsync("Backwards", 0, 0);
                            }
                            else if (direction.Equals("right"))
                            {
                                motor.motorCommandAsync("Turn Right", 0, 0);
                            }
                            else if (direction.Equals("left"))
                            {
                                motor.motorCommandAsync("Turn Left", 0, 0);
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
            // Else: it is already moving the correct direction.  Do nothing.

        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: updateSensors
        //Updates the simulated robot's sensors (compass, too) by calculating the distance between the robot and the nearest object
        //in the appropriate sensor directions, then updates the labels on the website accordingly.
        //------------------------------------------------------------------------------------------------------------------------
        public void updateSensors()
        {
            if (!useActualSensors)
            {
                // *** Update sensors ***
                // Update sonar sensors
                double startX = Canvas.GetLeft(theRobot);
                double startY = Canvas.GetTop(theRobot);
                double height = theRobot.Height;
                double width = theRobot.Width;
                double angle = robotRotation;

                double centerX = startX + (width / 2);
                double centerY = startY + (height / 2);

                double[] x = new double[4];
                double[] y = new double[4];

                // Rectangle:
                //      Front
                //      x0,y0    
                // x1,y1    x2, y2
                //      x3,y3
                //      Rear

                double angleInRadians = Math.PI / 180.0 * angle;
                x[0] = (centerX - centerX) * Math.Cos(angleInRadians) - (startY - centerY) * Math.Sin(angleInRadians) + centerX;
                x[1] = (startX - centerX) * Math.Cos(angleInRadians) - (centerY - centerY) * Math.Sin(angleInRadians) + centerX;
                x[2] = (startX + width - centerX) * Math.Cos(angleInRadians) - (centerY - centerY) * Math.Sin(angleInRadians) + centerX;
                x[3] = (centerX - centerX) * Math.Cos(angleInRadians) - (startY + height - centerY) * Math.Sin(angleInRadians) + centerX;

                y[0] = (centerX - centerX) * Math.Sin(angleInRadians) + (startY - centerY) * Math.Cos(angleInRadians) + centerY;
                y[1] = (startX - centerX) * Math.Sin(angleInRadians) + (centerY - centerY) * Math.Cos(angleInRadians) + centerY;
                y[2] = (startX + width - centerX) * Math.Sin(angleInRadians) + (centerY - centerY) * Math.Cos(angleInRadians) + centerY;
                y[3] = (centerX - centerX) * Math.Sin(angleInRadians) + (startY + height - centerY) * Math.Cos(angleInRadians) + centerY;

                // Amplify these four points so they are beyond the edge of the map (so we will find something...)
                x[0] = x[0] * simWorld.getWorldSize() * Math.Sin(angleInRadians);
                y[0] = y[0] * (0 - simWorld.getWorldSize()) * Math.Cos(angleInRadians);

                x[1] = x[1] * (0 - simWorld.getWorldSize()) * Math.Cos(angleInRadians);
                y[1] = y[1] * (0 - simWorld.getWorldSize()) * Math.Sin(angleInRadians);

                x[2] = x[2] * simWorld.getWorldSize() * Math.Cos(angleInRadians);
                y[2] = y[2] * simWorld.getWorldSize() * Math.Sin(angleInRadians);

                x[3] = x[3] * (0 - simWorld.getWorldSize()) * Math.Sin(angleInRadians);
                y[3] = y[3] * simWorld.getWorldSize() * Math.Cos(angleInRadians);

                location frontSensorLocation = simWorld.findNearestObject(centerX, centerY, x[0], y[0]);
                location leftSensorLocation = simWorld.findNearestObject(centerX, centerY, x[1], y[1]);
                location rightSensorLocation = simWorld.findNearestObject(centerX, centerY, x[2], y[2]);
                location rearSensorLocation = simWorld.findNearestObject(centerX, centerY, x[3], y[3]);

                // FIXME: These distances are calculated from the CENTER of the robot
                // The "+ 0.07" (an arbitrary small positive value) at the end is to fix the precision error of doubles when converting to ints.
                
                frontDistance = (int)(Math.Sqrt(Math.Pow(centerX - frontSensorLocation.x, 2) + Math.Pow(centerY - frontSensorLocation.y, 2)) + 0.07);
                leftDistance = (int)(Math.Sqrt(Math.Pow(centerX - leftSensorLocation.x, 2) + Math.Pow(centerY - leftSensorLocation.y, 2)) + 0.07);
                rightDistance = (int)(Math.Sqrt(Math.Pow(centerX - rightSensorLocation.x, 2) + Math.Pow(centerY - rightSensorLocation.y, 2)) + 0.07);
                rearDistance = (int)(Math.Sqrt(Math.Pow(centerX - rearSensorLocation.x, 2) + Math.Pow(centerY - rearSensorLocation.y, 2)) + 0.07);

                // Display updates on the website's labels
                frontSensorLabel.Content = frontDistance.ToString();
                leftSensorLabel.Content = leftDistance.ToString();
                rightSensorLabel.Content = rightDistance.ToString();
                rearSensorLabel.Content = rearDistance.ToString();

                // Update/display compass reading
                sonarLabel.Content = robotRotation.ToString();
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: updateFrame
        //Parameters: sender (object), EventArgs
        //This function handles the constant updating of the robot's position by calculating the new location for the robot, and
        //also performing the collision detection.
        //------------------------------------------------------------------------------------------------------------------------
        public void updateFrame(object sender, EventArgs e)
        {
            int startY = (int)Math.Round(Canvas.GetTop(theRobot));
            int startX = (int)Math.Round(Canvas.GetLeft(theRobot));
            double width = theRobot.Width;
            double height = theRobot.Height;

            double centerX = startX + (theRobot.Width / 2);
            double centerY = startY + (theRobot.Height / 2);

            double[] x = new double[4];
            double[] y = new double[4];

            // Rectangle:
            // x0,y0    x1,y1
            // x2,y2    x3,y3
            double angleInRadians = Math.PI / 180.0 * robotRotation;

            x[0] = (startX - centerX) * Math.Cos(angleInRadians) - (startY - centerY) * Math.Sin(angleInRadians) + centerX;
            x[1] = (startX + width - centerX) * Math.Cos(angleInRadians) - (startY - centerY) * Math.Sin(angleInRadians) + centerX;
            x[2] = (startX - centerX) * Math.Cos(angleInRadians) - (startY + height - centerY) * Math.Sin(angleInRadians) + centerX;
            x[3] = (startX + width - centerX) * Math.Cos(angleInRadians) - (startY + height - centerY) * Math.Sin(angleInRadians) + centerX;

            y[0] = (startX - centerX) * Math.Sin(angleInRadians) + (startY - centerY) * Math.Cos(angleInRadians) + centerY;
            y[1] = (startX + width - centerX) * Math.Sin(angleInRadians) + (startY - centerY) * Math.Cos(angleInRadians) + centerY;
            y[2] = (startX - centerX) * Math.Sin(angleInRadians) + (startY + height - centerY) * Math.Cos(angleInRadians) + centerY;
            y[3] = (startX + width - centerX) * Math.Sin(angleInRadians) + (startY + height - centerY) * Math.Cos(angleInRadians) + centerY;

            // Check for collision
            // Note: coordinate(0,0) is the top left corner.
            bool topOccupied = simWorld.occupiedBetweenTwoPoints(x[0], y[0] - 1, x[1], y[1] - 1);  // Top
            bool leftOccupied = simWorld.occupiedBetweenTwoPoints(x[0] - 1, y[0], x[2] - 1, y[2]);  // Left
            bool bottomOccupied = simWorld.occupiedBetweenTwoPoints(x[2], y[2] + 1, x[3], y[3] + 1);  // Bottom
            bool rightOccupied = simWorld.occupiedBetweenTwoPoints(x[1] + 1, y[1], x[3] + 1, y[3]);  // Right

            // Check if it is going to run into something...
            if (motion == "forward")
            {
                if (topOccupied)
                    return;
            }
            else if(motion == "reverse")
            {
                if (bottomOccupied)
                    return;
            }
            else if (motion == "left" || motion == "right")
            {
                if (topOccupied)
                    return;
            }


            // TODO: This section could be combined with the previous section by adding "else" statements to the nested if() statements.
            if (motion == "forward")
            {
                Canvas.SetTop(theRobot, Canvas.GetTop(theRobot) - Math.Cos(angleInRadians));
                Canvas.SetLeft(theRobot, Canvas.GetLeft(theRobot) + Math.Sin(angleInRadians));
            }
            else if (motion == "reverse")
            {
                Canvas.SetTop(theRobot, Canvas.GetTop(theRobot) + Math.Cos(angleInRadians));
                Canvas.SetLeft(theRobot, Canvas.GetLeft(theRobot) - Math.Sin(angleInRadians));
            }
            else if (motion == "left")
            {
                RotateTransform newRotation = new RotateTransform();
                newRotation.CenterX = theRobot.Width / 2;
                newRotation.CenterY = theRobot.Height / 2;
                robotRotation--;
                newRotation.Angle = robotRotation;

                theRobot.RenderTransform = newRotation;
            }
            else // (motion == "right")   // There is no "stop" as rendering is disabled when "stop(ped)"
            {
                RotateTransform newRotation = new RotateTransform();
                newRotation.CenterX = theRobot.Width / 2;
                newRotation.CenterY = theRobot.Height / 2;
                robotRotation++;
                newRotation.Angle = robotRotation;

                theRobot.RenderTransform = newRotation;
            }

            if (robotRotation < 0)
                robotRotation += 360;
            if (robotRotation >= 360)
                robotRotation -= 360;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: forwardButton_Click
        //Parameters: sender (object), RoutedEventArgs
        //This function is essentially the button listener for the "forward" button. When it is pressed, this function will set
        //the "manual control" checkbox to checked, set autonomous mode to "false", and then make the robot move forward.
        //------------------------------------------------------------------------------------------------------------------------
        private void forwardButton_Click(object sender, RoutedEventArgs e)
        {
            manualControlCheckBox.IsChecked = true;
            autonomousActive = false;
            programmingRoutineActive = false;
            move("forward");
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: leftButton_Click
        //Parameters: sender (object), RoutedEventArgs
        //This function is essentially the button listener for the "left" button. When it is pressed, this function will set
        //the "manual control" checkbox to checked, set autonomous mode to "false", and then make the robot start turning left.
        //------------------------------------------------------------------------------------------------------------------------
        private void leftButton_Click(object sender, RoutedEventArgs e)
        {
            manualControlCheckBox.IsChecked = true;
            autonomousActive = false;
            programmingRoutineActive = false;
            move("left");
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: leftButton_Click
        //Parameters: sender (object), RoutedEventArgs
        //This function is essentially the button listener for the "right" button. When it is pressed, this function will set
        //the "manual control" checkbox to checked, set autonomous mode to "false", and then make the robot start turning right.
        //------------------------------------------------------------------------------------------------------------------------
        private void rightButton_Click(object sender, RoutedEventArgs e)
        {
            manualControlCheckBox.IsChecked = true;
            autonomousActive = false;
            programmingRoutineActive = false;
            move("right");
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: leftButton_Click
        //Parameters: sender (object), RoutedEventArgs
        //This function is essentially the button listener for the "reverse" button. When it is pressed, this function will set
        //the "manual control" checkbox to checked, set autonomous mode to "false", and then make the robot start moving in reverse.
        //------------------------------------------------------------------------------------------------------------------------
        private void reverseButton_Click(object sender, RoutedEventArgs e)
        {
            manualControlCheckBox.IsChecked = true;
            autonomousActive = false;
            programmingRoutineActive = false;
            move("reverse");
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: stopButton_Click
        //Parameters: sender (object), RoutedEventArgs
        //This function is essentially the button listener for the "right" button. When it is pressed, this function will set
        //the "manual control" checkbox to checked, set autonomous mode to "false", and then make the robot stop moving.
        //------------------------------------------------------------------------------------------------------------------------
        private void stopButton_Click(object sender, RoutedEventArgs e)
        {
            manualControlCheckBox.IsChecked = true;
            autonomousActive = false;
            programmingRoutineActive = false;
            move("stop");
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: autonomousButton_Click
        //Parameters: sender (object), RoutedEventArgs
        //This function is essentially the button listener for the "autonomous" button. When it is pressed, this function will set
        //autonomous mode to "true", and then make the robot traverse the maze by itself (with an already-programmed algorithm,
        //not one set by the user).
        //------------------------------------------------------------------------------------------------------------------------
        private void autonomousButton_Click(object sender, RoutedEventArgs e)
        {
            programmingRoutineActive = false;

            if (!autonomousActive)
            {
                autonomousActive = true;

                autonomous = new Thread(autonomousMotion);
                autonomous.Start();
            }
            

        }
        //ATDL: May need to implement some data reset measures here to ensure data ("motion") remains accurate between switches of simulated robot
        //and physical robot
        private void manualControlCheckBox_Click(object sender, RoutedEventArgs e)
        {
            // Stop moving
            move("stop");

            // If enabled, enable the buttons, else vice versa.
            setButtonEnables();
        }

        private void robotConnectionCheckBox_Click(object sender, RoutedEventArgs e)
        {
            if (robotConnectionCheckBox.IsChecked == true)
            {
                //If connection has not yet been established, establish the connection then switch to robot control mode.
                //If connection cannot be established, uncheck the box and display a message, while resuming normal simulation.
                if (!robotConnectionEstablished)
                {
                    try
                    {
                        //ATDL: Try initializing the connection to the robot, then update the checkbox in some way ("Connected to robot/Not connected", checked/unchecked, etc.)
                        //to display the robot connection status. May want to implement some sort of variable to permanently keep track of this status.
                        //Need to figure out a way to implement a dynamic proxy without using WCFSamples, since it's not compatible with silverlight.
                        //Possible avenues of research: http://www.codeproject.com/Articles/262164/Using-WCF-Service-with-Silverlight
                        //                              http://kozmic.net/2008/12/16/castle-dynamicproxy-tutorial-part-i-introduction/
                        //Set up the phyiscal robot web services
                        motor = new motorService.MotorServiceClient();
                        sonar = new sonarService.SonarServiceClient();
                        motor.setSerialNumCompleted += new EventHandler<setSerialNumCompletedEventArgs>(autobot_setSerialNumCompleted);
                        motor.motorCommandCompleted += new EventHandler<motorCommandCompletedEventArgs>(autobot_motorCommandCompleted);
                        motor.setSerialNumAsync(165045, 164955);
                        robotConnectionCheckBox.Content = "Connecting...";
                        robotConnectionCheckBox.IsEnabled = false;
                    }
                    catch (Exception e2)
                    {
                        robotConnectionCheckBox.Content = "Connection failed.";
                        robotConnectionCheckBox.IsChecked = false;
                    }
                }
                else
                {
                    robotConnectionCheckBox.Content = "Connected!";
                    simulationActive = false;
                    sonar.getSonarDataAsync();
                    motor.getBearingAsync();
                }
            }
            else
            {
                simulationActive = true;
                robotRotation = simRobotRotation;
                if(robotConnectionEstablished)
                    robotConnectionCheckBox.Content = "Reconnect";
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: setButtonEnables
        //This function simply handles the enabling/disabling of the manual control buttons, depending on the status of the manual
        //control checkbox.
        //------------------------------------------------------------------------------------------------------------------------
        private void setButtonEnables()
        {
            // If the checkBox is now checked (manualControl was just set to true)
            if (manualControlCheckBox.IsChecked == true)
            {
                // Enable the buttons
                forwardButton.IsEnabled = true;
                reverseButton.IsEnabled = true;
                leftButton.IsEnabled = true;
                rightButton.IsEnabled = true;
                stopButton.IsEnabled = true;
                autonomousButton.IsEnabled = true;
            }
            else // the checkBox is unchecked (manualControl was just set to false)
            {
                // Disable the buttons
                forwardButton.IsEnabled = false;
                reverseButton.IsEnabled = false;
                leftButton.IsEnabled = false;
                rightButton.IsEnabled = false;
                stopButton.IsEnabled = false;
                autonomousButton.IsEnabled = false;
            }
        }

        double wantedAngle = 270;
        private void autonomousMotion()
        {
            // Right-Wall Following Algorithm
            //      if you can turn right: turn right
            //      if cannot go forward: turn left
            //      otherwise: go forward

            // Farthest Distance Algorithm
            //      if you can go forward: go forward
            //      otherwise:
            //          if left > right: turn left
            //          if right > left: turn right

            // turnLeft90(), turnRight90(), crossThreadMoveCall("forward"), crossThreadMoveCall("reverse"), crossThreadMoveCall("stop")
            // crossThreadSensorRead("[sensor]");  sensor = front, back, left, right, compass
            // Probably need: Thread.Sleep([integer]);        // The number is in milliseconds

            // Examples:
            // TurnRight90();
            // crossThreadMoveCall("forward");
            // crossThreadSensorRead("back");
            // Thread.Sleep(1000);

            wantedAngle = crossThreadSensorRead("compass");
            double frontSensor = -1;
            double rightSensor = -1;
            double compass = -1;
            double difference = -1;

            if (wantedAngle % 90 != 0)
            {
                // wantedAngle should be equal to the closest 90-degree angle
                if (wantedAngle >= (90 - 45) && wantedAngle < (90 + 45))
                    wantedAngle = 90;
                else if (wantedAngle >= (180 - 45) && wantedAngle < (180 + 45))
                    wantedAngle = 180;
                else if (wantedAngle >= (270 - 45) && wantedAngle < (270 + 45))
                    wantedAngle = 270;
                else // if (wantedAngle >= (360-45) && wantedAngle < (0 + 45))
                    wantedAngle = 0;

                // Read the compass
                compass = crossThreadSensorRead("compass");

                difference = simWorld.differenceInAngles(compass, wantedAngle);

                // Turn the robot to the correct angle
                while (Math.Abs(difference) > 2)
                {
                    // Get the angle set up correctly
                    if (difference < -2)
                    {
                        crossThreadMoveCall("left");
                        Thread.Sleep(50);
                        crossThreadMoveCall("stop");
                        Thread.Sleep(100);
                    }
                    else if (difference > 2)
                    {
                        crossThreadMoveCall("right");
                        Thread.Sleep(50);
                        crossThreadMoveCall("stop");
                        Thread.Sleep(100);
                    }

                    compass = crossThreadSensorRead("compass");
                    difference = simWorld.differenceInAngles(compass, wantedAngle);
                }

            }

            // Now continue with the logic for autonomous motion
            // This implements a right-wall following algorithm + drifting correction
            crossThreadMoveCall("forward");
            while (autonomousActive)
            {
                // Read the sensors
                frontSensor = crossThreadSensorRead("front");
                rightSensor = crossThreadSensorRead("right");

                // Maze algorithm
                if (rightSensor >= 80)
                {
                    crossThreadMoveCall("forward");
                    Thread.Sleep(700);
                    turnRight90();
                    Thread.Sleep(500);
                    crossThreadMoveCall("forward");
                    Thread.Sleep(2000);
                }
                else if (frontSensor <= 50)
                {
                    turnLeft90();
                    Thread.Sleep(500);
                    crossThreadMoveCall("forward");
                }

                // Read the compass
                compass = crossThreadSensorRead("compass");
                difference = simWorld.differenceInAngles(compass, wantedAngle);

                // Compensate for drifting toward the walls.
                if (difference < -2)
                {
                    crossThreadMoveCall("left");
                    Thread.Sleep(50);
                    crossThreadMoveCall("forward");
                    Thread.Sleep(100);
                }
                else if (difference > 2)
                {
                    crossThreadMoveCall("right");
                    Thread.Sleep(50);
                    crossThreadMoveCall("forward");
                    Thread.Sleep(100);
                }

            }
            
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: crossThreadMoveCall
        //Parameters: string
        //This function is called by the different threads in order to call the move function, passing the string as the direction
        //to move.
        //------------------------------------------------------------------------------------------------------------------------
        private void crossThreadMoveCall(string direction)
        {
            Dispatcher.BeginInvoke(delegate()
            {
                move(direction);
            });
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: crossThreadSensorRead
        //Parameters: string
        //This function is called by the different threads in order to retrieve the value for the sensor variables. Essentially,
        //is the accessor function for the sensor values.
        //------------------------------------------------------------------------------------------------------------------------
        private int crossThreadSensorRead(string sensor)
        {
            int reading = -100;

            if (sensor == "compass")
            {
                /*
                Dispatcher.BeginInvoke(delegate()
                {
                    reading = Convert.ToDouble(sonarLabel.Content);
                });*/
                reading = robotRotation;
            }

            else if (sensor == "front" || sensor == "forward")
            {             
                /*
                Dispatcher.BeginInvoke(delegate()
                {
                    reading = Convert.ToDouble(frontSensorLabel.Content);
                });*/
                reading = frontDistance;
            }

            else if(sensor == "right")
            {
                /*
                Dispatcher.BeginInvoke(delegate()
                {
                    reading = Convert.ToDouble(rightSensorLabel.Content);
                });*/
                reading = rightDistance;
            }

            else if (sensor == "left")
            {
                /*
                Dispatcher.BeginInvoke(delegate()
                {
                    reading = Convert.ToDouble(leftSensorLabel.Content);
                });*/
                reading = leftDistance;
            }

            else if (sensor == "back" || sensor == "rear" || sensor == "reverse")
            {
                /*
                Dispatcher.BeginInvoke(delegate()
                {
                    reading = Convert.ToDouble(rearSensorLabel.Content);
                });*/
                reading = rearDistance;
            }

            return reading;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: notKillingProgrammingThread
        //This function checks to make sure that the programming thread is either running, or is shut down, not in the middle of
        //switching between the two states.
        //------------------------------------------------------------------------------------------------------------------------
        public bool notKillingProgrammingThread()
        {
            return (programmingRoutineActive && !programmingRoutineShutDown) || (!programmingRoutineActive && programmingRoutineShutDown);
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: turnLeft90
        //This function implements the left 90-degree turn for the robot, and error-checking to ensure that the robot has indeed
        //turned the appropriate amount by comparing the expected direction with readings from the compass, and adjusting (turning)
        //accordingly to fine-tune the final orientation of the robot.
        //------------------------------------------------------------------------------------------------------------------------
        private void turnLeft90()
        {
            int originalCompassReading = 400;

            originalCompassReading = crossThreadSensorRead("compass");
            int newDirection;
            if (useActualSensors)
                newDirection = originalCompassReading - 80;
            else newDirection = originalCompassReading - 90;
            if (newDirection < 0)
                newDirection += 360;

            crossThreadMoveCall("left");

            int newReading = 400;
            while (true && notKillingProgrammingThread())
            {
                newReading = crossThreadSensorRead("compass");

                if (newReading < 0)
                    newReading += 360;

                if (simWorld.differenceInAngles(newReading, newDirection) >= -2)
                    break;
            }

            crossThreadMoveCall("stop");
            wantedAngle -= 90;
            if (wantedAngle < 0)
                wantedAngle += 360;

            // Straighten out to make a perfect 90 degree turn
            //Only care to do this if we are controlling virtual robot.
            if (!useActualSensors)
            {
                newReading = crossThreadSensorRead("compass");
                double difference = simWorld.differenceInAngles(newReading, newDirection);
                while ((difference > 1 || difference < -1) && notKillingProgrammingThread())   // i.e. difference != 0 (for a double)
                {
                    if (difference < 1)
                    {
                        crossThreadMoveCall("left");
                        Thread.Sleep(50);
                        crossThreadMoveCall("stop");
                        Thread.Sleep(50);
                    }
                    else // if (difference > -1)
                    {
                        crossThreadMoveCall("right");
                        Thread.Sleep(50);
                        crossThreadMoveCall("stop");
                        Thread.Sleep(50);
                    }

                    newReading = crossThreadSensorRead("compass");
                    if (newReading >= 360)
                        newReading -= 360;
                    else if (newReading < 0)
                        newReading += 360;
                    difference = simWorld.differenceInAngles(newReading, newDirection);
                }
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: turnRight90
        //This function implements the right 90-degree turn for the robot, and error-checking to ensure that the robot has indeed
        //turned the appropriate amount by comparing the expected direction with readings from the compass, and adjusting (turning)
        //accordingly to fine-tune the final orientation of the robot.
        //------------------------------------------------------------------------------------------------------------------------
        private void turnRight90()
        {
            int originalCompassReading = 400;

            originalCompassReading = crossThreadSensorRead("compass");

            int newDirection;
            if (useActualSensors)
                newDirection = originalCompassReading + 80;
            else newDirection = originalCompassReading + 90;
            if (newDirection >= 360)
                newDirection -= 360;

            crossThreadMoveCall("right");

            int newReading = 400;
            while (true && notKillingProgrammingThread())
            {
                newReading = crossThreadSensorRead("compass");

                if (newReading >= 360)
                    newReading -= 360;

                if (simWorld.differenceInAngles(newReading, newDirection) <= 2)
                    break;
            }

            crossThreadMoveCall("stop");
            wantedAngle += 90;
            if (wantedAngle >= 360)
                wantedAngle -= 360;

            // Straighten out to make a perfect 90 degree turn
            if (!useActualSensors)
            {
                newReading = crossThreadSensorRead("compass");
                double difference = simWorld.differenceInAngles(newReading, newDirection);
                while ((difference > 1 || difference < -1) && notKillingProgrammingThread())   // i.e. difference != 0 (for a double)
                {
                    if (difference < 1)
                    {
                        crossThreadMoveCall("left");
                        Thread.Sleep(50);
                        crossThreadMoveCall("stop");
                        Thread.Sleep(50);
                    }
                    else // if (difference > -1)
                    {
                        crossThreadMoveCall("right");
                        Thread.Sleep(50);
                        crossThreadMoveCall("stop");
                        Thread.Sleep(50);
                    }

                    newReading = crossThreadSensorRead("compass");
                    if (newReading >= 360)
                        newReading -= 360;
                    else if (newReading < 0)
                        newReading += 360;
                    difference = simWorld.differenceInAngles(newReading, newDirection);
                }
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: mouseDownClick_Handler
        //This function is essentially an action handler for mouse-clicks inside the simulation region. It calculates which "block"
        //was clicked, and then toggles the block to its opposite state (occupied/unoccupied).
        //------------------------------------------------------------------------------------------------------------------------
        private void mouseDownClick_Handler(object sender, MouseButtonEventArgs e)
        {
            int mouseXPos = Convert.ToInt32(e.GetPosition(null).X);
            int mouseYPos = Convert.ToInt32(e.GetPosition(null).Y);
            

            // Make sure the mouse-click occurred inside the "playable" region.
            if (mouseXPos < simWorld.getWorldSize() && mouseYPos < simWorld.getWorldSize())
            {


                // For debugging
                textBox1.Text = "X: " + mouseXPos.ToString() + ".  Y: " + mouseYPos.ToString();


                // Determine what "block"/"square" the mouse is in
                int blockX = (mouseXPos / simWorld.getBlockSize());
                int blockY = (mouseYPos / simWorld.getBlockSize());
                int blockXPos = blockX * simWorld.getBlockSize();
                int blockYPos = blockY * simWorld.getBlockSize();

                simWorld.toggleBlockNumber(LayoutRoot, blockX, blockY);

            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: KeyDownHandler
        //This function is the action handler for pressing down a keyboard arrow key. This function will call the appropriate
        //movement function corresponding to the arrow key pressed (up arrow --> forward, left arrow --> turn left, etc.)
        //------------------------------------------------------------------------------------------------------------------------
        private void KeyDownHandler(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Up)
            {
                if (manualControlCheckBox.IsChecked == false)
                {
                    manualControlCheckBox.IsChecked = true;
                    setButtonEnables();
                }
                autonomousActive = false;
                move("forward");
            }
            else if (e.Key == Key.Left)
            {
                if (manualControlCheckBox.IsChecked == false)
                {
                    manualControlCheckBox.IsChecked = true;
                    setButtonEnables();
                }
                autonomousActive = false;
                move("left");
            }
            else if (e.Key == Key.Right)
            {
                if (manualControlCheckBox.IsChecked == false)
                {
                    manualControlCheckBox.IsChecked = true;
                    setButtonEnables();
                }
                autonomousActive = false;
                move("right");
            }
            else if (e.Key == Key.Down)
            {
                if (manualControlCheckBox.IsChecked == false)
                {
                    manualControlCheckBox.IsChecked = true;
                    setButtonEnables();
                }
                autonomousActive = false;
                move("reverse");
            }
            /*else  // If other keys on the keyboard are hit, then the robot will stop.  FIXME ??
            {
                manualControlCheckBox.IsChecked = true;
                setButtonEnables();
                autonomousActive = false;
                move("stop");
            }*/
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: KeyUpHandler
        //This function is the action handler for releasing a keyboard arrow key. This function will call the "stop" function
        //when an arrow key has been released.
        //------------------------------------------------------------------------------------------------------------------------
        private void KeyUpHandler(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Up || e.Key == Key.Left || e.Key == Key.Down || e.Key == Key.Right)
            {
                if (manualControlCheckBox.IsChecked == false)
                {
                    manualControlCheckBox.IsChecked = true;
                    setButtonEnables();
                }
                autonomousActive = false;
                move("stop");
            }
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: addProgrammingIfButton_Click
        //This function is the button listener for the "Add new line" button on the UI. This will create another drop-down menu
        //for adding the "if"/"else-if" statements down the user-programmed algorithm section.
        //------------------------------------------------------------------------------------------------------------------------
        protected void addProgrammingIfButton_Click(object sender, RoutedEventArgs e)
        {
            double topPosition;
            double leftPosition;

            addedDropdowns.Add(new ComboBox());
            addedTextBoxes.Add(new TextBox());

            int newRowNumber = addedDropdowns.Count - 1;

            addedDropdowns[newRowNumber].Width = DROPDOWN_WIDTH;
            addedDropdowns[newRowNumber].Height = DROPDOWN_HEIGHT;
            addedDropdowns[newRowNumber].HorizontalAlignment = HorizontalAlignment.Left;    // HorizontalAlignment.Stretch; 
            addedDropdowns[newRowNumber].VerticalAlignment = VerticalAlignment.Top;         // VerticalAlignment.Stretch;
            populateComboBox(addedDropdowns[newRowNumber], ifOptions);
            // If it is not the first drop down box: make it "else if"...
            if (newRowNumber != 0)
            {
                for (int count = 0; count < addedDropdowns[newRowNumber].Items.Count - 1; count++)  // Do not add it to the very last option (aka "[Do nothing]")
                {
                    addedDropdowns[newRowNumber].Items[count] = "else " + addedDropdowns[newRowNumber].Items[count];
                }
            }
            addedDropdowns[newRowNumber].SelectedIndex = ifOptions.Length - 1;  // The last option is "Do nothing"

            addedTextBoxes[newRowNumber].Width = 40;  // 40 == 4 characters wide
            addedTextBoxes[newRowNumber].Height = DROPDOWN_HEIGHT;  // TextBoxes.Height == DropDown.Height
            addedTextBoxes[newRowNumber].HorizontalAlignment = HorizontalAlignment.Left;
            addedTextBoxes[newRowNumber].VerticalAlignment = VerticalAlignment.Top;
            addedTextBoxes[newRowNumber].Text = "50"; // Arbitrary default value

            topPosition = Canvas.GetTop(defaultActionComboBox) + (newRowNumber + 1) * (defaultActionComboBox.Height + SPACING);
            // The offset is correct for both default's addButton button and if's addButton button.
            leftPosition = Canvas.GetLeft(defaultActionComboBox);

            addedDropdowns[newRowNumber].Margin = new Thickness(leftPosition, topPosition, 0, 0);
            addedTextBoxes[newRowNumber].Margin = new Thickness(leftPosition + DROPDOWN_WIDTH + SPACING, topPosition, 0, 0);

            
            // FIXME: add the following code to the "Add new command for if statement" button (if you want that feature)
                //addedDropdowns[newRowNumber].Click += new RoutedEventHandler(addProgrammingIfButton_Click);

            LayoutRoot.Children.Add(addedDropdowns[newRowNumber]);
            LayoutRoot.Children.Add(addedTextBoxes[newRowNumber]);

            // Add the programming dropdown box to go with this If dropdown box.
            addProgrammingDropdownBox();
            
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: addProgrammingDropdownBox
        //This function handles adding the "Action to be performed" drop-down menu beneath the if-statement in the user programming
        //area.
        //------------------------------------------------------------------------------------------------------------------------
        private void addProgrammingDropdownBox()
        {
            double topPosition;
            double leftPosition;

            addedDropdowns.Add(new ComboBox());
            // Even though this is not used, it leaves the option open for adding textboxes to these dropdown boxes.
            // It also prevents a bug from appearing in the calling function.
            addedTextBoxes.Add(new TextBox());      

            int newRowNumber = addedDropdowns.Count - 1;

            addedDropdowns[newRowNumber].Width = DROPDOWN_WIDTH;
            addedDropdowns[newRowNumber].Height = DROPDOWN_HEIGHT;
            addedDropdowns[newRowNumber].HorizontalAlignment = HorizontalAlignment.Left;    // HorizontalAlignment.Stretch; 
            addedDropdowns[newRowNumber].VerticalAlignment = VerticalAlignment.Top;         // VerticalAlignment.Stretch;
            populateComboBox(addedDropdowns[newRowNumber], movementOptions);
            addedDropdowns[newRowNumber].SelectedIndex = 0;  // Arbitrary default value


            topPosition = addedDropdowns[newRowNumber - 1].Margin.Top + addedDropdowns[newRowNumber - 1].Height + SPACING;
            // The offset is correct for both default's addButton button and if's addButton button.
            leftPosition = addedDropdowns[newRowNumber - 1].Margin.Left + 50;

            addedDropdowns[newRowNumber].Margin = new Thickness(leftPosition, topPosition, 0, 0);

            LayoutRoot.Children.Add(addedDropdowns[newRowNumber]);
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: executeProgrammingButton_Click
        //This function is the button listener for the "execute" button on the UI in the user programming area. If the user is not
        //already running a programming routine, create another thread to handle the programming routine.
        //------------------------------------------------------------------------------------------------------------------------
        private void executeProgrammingButton_Click(object sender, RoutedEventArgs e)
        {
            manualControlCheckBox.IsChecked = true;
            setButtonEnables();
            autonomousActive = false;
            
            // Check all addedTextBoxes to make sure all inputs are valid.
            int tester;
            for (int count = 0; count < addedTextBoxes.Count; count += 2)
            {
                try
                {
                    tester = Convert.ToInt32(addedTextBoxes[count].Text);
                }
                catch
                {
                    // If there is a problem, mark it as "INValid" and then stop
                    addedTextBoxes[count].Text = "INV";
                    return;
                }
            }

            // If it's not already running, start executing the user's program
            if (!programmingRoutineActive)
            {
                programmingRoutineActive = true;

                programmingRoutine = new Thread(programmingThread);
                programmingRoutine.Start();
            }

        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: getCrossThreadValue
        //Paremeter: object
        //This function, while seemingly broad, actually is only called to return the number of added drop-down menus in the user
        //programming section in the "programmingThread" function.
        //------------------------------------------------------------------------------------------------------------------------
        private object getCrossThreadValue(object anObject)
        {
            object theObject = null;

            Dispatcher.BeginInvoke(delegate()
            {
                theObject = anObject;
            });
            while (theObject == null)
                Thread.Sleep(50);

            return theObject;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: getCrossThreadComboBoxValue
        //Parameter: ComboBox
        //This function returns the value located in the ComboBox that is passed to the function. This function is used to get the
        //string located in any ComboBox used in the UI, including the "if" drop-downs and the "then" drop-downs.
        //------------------------------------------------------------------------------------------------------------------------
        private string getCrossThreadComboBoxValue(ComboBox box)
        {
            string theValue = "-1";

            Dispatcher.BeginInvoke(delegate()
            {
                theValue = box.SelectedValue.ToString();
            });
            while (theValue == "-1")
                Thread.Sleep(50);

            return theValue;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: getCrossThreadTextBoxValue
        //Parameter: TextBox
        //This function returns the value located in the TextBox that is passed to the function. This function is used to get the
        //string located in text field located to the right of the "if" ComboBox-es.
        //------------------------------------------------------------------------------------------------------------------------
        private string getCrossThreadTextBoxValue(TextBox box)
        {
            string theValue = "-1";

            Dispatcher.BeginInvoke(delegate()
            {
                theValue = box.Text;
            });
            while (theValue == "-1")
                Thread.Sleep(50);

            return theValue;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: programmingThread
        //This function is the actual logical implementation of the user's programmed algorithm. It parses all the data located in
        //the user's ComboBox'es, then keeps checking each of the conditions sequentially until one is satisfied, then executes the
        //appropriate robot command / function call, before continuing the checking process once again.
        //------------------------------------------------------------------------------------------------------------------------
        private void programmingThread()
        {
            // ** LOADING **
            string comboBoxValue, nextComboBoxValue, performedOp = "";
            int textBoxValue;
            bool performedDelayedOp, performedAnOp;

            int addedDropdownsCount = (int)getCrossThreadValue(addedDropdowns.Count);
            string defaultActionComboBoxValue = getCrossThreadComboBoxValue(defaultActionComboBox);

            programmingRoutineShutDown = false;
            // *** DONE LOADING ***

            while (programmingRoutineActive)
            {
                /* string[] ifOptions = new string[] { "if sensor.forward > ", "if sensor.forward < ", "if sensor.reverse > ", "if sensor.reverse < ", 
                    "if sensor.left > ", "if sensor.left < ", "if sensor.right > ", "if sensor.right < ", "if sensor.left < sensor.right", 
                    "if sensor.left > sensor.right", "[Do nothing]" }; */
                performedDelayedOp = false;
                performedAnOp = false;

                for (int eachDD = 0; eachDD < addedDropdowns.Count && programmingRoutineActive; eachDD += 2)
                {
                    comboBoxValue = getCrossThreadComboBoxValue(addedDropdowns[eachDD]);    // *if*-then
                    nextComboBoxValue = getCrossThreadComboBoxValue(addedDropdowns[eachDD + 1]);    // if-*then*
                    textBoxValue = Convert.ToInt32(getCrossThreadTextBoxValue(addedTextBoxes[eachDD]));

                    if (comboBoxValue.Contains("else "))    // Check if this is an "else if ..." statement
                        comboBoxValue = comboBoxValue.Substring(5); // Remove the "else " when checking what it is selected

                    // "if sensor.forward > ", "if sensor.forward < "
                    if (comboBoxValue == ifOptions[0])
                    {
                        if (crossThreadSensorRead("front") > textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    else if (comboBoxValue == ifOptions[1])
                    {
                        if (crossThreadSensorRead("front") < textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    // "if sensor.reverse > ", "if sensor.reverse < "
                    else if (comboBoxValue == ifOptions[2])
                    {
                        if (crossThreadSensorRead("rear") > textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    else if (comboBoxValue == ifOptions[3])
                    {
                        if (crossThreadSensorRead("rear") < textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    // "if sensor.left > ", "if sensor.left < "
                    else if (comboBoxValue == ifOptions[4])
                    {
                        if (crossThreadSensorRead("left") > textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    else if (comboBoxValue == ifOptions[5])
                    {
                        if (crossThreadSensorRead("left") < textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    // "if sensor.right > ", "if sensor.right < "
                    else if (comboBoxValue == ifOptions[6])
                    {
                        if (crossThreadSensorRead("right") > textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    else if (comboBoxValue == ifOptions[7])
                    {
                        if (crossThreadSensorRead("right") < textBoxValue)
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    // "if sensor.left < sensor.right", "if sensor.left > sensor.right"
                    else if (comboBoxValue == ifOptions[8])
                    {
                        if (crossThreadSensorRead("left") < crossThreadSensorRead("right"))
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    else if (comboBoxValue == ifOptions[9])
                    {
                        if (crossThreadSensorRead("left") > crossThreadSensorRead("right"))
                        {
                            performedDelayedOp = performMovement(nextComboBoxValue);
                            performedAnOp = true;
                            performedOp = nextComboBoxValue;
                            break;
                        }
                    }
                    // "[Do nothing]"

                } // End for(each AddedDropdown)

                // Check if we performed an operation... if we did not then do the default action.  (Assuming the programming routing is still active)
                // string[] defaultOptions = new string[] { "Default: Forward", "Default: Reverse", "Default: Left", "Default: Right", "Default: Stop" };
                if (!performedAnOp && programmingRoutineActive)
                {
                    /*
                    if (defaultActionComboBoxValue == "Default: Forward")
                        crossThreadMoveCall("forward");
                    else if (defaultActionComboBoxValue == "Default: Reverse")
                        crossThreadMoveCall("reverse");
                    else if (defaultActionComboBoxValue == "Default: Left")
                        crossThreadMoveCall("left");
                    else if (defaultActionComboBoxValue == "Default: Right")
                        crossThreadMoveCall("right");
                    else //if (defaultActionComboBoxValue == "Default: Stop")
                        crossThreadMoveCall("stop");*/
                    performDefaultAction(defaultActionComboBoxValue);
                }

                // Delayed operations are used for turning corners.  As such, an added delay is required to make sure 
                // the robot fully exits the hallway it was in before checking its sensors again.
                if (performedDelayedOp && programmingRoutineActive)
                {
                    double sensorReading = -1;
                    string relevantSensor = "";

                    if (performedOp == "delayed left 90")
                        relevantSensor = "left";
                    else // "delayed right 90"
                        relevantSensor = "right";

                    sensorReading = crossThreadSensorRead(relevantSensor);

                    performDefaultAction(defaultActionComboBoxValue);
                    Thread.Sleep(2000);
                    
                    /*while (sensorReading == crossThreadSensorRead(relevantSensor) && programmingRoutineActive)
                        Thread.Sleep(100);

                    // Continue a little bit further
                    if (programmingRoutineActive)
                        Thread.Sleep(200);*/
                }
                else if (programmingRoutineActive)
                    Thread.Sleep(500);  // Delay to prevent the while() loop in this thread from consuming all computing resources.
                else
                    break;      // Stop if !programmingRoutineActive

            } // End while() loop

            programmingRoutineShutDown = true;

        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: performDefaultAction
        //Parameter: string
        //This function performs the appropriate robot function call that corresponds to the value given by the passed string
        //parameter, which was taken from the value located in the "Default" combo box.
        //------------------------------------------------------------------------------------------------------------------------
        private void performDefaultAction(string defaultActionComboBoxValue)
        {
            if (defaultActionComboBoxValue == "Default: Forward")
                crossThreadMoveCall("forward");
            else if (defaultActionComboBoxValue == "Default: Reverse")
                crossThreadMoveCall("reverse");
            else if (defaultActionComboBoxValue == "Default: Left")
                crossThreadMoveCall("left");
            else if (defaultActionComboBoxValue == "Default: Right")
                crossThreadMoveCall("right");
            else //if (defaultActionComboBoxValue == "Default: Stop")
                crossThreadMoveCall("stop");
        }
        //------------------------------------------------------------------------------------------------------------------------
        //Function: performMovement
        //Parameter: string
        //This function parses the command given in the "then" part of the user-defined algorithm, then calls the appropriate robot
        //function to perform the behavior. Returns true if it performed a delayedOp (an operation that includes a delay)
        //------------------------------------------------------------------------------------------------------------------------
        private bool performMovement(string nextComboBoxValue)
        {
            // string[] movementOptions = new string[] { "forward", "reverse", "delayed left 90", "left 90", "delayed right 90", "right 90", "turn 180" };
            int delay = 600;
            bool performedDelayedOp = false;

            // Check if the programming routine is still active.  If it is not, then stop immediately.
            if (programmingRoutineActive == false)
                return performedDelayedOp;

            if (nextComboBoxValue == movementOptions[0])
                crossThreadMoveCall("forward");
            else if (nextComboBoxValue == movementOptions[1])
                crossThreadMoveCall("reverse");
            else if (nextComboBoxValue == movementOptions[2])       // Delayed-left
            {
                Thread.Sleep(delay);     // FIXME!!! Need to add something to compensate for changes in "speed" and framerate (slower computers).
                if(programmingRoutineActive)    // Make sure the programming routine is still active.
                    turnLeft90();
                performedDelayedOp = true;
            }
            else if (nextComboBoxValue == movementOptions[3])
                turnLeft90();
            else if (nextComboBoxValue == movementOptions[4])       // Delayed-right
            {
                Thread.Sleep(delay);     // FIXME!!! Need to add something to compensate for changes in "speed" and framerate (slower computers).
                if (programmingRoutineActive)    // Make sure the programming routine is still active.
                    turnRight90();

                performedDelayedOp = true;
            }
            else if (nextComboBoxValue == movementOptions[5])
                turnRight90();
            else if (nextComboBoxValue == movementOptions[6])
            {
                if (programmingRoutineActive)    // Make sure the programming routine is still active.
                    turnLeft90();
                if (programmingRoutineActive)    // Make sure the programming routine is still active.
                    turnLeft90();
            }

            return performedDelayedOp;
        }

        //------------------------------------------------------------------------------------------------------------------------
        //Function: resetButton_Click
        //This function is essentially the button listener for the resetButton. When clicked, all settings are set back to the
        //initial state, all autonomous/user-defined routines are stopped, and the robot is issued the "stop" command. Then, the
        //robot is moved back to its initial position.
        //------------------------------------------------------------------------------------------------------------------------
        private void resetButton_Click(object sender, RoutedEventArgs e)
        {
            // Reset everything back to their initial state.
            if (manualControlCheckBox.IsChecked == false)
            {
                manualControlCheckBox.IsChecked = true;
                setButtonEnables();
            }
            // Disable the reset button while everything is being reset.
            resetButton.IsEnabled = false;

            // Stop everything...
            autonomousActive = false;
            programmingRoutineActive = false;

            while (programmingRoutineShutDown == false)
                Thread.Sleep(100);

            // Now that the programmingRoutine has stopped sending instructions to the robot, we can tell the robot to stop.
            move("stop");

            // Move the robot back to its initial position.
            Canvas.SetTop(theRobot, 243);
            Canvas.SetLeft(theRobot, 113);

            RotateTransform newRotation = new RotateTransform();
            newRotation.CenterX = theRobot.Width / 2;
            newRotation.CenterY = theRobot.Height / 2;
            robotRotation = 270;
            newRotation.Angle = robotRotation;
            theRobot.RenderTransform = newRotation;

            // Very rare race condition can occur.  Re-send the "stop" command, just in case, to help reduce the risk.
            Thread.Sleep(100);
            move("stop");

            // Re-enable the reset button.
            resetButton.IsEnabled = true;

            

        }

        /*Async Operations*/
        private void autobot_setSerialNumCompleted(object sender, setSerialNumCompletedEventArgs e)
        {
            robotConnectionCheckBox.IsEnabled = true;
            robotConnectionCheckBox.Content = "Connected!";
            robotConnectionEstablished = true;
            simulationActive = false;
            simRobotRotation = robotRotation;

            sonar.getSonarDataCompleted += new EventHandler<getSonarDataCompletedEventArgs>(autobot_getSonarDataCompleted);
            motor.getBearingCompleted += new EventHandler<getBearingCompletedEventArgs>(autobot_getBearingCompleted);
            sonar.getSonarDataAsync();
            motor.getBearingAsync();
        }

        private void autobot_getSonarDataCompleted(object sender, getSonarDataCompletedEventArgs e)
        {
            if (!simulationActive && useActualSensors)
            {
                //We wrap in trycatch because we may go from physical to simulated while waiting to hear back from robot.
                try
                {
                    frontDistance = (int)e.Result[1];
                    leftDistance = (int)e.Result[3];
                    rightDistance = (int)e.Result[5];
                    rearDistance = (int)e.Result[4];

                    // Display updates on the website's labels
                    frontSensorLabel.Content = frontDistance.ToString();
                    leftSensorLabel.Content = leftDistance.ToString();
                    rightSensorLabel.Content = rightDistance.ToString();
                    rearSensorLabel.Content = rearDistance.ToString();

                    sonar.getSonarDataAsync();
                }
                catch (Exception e2)
                {
                }
            }
        }

        private void autobot_getBearingCompleted(object sender, getBearingCompletedEventArgs e)
        {
            if (!simulationActive && useActualSensors)
            {
                if (e.Result >= 0 && e.Result < 360)
                    robotRotation = (int)e.Result;
                sonarLabel.Content = robotRotation.ToString();
                motor.getBearingAsync();
            }
        }

        private void autobot_motorCommandCompleted(object sender, motorCommandCompletedEventArgs e)
        {
            int asdf = 0;
        }

        private void useActualSonars_Click(object sender, RoutedEventArgs e)
        {
            if (useActualSonars.IsChecked == true)
            {
                simRobotRotation = robotRotation;
                useActualSensors = true;
                if (!simulationActive)
                {
                    sonar.getSonarDataAsync();
                    motor.getBearingAsync();
                }
            }
            else
            {
                robotRotation = simRobotRotation;
                useActualSensors = false;
            }
        }



        //-----------------------------------------UNUSED FUNCTIONS-------------------------------------------------//



        ////------------------------------------------------------------------------------------------------------------------------
        ////Function: clearWorld
        ////This function marks all points in the world as unoccupied, then removes all objects from the simulated world
        ////------------------------------------------------------------------------------------------------------------------------
        //public void clearWorld()
        //{
        //    // Remove all recorded physical objects from the simulated world.

        //    for (int x = 0; x < worldSize; x++)
        //        for (int y = 0; y < worldSize; y++)
        //        {
        //            theWorld[x, y] = false;

        //            if (drawingSpace[x, y] != null)
        //            {
        //                LayoutRoot.Children.Remove(drawingSpace[x, y]);
        //                drawingSpace[x, y] = null;  // Garbage collection will clean this up.
        //            }
        //        }
        //}

        /*
        private void mazePictureToText()
        {
            int numRows = 5;
            int numColumns = 11;
            int mazeXBorder = worldSize - 62;
            int mazeYBorder = 250;
            int laneSize = 100;
            int safety = 15;

            bool[,] walls = new bool[numColumns, numRows];

            for (int x = 0; x < mazeXBorder; x += (laneSize / 2))
            {
                for (int y = 0; y < mazeYBorder; y += (laneSize / 2))
                {
                    bool occupied = false;

                    // Search for occupied space...
                    for (int x2 = x + safety; x2 < x+(laneSize-safety); x2++)
                    {
                        for (int y2 = y + safety; y2 < y+(laneSize-safety); y2++)
                        {
                            // Anything beyond the edge of the world is to be considered a wall
                            if (x2 >= worldSize || y2 >= worldSize)
                            {
                                occupied = true;
                                break;
                            }

                            // Check if this spot is occupied.
                            if (theWorld[x2, y2])
                            {
                                occupied = true;
                                break;
                            }
                        }
                        if (occupied)
                            break;
                    }
                    if (occupied)
                        walls[x/(laneSize/2), y/(laneSize/2)] = true;
                }
            }

            string[] mazeRows = new string[numRows];

            for (int wallY = 0; wallY < numRows; wallY++)
            {
                mazeRows[wallY] = new string('1', 1);
                mazeRows[wallY] = "";

                for (int wallX = 0; wallX < numColumns; wallX++)
                {
                    if (walls[wallX, wallY])
                        //mazeRows[wallY] += " X ";   // Occupied
                        mazeRows[wallY] += "1";   // Occupied
                    else
                        //mazeRows[wallY] += " --";   // Not occupied
                        mazeRows[wallY] += "0";   // Not occupied
                }
                mazeRows[wallY] += "\n";
            }

            string combinedMaze = "";
            foreach(string mazeRow in mazeRows)
                combinedMaze += mazeRow;

            // Send the result to the web service to be retrieved by the client application.
            robotService.sendMessageAsync(combinedMaze);
        }
        */
    }
}

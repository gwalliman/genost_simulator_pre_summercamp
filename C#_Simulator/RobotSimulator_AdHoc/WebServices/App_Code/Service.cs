using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;


// NOTE: You can use the "Rename" command on the "Refactor" context-menu to change the class name "Service" in code, svc and config file together.
[ServiceBehavior(IncludeExceptionDetailInFaults = true, InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
public class Service : IService
{

    #region IService Members

    protected static string motion = "stop";
    protected static double frontSensor = -1.0;
    protected static double leftSensor = -1.0;
    protected static double rightSensor = -1.0;
    protected static double rearSensor = -1.0;
    protected static double compass = -1.0;
    List<string> messages = new List<string>();

    public void sendMessage(string newMsg)
    {
        messages.Add(newMsg);
    }

    public string receiveMessage()
    {
        if (messages.Count == 0)
            return "N/A";
        else
        {
            string nextMsg = messages[0];
            messages.RemoveAt(0);
            return nextMsg;
        }
    }

    public string getMotion()
    {
        return motion;
    }

    public void setMotion(string newMotion)
    {
        motion = newMotion;
    }

    public double[] getSensorUpdate()
    {
        double[] data = new double[5];
        data[0] = frontSensor;
        data[1] = leftSensor;
        data[2] = rightSensor;
        data[3] = rearSensor;
        data[4] = compass;
        return data;
    }

    public void updateSensor(double front, double left, double right, double rear, double newCompass)
    {
        // Sensors = "front", "left", "right", "rear", "compass"

        frontSensor = front;
        leftSensor = left;
        rightSensor = right;
        rearSensor = rear;
        compass = newCompass;

    }

    #endregion

}

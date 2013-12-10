using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;

//using System.Web.Services;

// NOTE: You can use the "Rename" command on the "Refactor" menu to change the interface name "IService" in both code and config file together.
//[ServiceContract(SessionMode=SessionMode.Required)]
[ServiceContract]
public interface IService
{
    [OperationContract]
    string getMotion();

    [OperationContract]
    void setMotion(string newMotion);

    [OperationContract]
    double[] getSensorUpdate();

    [OperationContract]
    void updateSensor(double front, double left, double right, double rear, double newCompass);

    [OperationContract]
    void sendMessage(string newMsg);

    [OperationContract]
    string receiveMessage();

}


/* List of source files:
 * MainPage.xaml.cs
 * location.cs
 * World.cs 
 * objectInWorld.cs
*/

using System;
using System.Windows.Shapes;


namespace SilverlightTest
{
    public class objectInWorld
    {
        public Rectangle rectangle;
        public double angle;

        public objectInWorld(Rectangle theRectangle, double theAngle)
        {
            rectangle = theRectangle;
            angle = theAngle;
        }
    }
}
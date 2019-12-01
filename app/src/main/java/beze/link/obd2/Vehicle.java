package beze.link.obd2;

import com.hypertrack.hyperlog.HyperLog;

import java.util.HashMap;
import java.util.Map;

import beze.link.Globals;
import beze.link.obd2.cables.Cable;

public class Vehicle
{
    public static final String TAG = Globals.TAG_BASE + "Vehicle";

    /// <summary>
    /// The Vehicle Identification Number for the vehicle.
    /// </summary>
    public String VIN;

    /// <summary>
    /// The manufacturer of the vehicle, as determined by the VIN.
    /// </summary>
    public String Manufacturer;

    /// <summary>
    /// The year of the vehicle, as determined by the VIN.
    /// </summary>
    public int Year;

    /// <summary>
    /// The model of the vehicle. This value may be supplied or left empty.
    /// </summary>
    public String Model;

    public HashMap<Integer, String> Controllers;

    private Map<String, String> wmis;

    public Vehicle(String vin, Map<String, String> wmis)
    {
        Controllers = new HashMap<>();

        // set the private members first
        this.wmis = wmis;

        // this needs to be assigned first so other properties can be populated from it
        this.VIN = vin.toUpperCase();

        // populate other properties that depend on the vin
        this.Manufacturer = GetManufacturer();
        this.Year = GetYear();
    }

    @Override
    public String toString()
    {
        return
                "VIN: " + this.VIN                      + "\r\n" +
                "Year: " + Integer.toString(this.Year)  + "\r\n" +
                "Make: " + this.Manufacturer            + "\r\n" +
                "Model: " + ((this.Model != null) ? this.Model : "");
    }

    /// <summary>
    /// Converts the 10th VIN digit into the manufacturing year.
    /// </summary>
    /// <returns> The year of manufacturer as denoted by the VIN. </returns>
    /// <remarks>
    /// This value starts at 1996 for valid 10th VIN digits.
    /// </remarks>
    private int GetYear()
    {
        if (this.VIN != null && !this.VIN.isEmpty() && this.VIN.length() >= 10)
        {
            switch (this.VIN.charAt(9))
            {
                // OBD2 did not exist prior to 1996 so they could not be using an elm327 scanner to read the vin
                case 'T': return 1996; //break;
                case 'V': return 1997; //break;
                case 'W': return 1998; //break;
                case 'X': return 1999; //break;
                case 'Y': return 2000; //break;
                case '1': return 2001; //break;
                case '2': return 2002; //break;
                case '3': return 2003; //break;
                case '4': return 2004; //break;
                case '5': return 2005; //break;
                case '6': return 2006; //break;
                case '7': return 2007; //break;
                case '8': return 2008; //break;
                case '9': return 2009; //break;
                case 'A': return 2010; //break;
                case 'B': return 2011; //break;
                case 'C': return 2012; //break;
                case 'D': return 2013; //break;
                case 'E': return 2014; //break;
                case 'F': return 2015; //break;
                case 'G': return 2016; //break;
                case 'H': return 2017; //break;
                case 'J': return 2018; //break;
                case 'K': return 2019; //break;
                default: return 0; //break;
            }
        }

        return 0;
    }

    /// <summary>
    /// Gets the manufacturer as denoted by the VIN.
    /// </summary>
    /// <returns> The manufacturer's long name. </returns>
    /// <remarks>
    /// This value is determined from the supplied IniFile <see cref="wmis"/> parameter from the constructor.
    /// </remarks>
    private String GetManufacturer()
    {
        if (this.VIN != null && !this.VIN.isEmpty() && this.VIN.length() >= 3)
        {
            String longwmi = this.VIN.substring(0, 3);
            String shortwmi = this.VIN.substring(0, 2);

            if (wmis.containsKey(longwmi))
            {
                return (String) wmis.get(longwmi);
            }
            else if (wmis.containsKey(shortwmi))
            {
                return (String) wmis.get(shortwmi);
            }
            else
            {
                return "Unknown";
            }
        }

        return "Unknown (Invalid VIN)";
    }

    private void DiscoverControllers(Cable cable)
    {
        ParameterIdentification discoveryPid = new ParameterIdentification(
                "Available PIDs 01",
                (byte)1,
                (short)0,
                (byte)4,
                "",
                "",
                "",
                "",
                "",
                "");

        if (cable.Protocol == Protocols.Protocol.J1850)
        {
            HyperLog.v(TAG, "Discovering J1850 controllers...");

            discoveryPid.Header = Protocols.J1850.Headers.TCM;
            if (cable.Communicate(discoveryPid) != null)
            {
                // TODO: save this controller
            }

            discoveryPid.Header = Protocols.J1850.Headers.BCM;
            if (cable.Communicate(discoveryPid) != null)
            {
                // TODO: save this controller
            }

            discoveryPid.Header = Protocols.J1850.Headers.PCM;
            if (cable.Communicate(discoveryPid) != null)
            {
                // TODO: save this controller
            }

            discoveryPid.Header = Protocols.J1850.Headers.Default;
            if (cable.Communicate(discoveryPid) != null)
            {
                // TODO: save this controller
            }

            discoveryPid.Header = Protocols.J1850.Headers.AirBag;
            if (cable.Communicate(discoveryPid) != null)
            {
                // TODO: save this controller
            }

            discoveryPid.Header = Protocols.J1850.Headers.ABS;
            if (cable.Communicate(discoveryPid) != null)
            {
                // TODO: save this controller
            }
        }
        else if (Protocols.IsCan(cable.Protocol))
        {
            HyperLog.v(TAG, "Discovering CANBUS controllers...");
            // TODO: test each of the 07E0 through 07E7 controllers
        }
    }

}

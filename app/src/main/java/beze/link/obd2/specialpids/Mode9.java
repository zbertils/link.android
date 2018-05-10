package beze.link.obd2.specialpids;

import android.util.Log;

import beze.link.Globals;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.Protocols;
import beze.link.obd2.cables.Cable;

public class Mode9 extends ParameterIdentification
{
    private static final String TAG = Globals.TAG + "Mode9";

    /// <summary>
/// Instantiates a new instance of the Elm327 Mode9 class. This class should not be used on its own.
/// </summary>
    public Mode9()
    {
        super(
                "Diagnostic Trouble Code Request",
                (byte) 0x09,
                (short) 0x02,
                (byte) 0x01,
                "",
                "",
                "DTC",
                "Diagnostic",
                "",
                "");
    }

    @Override
    public byte PacketSize()
    {
        return 0x01;
    }

    @Override
    public String Pack()
    {
        return String.format("%02X %02X", Mode, PID);
    }

    public String RequestVIN(Cable cable)
    {
        String dataStr = cable.Communicate(this);
        String[] dataLines = ParameterIdentification.PrepareResponseString(dataStr);
        String vin = "";

        if (dataLines != null && dataLines.length > 0)
        {
            dataLines = prepMarkedLines(dataLines);
            for (String line : dataLines)
            {
                int[] dataBytes = ParameterIdentification.ParseStringValues(line);
                if (dataBytes.length >= 7)
                {
                    byte receivedMode = (byte) (dataBytes[0] - 0x40);
                    if (receivedMode == this.Mode)
                    {
                        // first byte is the mode, second the pid, third the line number,
                        // and fourth or index 3 is the start of vin data
                        for (int i = 3; i < dataBytes.length; i++)
                        {
                            if (dataBytes[i] != 0)
                            {
                                vin += (char) dataBytes[i];
                            }
                        }
                    }
                    else
                    {
//                    link.DiagnosticLogger.Write("Cannot decode VIN, expected mode " + this.Mode + " and received " + receivedMode);
                        Log.e(TAG, "RequestVIN: cannot decode VIN, expected mode " + this.Mode + " and received " + receivedMode);
                        return null;
                    }
                }
                else
                {
//                link.DiagnosticLogger.Write("Cannot decode VIN, expected at least 7 characters in line \"" + line + "\" and received " + dataBytes.Length);
                    Log.e(TAG, "RequestVIN: cannot decode VIN, expected at least 7 characters in line \"" + line + "\" and received " + dataBytes.length);
                    return null;
                }
            }
        }

        return vin;
    }

    @Override
    public String SimulatedResponse(Protocols.Protocol type)
    {
        if (type == Protocols.Protocol.J1850)
        {

            return
                    "49 02 01 00 00 00 31" + Protocols.Elm327.EndOfLine +
                    "49 02 02 47 43 48 4B" + Protocols.Elm327.EndOfLine +
                    "49 02 03 33 33 32 38" + Protocols.Elm327.EndOfLine +
                    "49 02 04 37 31 34 32" + Protocols.Elm327.EndOfLine +
                    "49 02 05 30 38 32 36" + Protocols.Elm327.EndOfLine +
                    Protocols.Elm327.Prompt;
        }
        else
        {

            return
                    "0: 49 02 01 00 00 00 31" + Protocols.Elm327.EndOfLine +
                    "1: 02 47 43 48 4B" + Protocols.Elm327.EndOfLine +
                    "2: 03 33 33 32 38" + Protocols.Elm327.EndOfLine +
                    "3: 04 37 31 34 32" + Protocols.Elm327.EndOfLine +
                    "4: 05 30 38 32 36" + Protocols.Elm327.EndOfLine +
                    Protocols.Elm327.Prompt;
        }
    }
}

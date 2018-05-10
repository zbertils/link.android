package beze.link.obd2.specialpids;


import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.DtcParameterIdentification;
import beze.link.obd2.Protocols;

public class Mode3 extends DtcParameterIdentification
{

    /// <summary>
/// Instantiates a new instance of the Elm327 Mode3 class. This class should not be used on its own.
/// </summary>
    public Mode3()
    {
        super(
                DiagnosticTroubleCode.CodeType.Set,
                "Diagnostic Trouble Code Request",
                (byte)0x03,
                (short)0x55,
                (byte)0x01,
                "",
                "",
                "DTC",
                "Diagnostic");
    }

    @Override
    public byte PacketSize() { return 0x01; }

    @Override
    public String SimulatedResponse(Protocols.Protocol type)
    {
        if (type == Protocols.Protocol.J1850)
        {
            // this simulation is for ISO based protocols only, CAN may or may not
            // have an extra byte after the mode describing how many codes there are
            return "43 01 76 02 56 45 10" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }
        else
        {

            // simulation for CAN related vehicles with only one packets-worth of codes
            //        return "43 03 01 76 02 56 45 10";

            // simulation for CAN related to vehicles with multiple packets-worth of codes
            // where each line is marked with an identifier
            return "43 00 AA AA AA AA AA " + Protocols.Elm327.EndOfLine + // purposeful bad line
                    "010 " + Protocols.Elm327.EndOfLine + // purposeful bad line
                    "0: 43 07 00 97 01 02 " + Protocols.Elm327.EndOfLine + // start of trouble codes
                    "1: 01 13 11 01 11 C2 22 " + Protocols.Elm327.EndOfLine +
                    " 2: 27 22 28 AA AA AA AA" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }
    }

}

package beze.link.obd2.specialpids;

import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.DtcParameterIdentification;
import beze.link.obd2.Protocols;

public class Mode7 extends DtcParameterIdentification
{

    /// <summary>
/// Instantiates a new instance of the Elm327 Mode7 class. This class should not be used on its own.
/// </summary>
    public Mode7()
    {
        super(
                DiagnosticTroubleCode.CodeType.Pending,
                "Diagnostic Trouble Code Request",
                (byte)0x07,
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
            return "47 02 76 96 45" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }
        else
        {
            return "47 02 02 76 96 45 AA AA" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }
    }
}

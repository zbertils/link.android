package beze.link.obd2.specialpids;

import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.DtcParameterIdentification;
import beze.link.obd2.Protocols;

public class ModeA extends DtcParameterIdentification
{

    /// <summary>
/// Instantiates a new instance of the Elm327 Mode0A class. This class should not be used on its own.
/// </summary>
    public ModeA()
    {
        super(
                DiagnosticTroubleCode.CodeType.Permanent,
                "Cleared Diagnostic Trouble Codes",
                (byte)0x0A,
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
            return "4A 06 70" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }
        else
        {
            // for CAN related protocols
            return "4A 01 06 70 AA AA AA AA" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }
    }
}

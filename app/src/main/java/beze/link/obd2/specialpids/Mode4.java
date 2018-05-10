package beze.link.obd2.specialpids;

import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.DtcParameterIdentification;
import beze.link.obd2.Protocols;

public class Mode4 extends DtcParameterIdentification
{

    /// <summary>
/// Instantiates a new instance of the Elm327 Mode4 class. This class should not be used on its own.
/// </summary>
    public Mode4()
    {
        super(
                DiagnosticTroubleCode.CodeType.Permanent,
                "Diagnostic Trouble Code Clear",
                (byte)0x04,
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
        return Protocols.Elm327.Prompt; // there is nothing to return
    }

}

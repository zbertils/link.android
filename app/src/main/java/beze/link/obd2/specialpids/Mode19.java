package beze.link.obd2.specialpids;

import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beze.link.Globals;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.Protocols;
import beze.link.obd2.cables.Cable;

public class Mode19 extends ParameterIdentification
{
    private static final String TAG = Globals.TAG_BASE + "Mode19";

    public static class RequestType
    {
        public static final byte ImmatureCode = 0x01;
        public static final byte CurrentCode = 0x02;
        public static final byte Reserved1 = 0x04;
        public static final byte Reserved2 = 0x08;
        public static final byte FreezeFrameAvailable = 0x10;
        public static final byte OldCode = 0x20;
        public static final byte PendingCode = 0x40;
        public static final byte MILActive = (byte)0x80;

        public static final byte ActiveAndPending = (byte)(RequestType.CurrentCode + RequestType.PendingCode + RequestType.FreezeFrameAvailable + RequestType.MILActive);
        public static final byte ActiveOnly = (byte)(RequestType.CurrentCode + RequestType.FreezeFrameAvailable + RequestType.MILActive);
        public static final byte PendingOnly = (byte)(RequestType.PendingCode + RequestType.FreezeFrameAvailable + RequestType.MILActive);

        public static final byte All = (byte)0xFF;
    }

    public static final String[] StatusByteEncodedBitDescription =
            {
                    "Insufficient data to consider malfunction",
                    "Current code present at time of request",
                    "Manufacturer specific status",
                    "Manufacturer specific status",
                    "Stored trouble code",
                    "Warning lamp previously illuminated for this code, malfunction not currently detected, code not yet erased",
                    "Warning lamp pending for this code, not illuminated but malfunction was detected",
                    "Warning lamp illuminated for this code"
            };

    /// <summary>
    /// Instantiates a new instance of the Mode19 class. This class should not be used on its own.
    /// </summary>
    public Mode19()
    {
        super(
                "Diagnostic Trouble Code Status",
                (byte)0x19,
                RequestType.All,
                (byte)0x01,
                "",
                "",
                "DTC",
                "Diagnostic",
                "",
                Protocols.J1850.Headers.PCM);
    }

    @Override
    public byte PacketSize() { return 0x01; }

    public HashMap<String, DiagnosticTroubleCode> RequestAllDtcStatuses(Cable cable)
    {
        HashMap<String, DiagnosticTroubleCode> statuses = new HashMap<>();

        String response = cable.Communicate(this, 5000);
        String[] responses = ParameterIdentification.PrepareResponseString(response);
        if (responses != null)
        {
            for (String individualResponse : responses)
            {
                int[] responseBytes = ParameterIdentification.ParseStringValues(individualResponse);
                if (responseBytes != null)
                {
                    if (responseBytes.length == 4)
                    {
                        if (responseBytes[0] - 0x40 == this.Mode)
                        {
                            String firstByte = String.format("%02X", responseBytes[1]);
                            String secondByte = String.format("%02X", responseBytes[2]);

                            // the code is still in elm327 encoded format, e.g. "4670" which would be DTC B0670
                            String elm327code = firstByte + secondByte;
                            DiagnosticTroubleCode code = new DiagnosticTroubleCode(elm327code, DiagnosticTroubleCode.CodeType.StatusCheck);

                            String codeStatusDescription = GetStatusDescription(responseBytes[3]);
                            code.Status = codeStatusDescription;

                            // see if the code exists in the list of known codes to get the description
                            if (Globals.dtcDescriptions != null &&
                                Globals.dtcDescriptions.containsKey(code.Code))
                            {
                                String description = Globals.dtcDescriptions.get(code.Code);
                                code.Description = description;
                            }
                            else
                            {
                                code.Description = "Unknown Code";
                            }

                            // the P0000 code does not actually exist, remove it from the final list
                            if (!code.Code.equalsIgnoreCase("P0000"))
                            {
                                statuses.put(code.Code, code);
                            }
                        }
                        else
                        {
//                            link.DiagnosticLogger.Write("Invalid mode for mode 19 response line \"" + individualResponse + "\"");
                            HyperLog.e(TAG, "RequestAllDtcStatuses: invalid mode received for mode 19 response, line \"" + individualResponse + "\"");
                        }
                    }
                    else
                    {
//                        link.DiagnosticLogger.Write("Received a mode 19 response line that did not have 4 bytes. Received \"" + individualResponse + "\"");
                        HyperLog.e(TAG, "RequestAllDtcStatuses: received a mode 19 response that did not have 4 bytes, received \"" + individualResponse + "\"");
                    }
                }
                else
                {
//                    link.DiagnosticLogger.Write("ParseStringValues() returned null for response \"" + individualResponse ?? String.Empty + "\"");
                    HyperLog.e(TAG, "RequestAllDtcStatuses: ParseStringValues() returned null for response \"" + (individualResponse != null ? individualResponse : "") + "\"");
                }
            }
        }
        else
        {
//            link.DiagnosticLogger.Write("PrepareResponseString() returned null for \"" + response ?? String.Empty + "\"");
            HyperLog.e(TAG, "RequestAllDtcStatuses: PrepareResponseString() returned null for \"" + (response != null ? response : "") + "\"");
        }

        return statuses;
    }

    /// <summary>
/// Gets the most severe description of the status bits.
/// </summary>
/// <param name="statusValue"> The 8 status bit value to obtain the description for. </param>
/// <returns> The most severe description of the status bits. </returns>
    private String GetStatusDescription(int statusValue)
    {
        String description = "";

        for (int index = 0; statusValue > 0 && index < 8; index++)
        {
            int bitValue = statusValue & 0x1;
            if (bitValue > 0)
            {
                // since the index counts up, and the severity increases with each bit only save the most severe bit
                description = StatusByteEncodedBitDescription[index];
            }

            // decrease the value by shifting the bits over by one
            statusValue = statusValue >> 1;
        }

        return description;
    }

    @Override
    public String Pack(Protocols.Protocol protocol)
    {
        return String.format("%02X %02X 00", Mode, PID);
    }

    @Override
    public String SimulatedResponse(Protocols.Protocol type)
    {
        if (type == Protocols.Protocol.J1850)
        {
            return
                    "59 A9 57 01" + Protocols.Elm327.EndOfLine +
                    "59 A9 58 01" + Protocols.Elm327.EndOfLine +
                    "59 B8 02 FF" + Protocols.Elm327.EndOfLine +
                    "59 D0 16 11" + Protocols.Elm327.EndOfLine +
                    "59 A9 57 3F" + Protocols.Elm327.EndOfLine +
                    "59 A9 57 25" + Protocols.Elm327.EndOfLine +
                    "59 06 70 7F" + Protocols.Elm327.EndOfLine +
                    "59 04 01 3F" + Protocols.Elm327.EndOfLine +
                    "59 27 71 21" + Protocols.Elm327.EndOfLine +
                    "59 00 00 13" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
        }

        return "";
//        else
//        {
//            return
//                    "0: 59 A9 57 01" + Protocols.Elm327.EndOfLine +
//                            "1:A9 58 01" + Protocols.Elm327.EndOfLine +
//                            "2: B8 02 01" + Protocols.Elm327.EndOfLine +
//                            "3: D0 16 11" + Protocols.Elm327.EndOfLine +
//                            "4: A9 57 3F" + Protocols.Elm327.EndOfLine +
//                            "5: A9 57 25" + Protocols.Elm327.EndOfLine +
//                            "6: 06 70 7F" + Protocols.Elm327.EndOfLine +
//                            "7:04 01 3F" + Protocols.Elm327.EndOfLine +
//                            "8: 27 71 21" + Protocols.Elm327.EndOfLine +
//                            "9: 00 00 13" + Protocols.Elm327.EndOfLine + Protocols.Elm327.Prompt;
//        }
    }
}

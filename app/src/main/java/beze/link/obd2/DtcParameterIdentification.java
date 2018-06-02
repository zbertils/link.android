package beze.link.obd2;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import beze.link.Globals;
import beze.link.obd2.cables.Cable;

public class DtcParameterIdentification extends ParameterIdentification
{
    private static final String TAG = Globals.TAG_BASE + "DtcPid";

    public final DiagnosticTroubleCode.CodeType CodeType;

    public DtcParameterIdentification(
            DiagnosticTroubleCode.CodeType codeType,
            String name,
            byte mode,
            short pid,
            byte dataByteCount,
            String formulaString,
            String units,
            String group,
            String pidType,
            String description,
            String header)
    {
        super(name, mode, pid, dataByteCount, formulaString, units, group, pidType, description, header);
        this.CodeType = codeType;
    }

    public DtcParameterIdentification(
            DiagnosticTroubleCode.CodeType codeType,
            String name,
            byte mode,
            short pid,
            byte dataByteCount,
            String formulaString,
            String units,
            String group,
            String pidType)
    {
        super(name, mode, pid, dataByteCount, formulaString, units, group, pidType, "", "");
        this.CodeType = codeType;
    }

    /// <summary>
    /// Converts the special mode into a String suitable for sending to ELM327 cables.
    /// </summary>
    /// <returns> The String representing the mode only in ELM327 format. </returns>
    public String Pack()
    {
        return String.format("%02X", this.Mode);
    }

    public List<DiagnosticTroubleCode> RequestTroubleCodes(Cable cable)
    {
        // TODO: set frame headers for other protocols

        // set the frame header to the default PCM for the main engine codes
        if (cable.Protocol == Protocols.Protocol.HighSpeedCAN11 ||
                cable.Protocol == Protocols.Protocol.LowSpeedCAN11)
        {
            Protocols.Elm327.SetFrameHeader(Protocols.CAN.Headers.Default);
        }
        else if (cable.Protocol == Protocols.Protocol.J1850)
        {
            Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.Default);
        }

        return GetDtc(cable, this, CodeType);
    }

    private List<DiagnosticTroubleCode> GetDtc(
            Cable cable,
            DtcParameterIdentification specialMode,
            DiagnosticTroubleCode.CodeType codeType)
    {
        List<DiagnosticTroubleCode> codes = new ArrayList<DiagnosticTroubleCode>();

        try
        {
            String dtcString = cable.Communicate(specialMode);
            Log.d(TAG, "GetDtc: mode " + this.Mode + " received \"" + dtcString + "\"");

            // possible there is no data so check
            if (dtcString != null && !dtcString.isEmpty() && !dtcString.contains(Protocols.Elm327.Responses.NoData))
            {
                String[] dataLines = ParameterIdentification.PrepareResponseString(dtcString);
                if (dataLines != null && dataLines.length > 0)
                {
                    dataLines = prepMarkedLines(dataLines);
                    for (String line : dataLines)
                    {
                        int[] dtcBytes = ParameterIdentification.ParseStringValues(line);
                        if (dtcBytes != null && dtcBytes.length > 0)
                        {
                            // make sure the correct pid was received
                            if (dtcBytes[ParameterIdentification.ResponseByteOffsets.Mode] - 0x40 == specialMode.Mode)
                            {
                                int[] dtcNumbers = Arrays.copyOfRange(dtcBytes, 1, dtcBytes.length);

                                // check if the returned numbers are all zeros, some times the data is not valid
                                boolean allZeros = true;
                                for (int b : dtcNumbers)
                                {
                                    if (b != 0 && b != 0xAA)
                                    { // 0 for non-CAN vehicles, and 0xAA for CAN
                                        allZeros = false;
                                        break;
                                    }
                                }
                                // check if the response is empty, some protocols will return data with all zeros
                                if (!allZeros)
                                {
                                    // the CAN protocols use a byte for how many codes are received, ISO based protocols do not,
                                    if (cable.Protocol == Protocols.Protocol.HighSpeedCAN11 ||
                                            cable.Protocol == Protocols.Protocol.LowSpeedCAN11 ||
                                            cable.Protocol == Protocols.Protocol.HighSpeedCAN29 ||
                                            cable.Protocol == Protocols.Protocol.LowSpeedCAN29)
                                    {
                                        dtcNumbers = Arrays.copyOfRange(dtcNumbers, 1, dtcNumbers.length);
                                    }

                                    // each code should be two bytes, meaning we need an even number of bytes to parse correctly
//                                    if (dtcNumbers.length % 2 == 0) {
                                    {
                                        for (int index = 0; index < dtcNumbers.length - 1; index += 2)
                                        {
                                            // it is possible to have zero values padded out in the packet, check if this is the case
                                            if (!(dtcNumbers[index] == 0 && dtcNumbers[index + 1] == 0) &&
                                                    !(dtcNumbers[index] == 0xAA && dtcNumbers[index + 1] == 0xAA))
                                            {
                                                String firstByte = String.format("%02X", dtcNumbers[index]);
                                                String secondByte = String.format("%02X", dtcNumbers[index + 1]);

                                                // the code is still in elm327 encoded format, e.g. "4670" which would be DTC B0670
                                                String elm327code = firstByte + secondByte;
                                                DiagnosticTroubleCode code = new DiagnosticTroubleCode(elm327code, codeType);

                                                // see if the code exists in the list of known codes to get the description
                                                if (Globals.dtcDescriptions.containsKey(code.Code))
                                                {
                                                    String description = Globals.dtcDescriptions.get(code.Code);
                                                    code.Description = description;
                                                }

                                                // finally add the code to the list after the description was checked
                                                Log.d(TAG, "GetDtc: finished decoding dtc " + code.Code);
                                                codes.add(code);
                                            }
                                            else
                                            {
                                                Log.w(TAG, "GetDtc: skipping zero values DTC");
                                            }
                                        }
                                    }
                                    if (dtcNumbers.length % 2 != 0)
                                    {
                                        Log.w(TAG, "GetDtc: number of bytes to work with for dtc check is not an expected even number, got" + dtcNumbers.length + " total bytes, ignoring last byte");
                                    }
                                }
                                else
                                {
                                    Log.i(TAG, "GetDtc: Received correct mode and valid DTC data, but it is all zeros, no DTC to report");
                                }
                            }
                            else
                            {
                                Log.e(TAG, "GetDtc: mode returned for DTC check is invalid, received " + (dtcBytes[ParameterIdentification.ResponseByteOffsets.Mode] - 0x40) + ", and expected " + specialMode.Mode);
                            }
                        }
                        else
                        {
                            Log.i(TAG, "GetDtc: dtcBytes is null or zero sized");
                        }
                    } // end for (String line : dataLines)
                }
                else
                {
                    Log.i(TAG, "dataLines was null, nothing returned for mode " + this.Mode);
                }
            }
            else
            {
                // maybe do something, most likely there are no codes and "NO DATA" was returned
                Log.i(TAG, "GetDtc: it doesn't look like there are any trouble codes to be had");
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "GetDtc: could not get trouble codes due to exception:\n" + Globals.getStackTraceAndMessage(ex));
            ex.printStackTrace();
        }

        return codes;
    }

}

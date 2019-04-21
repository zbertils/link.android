package beze.link.obd2;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import beze.link.Globals;
import beze.link.util.MathStringEngine;

public class ParameterIdentification {

    private static final String TAG = Globals.TAG_BASE + "ParameterIdentification";

    public static class ResponseByteOffsets
    {
        public static final byte Mode = 0;

        // offsets for normal, non-extended pids
        public static final byte PID = 1;
        public static final byte DataByte0 = 2;
        public static final byte DataByte1 = 3;
        public static final byte DataByte2 = 4;
        public static final byte DataByte3 = 5;
        public static final byte DataByte4 = 6;

        // offsets for extended pids
        public static final byte ExtendedPIDByte0 = 1;
        public static final byte ExtendedPIDByte1 = 2;
        public static final byte ExtendedDataByte0 = 3;
        public static final byte ExtendedDataByte1 = 4;
        public static final byte ExtendedDataByte2 = 5;
        public static final byte ExtendedDataByte3 = 6;
    }

    public static class PacketSizes
    {
        public static final byte Normal = 2;
        public static final byte Extended = 3;
    }

    // these should be read only, for now they are mutable
    public String FormulaString;
    public String Name;
    public String Description;
    public String Units;
    public String Group;
    public String PidType;
    public int Mode;
    public short PID;
    public int DataByteCount;
    public String Header;
    public String CANHeader;
    public Boolean Supported = null;

    public String lastError = "";

    // settable by outside classes
    public Boolean LogThisPID;
    public long Timestamp;

    private double mLastDecodedValue;

    public String getShortName() {
        String shortName = this.Name.substring(0, Math.min(this.Name.length(), 25));
        if (this.Name.length() > 25) {
            shortName += "...";
        }

        return shortName;
    }

    public ParameterIdentification(org.json.JSONObject jsonObj)
    {
        try {
            this.Name = jsonObj.getString("Name");
            this.Mode = (byte) jsonObj.getInt("Mode");
            this.PID = (short) jsonObj.getInt("PID");
            this.DataByteCount = (byte) jsonObj.getInt("DataByteCount");
            this.FormulaString = jsonObj.getString("FormulaString");
            this.Units = jsonObj.getString("Units");
            this.Group = jsonObj.getString("Group");
            this.PidType = jsonObj.getString("PidType");
            this.Description = jsonObj.getString("Description");
            this.LogThisPID = jsonObj.getBoolean("LogThisPID");

            // the headers are optional
            this.Header = jsonObj.has("Header") ? jsonObj.getString("Header") : null;
            this.CANHeader = jsonObj.has("CANHeader") ? jsonObj.getString("CANHeader") : null;

            // other values to be initialized
            this.Timestamp = 0;
            this.mLastDecodedValue = 0;
        }
        catch (Exception ex) {
            Log.e("ParameterIdentification", "ParameterIdentification: could not parse jsonObj " + jsonObj.toString() );

            // set everything to default values, this pid is not valid anymore
            this.Name = "";
            this.Mode = 0;
            this.PID = 0;
            this.DataByteCount = 0;
            this.FormulaString = "";
            this.Units = "";
            this.Group = "";
            this.PidType = "";
            this.Description = "";
            this.Header = "";
            LogThisPID = false;
            this.Timestamp = 0;
            this.mLastDecodedValue = 0;
        }

        this.Supported = null; // default to null, this is something determined at run time
    }

    public ParameterIdentification(
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
        this.Name = name;
        this.Mode = mode;
        this.PID = pid;
        this.DataByteCount = dataByteCount;
        this.FormulaString = formulaString;
        this.Units = units;
        this.Group = group;
        this.PidType = pidType;
        this.Description = description != null ? description : "";
        this.Header = header != null ? header : "";
        this.Supported = null; // default to false, this is something determined at run time
    }

    public byte PacketSize()
    {
        if (this.PID <= Byte.MAX_VALUE && this.Mode <= 2)
        {
            return PacketSizes.Normal;
        }
        else
        {
            return PacketSizes.Extended;
        }
    }

    public double LastDecodedValue()
    {
        return mLastDecodedValue;
    }

    public void setLastDecodedValue(double value)
    {
        this.mLastDecodedValue = value;
        Timestamp = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public String toString()
    {
        return String.format(
                "Name:          %1$s\n" +
                "Mode:          %2$d\n" +
                "PID:           %3$d\n" +
                "DataByteCount: %4$d\n" +
                "FormulaString: %5$s\n" +
                "Units:         %6$s\n" +
                "Group:         %7$s\n" +
                "PidType:       %8$s\n" +
                "Description:   %9$s\n" +
                "Header:        %10$s",
                this.Name,
                this.Mode,
                this.PID,
                this.DataByteCount,
                this.FormulaString,
                this.Units,
                this.Group,
                this.PidType,
                this.Description,
                this.Header);
    }

    /// <summary>
    /// Packetizes the ParameterIdentification object.
    /// </summary>
    /// <param name="protocol"> The protocol being used for communicating this PID. </param>
    /// <returns> The byte[] object representing the packet to send across the OBD2 connection. </returns>
    public String Pack(Protocols.Protocol protocol)
    {
        String dataStr = "";

        dataStr = String.format("%02X%02X",
                this.Mode,
                this.PID);

        // a small optimization, if there are four or less data bytes being
        // returned then it can fit into one frame and the cable can return
        // immediately after receiving that one frame, otherwise just let
        // the cable timeout on reads in case it is a special PID like mode 9,
        // CAN protocols do not like having this extra 01 as its interpreted
        // as another part of the PID itself and not an expected byte count
        if (this.DataByteCount <= 4 &&
                this.Mode == 0x22 &&
                !Protocols.IsCan(protocol))
        {
            dataStr += "01";
        }

        return dataStr;
    }

    /// <summary>
    /// Unpacks response data to the given pid.
    /// </summary>
    /// <param name="data"> The response to communicating <paramref name="pid"/> to the OBD2 cable. </param>
    /// <returns> The decoded value if successful, and double.NaN otherwise. </returns>
    public double Unpack(String data)
    {
        try
        {
            // if there are not enough bytes to decode the message than return an empty String
            if (data == null || data.isEmpty())
            {
                return Double.NaN;
            }

            // convert back to a String so the ascii values can be parsed into their integer representations,
            // then parse into individual integers, for the basic pid values there will only be one line returned,
            // for all other multi-line pids they are expected to override the Unpack() function and handle the parsing individually
            String[] dataStr = PrepareResponseString(data);
            if (dataStr.length > 0)
            {
                int[] values = ParseStringValues(dataStr[0]);

                // default the respond pid to be if not using extended pids,
                // then figure out if it should be adjusted if it is an extended pid
                int responsePid = values[ResponseByteOffsets.PID];
                if (this.PacketSize() == 3)
                {
                    responsePid = (values[ResponseByteOffsets.ExtendedPIDByte0] << 8) +
                            values[ResponseByteOffsets.ExtendedPIDByte0 + 1];
                }

                // make sure this response has the correct number of expected data bytes,
                // the correct expected mode, and the correct expected pid
                if (values[ResponseByteOffsets.Mode] - 0x40 != this.Mode)
                {
                    return Double.NaN;
                }

                if (responsePid != this.PID) {
                    return Double.NaN;
                }

                int startIndex = ResponseByteOffsets.DataByte0;
                if (this.PacketSize() == 3) {
                    startIndex = ResponseByteOffsets.ExtendedDataByte0;
                }

                // manually convert each byte to a string value since the String.format function doesn't like parsing byte arrays into strings
                String[] responseValueStrings = new String[this.DataByteCount];
                for (int i = 0; i < this.DataByteCount; i++) {
                    responseValueStrings[i] = Integer.toString(values[startIndex + i]);
                }

                String responseExpression = String.format(this.FormulaString, (Object[])responseValueStrings);
                return MathStringEngine.eval(responseExpression);
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Unpack: could not unpack\r\n" + this.toString());
            lastError = ex.toString();
            return Double.NaN;
        }

        return Double.NaN;
    }

    /// <summary>
    /// Removes any ELM specific characters that do not relate to a request.
    /// </summary>
    /// <param name="dataStr"> The data String that was sent from the cable. </param>
    /// <returns> The prepared line without prompt characters or end of line characters. </returns>
    public static String[] PrepareResponseString(String dataStr)
    {
        if (dataStr != null && !dataStr.isEmpty())
        {
            dataStr = dataStr.trim(); // remove leading and trailing spaces
            dataStr = dataStr.replace(Protocols.Elm327.Prompt, "");
            String[] splits = dataStr.split(Protocols.Elm327.EndOfLine);

            return splits;

//            List<String> finalSplits = new ArrayList<>();
//            for (String s : splits) {
//                if (s != null && !s.isEmpty()) {
//                    finalSplits.add(s.trim());
//                }
//            }
//
//            return finalSplits.toArray();
        }

        return null;
    }

    /// <summary>
    /// Parses String hex values into individual bytes.
    /// </summary>
    /// <param name="elm327Response"> The ELM response to a request. Assumed to be space-separated for each byte. </param>
    /// <returns> The array containing parsed values. </returns>
    public static int[] ParseStringValues(String elm327Response)
    {
        if (elm327Response == null || elm327Response.isEmpty())
        {
            return null;
        }

        // split the values based on spaces
        elm327Response = elm327Response.trim();
        String[] dataStrValues = elm327Response.split(" ");
        int[] values = new int[dataStrValues.length];

        for (int i = 0; i < dataStrValues.length; i++)
        {
            try
            {
                // parse everything from hex as a uint, there are no signed values with obd2,
                // since Byte.parseHexString() requires signed values things like FF throw exceptions,
                // so parse using a special method that will correctly handle the unsignedness
                values[i] = Integer.parseInt(dataStrValues[i], 16);
            }
            catch (Exception ex)
            {
                Log.e(TAG, "ParseStringValues: invalid conversion from String to Integer for value " + dataStrValues[i] + " at position " + i);
                return null;
            }
        }

        return values;
    }

    protected String[] prepMarkedLines(String[] preppedLines) {

        final String identifier = ":";

        // determine if the array of strings contains any lines in the format "X: hh hh hh..."
        boolean linesAreMarked = false;
        for (String line : preppedLines) {
            if (line != null && !line.isEmpty() && line.length() > 2) {
                if (line.contains(identifier)) {
                    // line contains an identifier, assume all lines are marked
                    linesAreMarked = true;
                    break;
                }
            }
        }

        // if the lines are marked, remove any that do not have an identifier,
        // some bad elm adapters have buffer issues when receiving multiple lines
        // and put a "no dtc reported" line first that is not marked with an identifier
        if (linesAreMarked) {
            List<String> keptLines = new ArrayList<>();
            for (String line : preppedLines) {
                if (line.contains(identifier)) {
                    // only keep the line if it has an identifier
                    keptLines.add(line);
                }
            }

            // go through each kept line and put them together as a single line,
            // this is because some adapters put bytes out of order with an odd number per line,
            // but the values as a whole make sense and add up to the correct number of bytes
            String finalLine = "";
            for (String line : keptLines) {

                // if the final line is not empty, meaning there are leading characters, then put a space so the parser can still separate them
                if (!finalLine.isEmpty()) {
                    finalLine += " ";
                }

                int index = line.indexOf(identifier);
                String subStr = line.substring(index + 1, line.length());

                // the lines should already be in order, append to the final line in the order of the list
                finalLine += subStr.trim();
            }

            return new String[] { finalLine.trim() };
        }
        else {
            // lines are not marked, assume each line is in the format "43 <optional count> hh hh hh..."
            return preppedLines;
        }
    }

    public String SimulatedResponse(Protocols.Protocol type)
    {
        // populate the String with valid info until the data packets, then fill with random data
        String dataStr = Integer.toHexString(this.Mode + 0x40) + " ";

        // the pid needs to be split into two separate bytes if an extended pid
        // with a space separating them just like the elm response would be
        if (this.PacketSize() == 3)
        {
            byte[] pidBytes = ByteBuffer.allocate(2).putShort(this.PID).array();
            dataStr += Integer.toHexString(pidBytes[0]) + " " + Integer.toHexString(pidBytes[1]);
        }
        else
        {
            dataStr += String.format("%02X", this.PID);
        }

        for (int i = 0; i < this.DataByteCount; i++)
        {
            int randomInt = (new Random()).nextInt();
            byte randomByte = (byte)(Math.abs(randomInt) & 0x7F);
            dataStr += " " + String.format("%02X", randomByte);
        }

        if (type == Protocols.Protocol.HighSpeedCAN11 ||
                type == Protocols.Protocol.LowSpeedCAN11 ||
                type == Protocols.Protocol.HighSpeedCAN29 ||
                type == Protocols.Protocol.LowSpeedCAN29)
        {
            dataStr += " AA AA AA AA";
        }

        // purposely sleep to simulate the minimum cable transmission delay
        try {
            Thread.sleep(100);
        }
        catch (Exception ex) {
            Log.i(TAG, "SimulatedResponse: could not sleep");
        }

        return dataStr;
    }

}

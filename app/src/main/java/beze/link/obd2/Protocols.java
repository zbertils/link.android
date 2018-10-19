package beze.link.obd2;

import android.net.wifi.WifiConfiguration;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Protocols
{
    public static final String ToHexFormat = "X2";

    public static boolean IsCan(Protocol protocol)
    {
        return protocol == Protocol.HighSpeedCAN11 ||
                protocol == Protocol.HighSpeedCAN29 ||
                protocol == Protocol.LowSpeedCAN11 ||
                protocol == Protocol.LowSpeedCAN29;
    }

    public enum Protocol
    {
        None,
        Auto,
        J1850,
        HighSpeedCAN11,
        LowSpeedCAN11,
        HighSpeedCAN29,
        LowSpeedCAN29,
        ISO9141,
        ISO9142,
        Unknown
    }

    public static Protocol NameToProtocol(String name)
    {
        String upperName = name.toUpperCase();
        if (upperName.contains("J1850"))
        {
            return Protocol.J1850;
        }
        else if (upperName.contains("CAN"))
        {
            if (upperName.contains("11/500"))
            {
                return Protocol.HighSpeedCAN11;
            }
            else if (upperName.contains("11/250"))
            {
                return Protocol.LowSpeedCAN11;
            }
            else if (upperName.contains("29/500"))
            {
                return Protocol.HighSpeedCAN29;
            }
            else if (upperName.contains("29/250"))
            {
                return Protocol.LowSpeedCAN29;
            }
            else {
                return Protocol.Unknown;
            }
        }
        else if (upperName.contains("AUTO"))
        {
            return Protocol.Auto;
        }
        else if (upperName.contains("9141"))
        {
            return Protocol.ISO9141;
        }
        else if (upperName.contains("9142"))
        {
            return Protocol.ISO9142;
        }

        return Protocol.None;
    }

    public static List<Protocol> NamesToProtocols(String[] names) {
        List<Protocol> protocols = new ArrayList<Protocol>();
        for (String name : names) {
            protocols.add(NameToProtocol(name));
        }
        return protocols;
    }

    public static class J1850
    {
        public static class Headers
        {
            public static class PriorityAndType
            {
                public static final String LowPriorityFunctional = "68";
                public static final String LowPriorityNodeToNode = "6C";

                public static final String Default = LowPriorityFunctional;
            }

            public static class Destinations
            {
                public static final String RequestLegislatedDiagnostics = "6A";
                public static final String Engine = "10";
                public static final String Transmission = "18";
                public static final String ABS = "28";
                public static final String Body = "40";
                public static final String AirBag = "58";
                public static final String NULL = "FF";

                public static final String Default = RequestLegislatedDiagnostics;
            }

            public static class Sources
            {
                public static final String OffBoardCable = "F1";

                public static final String Default = OffBoardCable;
            }

            public static final String Default = PriorityAndType.Default               + Destinations.Default        + Sources.Default;
            public static final String PCM     = PriorityAndType.LowPriorityNodeToNode + Destinations.Engine         + Sources.OffBoardCable;
            public static final String TCM     = PriorityAndType.LowPriorityNodeToNode + Destinations.Transmission   + Sources.OffBoardCable;
            public static final String BCM     = PriorityAndType.LowPriorityNodeToNode + Destinations.Body           + Sources.OffBoardCable;
            public static final String AirBag  = PriorityAndType.LowPriorityNodeToNode + Destinations.AirBag         + Sources.OffBoardCable;
            public static final String ABS     = PriorityAndType.LowPriorityNodeToNode + Destinations.ABS            + Sources.OffBoardCable;

            public static final String NULL    = PriorityAndType.LowPriorityNodeToNode + Destinations.NULL           + Sources.OffBoardCable;
        }

    }

    public static class CAN {
        public static class Headers {

            public static class Destinations {
                public static final String OffBoardCable = "D";
            }

            public static class Sources {
                public static final String PCM = "8";
                public static final String All = "F";
            }

            public static final String Default = "7" + Destinations.OffBoardCable + Sources.All;
        }
    }

    public static class Elm327
    {

        public static final String Header = "ELM327";
        public static final String Reset = "ATZ";
        public static final String DisplayProtocol = "ATDP";
        public static final String SetAutoProtocol = "ATSP0";
        public static final String SetTimeoutMaximum = "ATSTFF";
        public static final String SetSpacesOff = "ATS0";
        public static final String EchoOff = "ATE0";
        public static final String EchoOn = "ATE1";
        public static final String AdaptiveTimingOn = "ATAT1";
        public static final String AdaptiveTimingOff = "ATAT0";
        public static final String SetHeadersOff = "ATH0";
        public static final String Prompt = ">";

        /// <summary>
        /// The command for setting the frame header. This String needs to be given the 3-byte frame header value, e.g. "6C 10 F1" or a value from Protocols.J1850.Headers.
        /// </summary>
        public static String SetFrameHeader(String header)
        {
            return "ATSH" + header;
        }

        /// <summary>
        /// Forces a protocol search after SetAutoProtocol has been set.
        /// </summary>
        public static final String ForceProtocolSearch = "0105";

        public static final String EndOfLine = "\r";
        public static final char EndOfLineChar = '\r';
        public static final byte EndOfLineByte = 0x0D;

        public static class Responses
        {
            public static final String Auto = "AUTO";
            public static final String OK = "OK";
            public static final String NoData = "NO DATA";
            public static final String EndOfLine = "\r";
            public static final String Searching = "SEARCHING...";
            public static final String Stopped = "STOPPED";
        }

    }

}

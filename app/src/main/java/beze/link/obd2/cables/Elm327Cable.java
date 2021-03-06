package beze.link.obd2.cables;

import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import beze.link.Globals;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.Protocols;
import beze.link.obd2.specialpids.Mode19;
import beze.link.obd2.specialpids.Mode3;
import beze.link.obd2.specialpids.Mode4;
import beze.link.obd2.specialpids.Mode7;
import beze.link.obd2.specialpids.Mode9;
import beze.link.obd2.specialpids.ModeA;

public class Elm327Cable extends Cable
{
    private static final String TAG = Globals.TAG_BASE + "Elm327Cable";

    protected Mode3 mode3 = new Mode3();
    protected Mode4 mode4 = new Mode4();
    protected Mode7 mode7 = new Mode7();
    protected Mode9 mode9 = new Mode9();
    protected ModeA modeA = new ModeA();
    protected Mode19 mode19 = new Mode19();

    protected boolean needsReconnect = false;

    protected IConnectionCallback callback;

    /// <summary>
    /// Constructor for simulated cables.
    /// </summary>
    protected Elm327Cable()
    {
        super();
        CableType = Type.Elm327;
    }

    /// <summary>
    /// Creates a new instance of CustomCable.
    /// </summary>
    /// <param name="deviceName"> The port the cable is connected to. </param>
    /// <param name="timeoutMilliseconds"> The timeout to use for communication with the cable. </param>
    public Elm327Cable(IConnectionCallback callback)
    {
        super();
        this.callback = callback;
        mOpen = false;
        mInitialized = false;
    }

    protected boolean initialize()
    {
        // default cable info
        info = new CableInfo();

        HyperLog.i(TAG, "Elm327Cable: discovering ELM version");
        //callback.ConnectionCallbackAction("Discovering ELM version");

        // detect what type of cable is connected
        String response = SendCommand(Protocols.Elm327.Reset, 10000, Protocols.Elm327.Header);
        if (response != null && response.contains(Protocols.Elm327.Header))
        {
            HyperLog.i(TAG, "Elm327Cable: cable is ELM327 type");
            String version = "NA";

            // get the version number for posterity
            if (response.contains("v"))
            {
                int indexOfVersion = response.indexOf("v");
                version = response.substring(indexOfVersion);
                HyperLog.i(TAG, "Elm327Cable: discovered ELM version: " + version);
                info.Version = version;
//                callback.ConnectionCallbackAction("Discovered ELM version " + version);
            }

            // turn echo off
            HyperLog.i(TAG, "Elm327Cable: turning echo off");
//            callback.ConnectionCallbackAction("Turning echo off");

            response = SendCommand(Protocols.Elm327.EchoOff, 10000);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description += "Could not turn echo off...\r\n";
                HyperLog.e(TAG, "Elm327Cable: could not turn echo off");
                return false;
            }

            info.EchoOff = true;

//            callback.ConnectionCallbackAction("Setting timeout to maximum (1 second)");
//            HyperLog.i(TAG, "Elm327Cable: setting timeout to maximum (1 second)");
//            response = SendCommand(Protocols.Elm327.SetTimeoutMaximum, 10000);
//            if (!response.contains(Protocols.Elm327.Responses.OK))
//            {
//                info.Description += "Could not set maximum timeout\r\n";
//                HyperLog.w(TAG, "Elm327Cable: could not set maximum timeout");
//                //return false;
//            }

//            callback.ConnectionCallbackAction("Setting headers to off");
            HyperLog.i(TAG, "Elm327Cable: setting headers to off");
            response = SendCommand(Protocols.Elm327.SetHeadersOff, 10000);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description += "Could not turn headers off\r\n";
                HyperLog.w(TAG, "Elm327Cable: could not turn headers off");
                //return false;
            }

//            callback.ConnectionCallbackAction("Turning auto protocol on");
            HyperLog.i(TAG, "Elm327Cable: turning auto protocol on");
            response = SendCommand(Protocols.Elm327.SetAutoProtocol, 10000);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description += "Could not set auto protocol\r\n";
                HyperLog.e(TAG, "Elm327Cable: could not set protocol to Auto");
                return false;
            }

//            callback.ConnectionCallbackAction("Forcing search for existing protocols");
            HyperLog.i(TAG, "Elm327Cable: forcing a search for existing protocols");

            // send a single pid, wait for a long time since some cars take a while
            response = SendCommand(Protocols.Elm327.ForceProtocolSearch, 10000);
            if (response == null || response.isEmpty())
            {
                HyperLog.e(TAG, "Elm327Cable: could not force an auto protocol search");
                return false;
            }

            response = SendCommand(Protocols.Elm327.DisplayProtocol, 10000);
            String chosenProtocol = response
                    .replace(Protocols.Elm327.Responses.Auto, "")
                    .replace(",", "").trim();

//            callback.ConnectionCallbackAction("Chosen protocol: " + chosenProtocol);
            HyperLog.i(TAG, "Elm327Cable: protocol chosen: " + chosenProtocol);
            if (!response.contains(Protocols.Elm327.Responses.Auto))
            {
                info.Description += "Displayed protocol did not mention auto\r\n";
                HyperLog.w(TAG, "Elm327Cable: displayed protocol did not mention auto");
                //return false;
            }

            Protocol = Protocols.NameToProtocol(chosenProtocol);
            info.Protocol = Protocol;
            info.ProtocolName = chosenProtocol;
            info.AutoProtocolSet = true;

            // everything is good to go
            mInitialized = true;

            // fully initialized, the fourth step is the final step
            info.Description += "Connected\r\n";
            HyperLog.i(TAG, "Elm327Cable: connected!");
        }
        else {
//            callback.ConnectionCallbackAction("Could not reset ELM device, received:\r\n" + response.replace(Protocols.Elm327.EndOfLine, "\r\n"));
        }

        return true;
    }

    protected String readline() throws TimeoutException {
        return readUntil(Protocols.Elm327.Prompt);
    }

    protected String readUntil(int timeout) throws TimeoutException {
        return readUntil(null, timeout);
    }

    protected String readUntil(String end) throws TimeoutException {
        return readUntil(end, -1);
    }

    protected String readUntil(String end, int timeout) throws TimeoutException {
        final int checkInterval = 5;
        final int totalChecks = (timeout == -1) ? Integer.MAX_VALUE : timeout / checkInterval + 1;
        int checkCount = 0;

        byte[] buffer = new byte[256];
        String response = "";

        try
        {
            boolean endReached = false;

            do {
                // check if there is anything to read in the first place
                while (cableConnection.available() > 0) {

                    // read the next amount of data and make it a string
                    int length = cableConnection.read(buffer);
                    if (length > 0) {
                        String current = new String(buffer, 0, length);

                        // if the new response is not empty or null, append to the total response returned
                        if (current != null && !current.isEmpty()) {
                            response += current;

                            if (end != null && !end.isEmpty()) {
                                if (response.contains(end)) {
                                    endReached = true;
                                    break; // found what we were looking for, break out and return
                                }
                            }
                        }
                    }
                }

                // if the end was reached then break before incrementing the count,
                // this keeps the logic in place to determine if a timeout was reached or not
                if (endReached) {
                    //HyperLog.v(TAG, "readUntil reached expected end \"" + end + "\"");
                    break;
                }

                // increment the count and sleep for an interval
                checkCount++;
                Thread.sleep(checkInterval);

            } while (checkCount < totalChecks);
        }
        catch (Exception ex) {
            HyperLog.w(TAG, "Exception occurred during readUntil for \"" + end + "\"", ex);
            return null;
        }

        // if we could not read all of the data then something went wrong,
        // such as reaching the timeout when expecting a definite end to the packet
        if (checkCount >= totalChecks && end != null && !end.isEmpty()) {
            throw new TimeoutException("Timeout occurred during read (checks/total = " + checkCount + "/" + totalChecks + "), partial read: " + ((response != null) ? response : ""));
        }

        return response.toString();
    }

    protected String SendCommand(String data, int sleepMilliseconds, String waitFor)
    {
        try
        {
            Send(data);
            String response = readUntil(waitFor, sleepMilliseconds);

            response = response.replace(Protocols.Elm327.Prompt, ""); // remove the prompt character before returning the response
            HyperLog.v(TAG, "SendElmInitString: response: " + response);

            return response;
        }
        catch (Exception ex)
        {
            HyperLog.w(TAG, "SendElmInitString: could not send Elm init string: " + data, ex);
            return "";
        }
    }

    @Override
    public String SendCommand(String data, int sleepMilliseconds)
    {
        return SendCommand(data, sleepMilliseconds, Protocols.Elm327.Prompt);
    }

    @Override
    synchronized
    public String Communicate(ParameterIdentification pid)
    {
        return Communicate(pid, 1500);
    }

    @Override
    synchronized
    public String Communicate(ParameterIdentification pid, int timeout)
    {
        if (!SetFrameHeader(pid))
        {
            return null;
        }

        // send the pid value first
        if (Send(pid))
        {
            // if sending was successful then try to receive
            return Receive(timeout);
        }

        // sending did not work, return null
        HyperLog.w(TAG, "Communicate() did not send properly, returning null");
        return null;
    }

    @Override
    synchronized
    public boolean Send(ParameterIdentification pid)
    {
        return Send(pid.Pack(this.Protocol));
    }

    @Override
    synchronized
    public boolean Send(String data)
    {
        if (cableConnection != null && cableConnection.isConnected())
        {
            HyperLog.i(TAG, "Send: sending data " + data);
            try {
                data += Protocols.Elm327.EndOfLine;
                cableConnection.write(data.getBytes());
                BytesSent += data.length();
                return true;
            }
            catch (Exception ex)
            {
                HyperLog.e(TAG, "Send: error sending data \"" + data + "\"", ex);
                needsReconnect = true;
            }
        }

        return false;
    }

    @Override
    synchronized
    public String Receive(int timeoutMilliseconds)
    {
        try
        {
            String response = readUntil(Protocols.Elm327.Prompt, timeoutMilliseconds);
            response = response.replace(Protocols.Elm327.Prompt, ""); // remove the prompt character
            HyperLog.v(TAG, "Receive: " + response);

            if (response.contains(Protocols.Elm327.Responses.NoData) ||
                response.contains(Protocols.Elm327.Responses.Searching) ||
                response.contains(Protocols.Elm327.Responses.Stopped))
            {
                HyperLog.w(TAG, "Receive: received invalid response for a PID: \"" + response + "\"");
                return null;
            }

            BytesReceived += response.length();

            return response;
        }
        catch (Exception ex)
        {
            // do nothing, timeout occurred and null should be returned
            HyperLog.w(TAG, "Receive: exception attempting read of PID", ex);
        }

        return null;
    }

    @Override
    synchronized
    public String RequestVIN()
    {
        return mode9.RequestVIN(this);
    }

    @Override
    synchronized
    public void ClearTroubleCodes()
    {
        Communicate(mode4);
    }

    @Override
    synchronized
    public HashMap<String, DiagnosticTroubleCode> RequestTroubleCodes()
    {
        List<DiagnosticTroubleCode> codes = new ArrayList<DiagnosticTroubleCode>();

        codes.addAll(mode3.RequestTroubleCodes(this));
        codes.addAll(mode7.RequestTroubleCodes(this));
        codes.addAll(modeA.RequestTroubleCodes(this));

        // if the J1850 protocol is used then request the active and pending codes from all controllers
        if (Globals.cable.Protocol == Protocol.J1850)
        {
            HashMap<String, DiagnosticTroubleCode> activePending = new HashMap<String, DiagnosticTroubleCode>();

            HyperLog.v(TAG, "Requesting active default DTC");
            mode19.Header = Protocols.J1850.Headers.Default;
            activePending.putAll(mode19.RequestActiveDtc(this));

            HyperLog.v(TAG, "Requesting active BCM DTC");
            mode19.Header = Protocols.J1850.Headers.BCM;
            activePending.putAll(mode19.RequestActiveDtc(this));

            HyperLog.v(TAG, "Requesting active PCM DTC");
            mode19.Header = Protocols.J1850.Headers.PCM;
            activePending.putAll(mode19.RequestActiveDtc(this));

            HyperLog.v(TAG, "Requesting active TCM DTC");
            mode19.Header = Protocols.J1850.Headers.TCM;
            activePending.putAll(mode19.RequestActiveDtc(this));

            HyperLog.v(TAG, "Requesting active Air Bag DTC");
            mode19.Header = Protocols.J1850.Headers.AirBag;
            activePending.putAll(mode19.RequestActiveDtc(this));

            HyperLog.v(TAG, "Requesting active ABS DTC");
            mode19.Header = Protocols.J1850.Headers.ABS;
            activePending.putAll(mode19.RequestActiveDtc(this));

            List<DiagnosticTroubleCode> activePendingList = new ArrayList(activePending.values());
            codes.addAll(activePendingList); // add the active pending list to the existing codes list
        }

        // put the list of values into a hashmap, this is mainly done as a simple way of removing duplicates
        HashMap<String, DiagnosticTroubleCode> codeMap = new HashMap<>();
        for (DiagnosticTroubleCode dtc : codes) codeMap.put(dtc.Code, dtc);
        return codeMap;
    }

    @Override
    synchronized
    public HashMap<String, DiagnosticTroubleCode> RequestAllDtcStatuses()
    {
        HashMap<String, DiagnosticTroubleCode> statuses = new HashMap<>();

        if (!Protocols.IsCan(this.Protocol))
        {
            HyperLog.v(TAG, "Requesting default DTC statuses");
            mode19.Header = Protocols.J1850.Headers.Default;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));

            HyperLog.v(TAG, "Requesting BCM DTC statuses");
            mode19.Header = Protocols.J1850.Headers.BCM;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));

            HyperLog.v(TAG, "Requesting PCM DTC statuses");
            mode19.Header = Protocols.J1850.Headers.PCM;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));

            HyperLog.v(TAG, "Requesting TCM DTC statuses");
            mode19.Header = Protocols.J1850.Headers.TCM;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));

            HyperLog.v(TAG, "Requesting Air Bag DTC statuses");
            mode19.Header = Protocols.J1850.Headers.AirBag;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));

            HyperLog.v(TAG, "Requesting ABS DTC statuses");
            mode19.Header = Protocols.J1850.Headers.ABS;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));
        }
        else
        {
            HyperLog.v(TAG, "Requesting default CAN DTC statuses");
            mode19.CANHeader = Protocols.CAN.ShortHeaders.Default;
            statuses.putAll(mode19.RequestAllDtcStatuses(this));
        }

        return statuses;
    }

}

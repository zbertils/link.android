package beze.link.obd2.cables;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        Log.i(TAG, "Elm327Cable: discovering ELM version");
        callback.ConnectionCallbackAction("Discovering ELM version");

        // detect what type of cable is connected
        String response = SendCommand(Protocols.Elm327.Reset, 1000, Protocols.Elm327.Header);
        if (response != null && response.contains(Protocols.Elm327.Header))
        {
            Log.i(TAG, "Elm327Cable: cable is ELM327 type");
            String version = "NA";

            // get the version number for posterity
            if (response.contains("v"))
            {
                int indexOfVersion = response.indexOf("v");
                version = response.substring(indexOfVersion);
                Log.i(TAG, "Elm327Cable: discovered ELM version: " + version);
                info.Version = version;
                callback.ConnectionCallbackAction("Discovered ELM version " + version);
            }

            // turn echo off
            Log.i(TAG, "Elm327Cable: turning echo off");
            callback.ConnectionCallbackAction("Turning echo off");

            response = SendCommand(Protocols.Elm327.EchoOff, 1000);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description = "Could not turn echo off...";
                Log.e(TAG, "Elm327Cable: could not turn echo off");
                return false;
            }

            info.EchoOff = true;

            callback.ConnectionCallbackAction("Setting timeout to maximum (1 second)");
            Log.i(TAG, "Elm327Cable: setting timeout to maximum (1 second)");
            response = SendCommand(Protocols.Elm327.SetTimeoutMaximum, 1000);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description = "Could not set maximum timeout";
                Log.w(TAG, "Elm327Cable: could not set maximum timeout");
                //return false;
            }

            callback.ConnectionCallbackAction("Setting headers to off");
            Log.i(TAG, "Elm327Cable: setting headers to off");
            response = SendCommand(Protocols.Elm327.SetHeadersOff, 1000);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description = "Could not turn headers off";
                Log.w(TAG, "Elm327Cable: could not turn headers off");
                //return false;
            }

            callback.ConnectionCallbackAction("Turning auto protocol on");
            Log.i(TAG, "Elm327Cable: turning auto protocol on");
            response = SendCommand(Protocols.Elm327.SetAutoProtocol, 1500);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                info.Description = "Could not set auto protocol";
                Log.e(TAG, "Elm327Cable: could not set protocol to Auto");
                return false;
            }

            callback.ConnectionCallbackAction("Forcing search for existing protocols");
            Log.i(TAG, "Elm327Cable: forcing a search for existing protocols");

            // purposely do it twice, some times the first one needs a little time to find the protocol
            SendCommand(Protocols.Elm327.ForceProtocolSearch, 3000);
            response = SendCommand(Protocols.Elm327.ForceProtocolSearch, 3000);
            if (response == null || response.isEmpty())
            {
                Log.e(TAG, "Elm327Cable: could not force an auto protocol search");
                return false;
            }

            response = SendCommand(Protocols.Elm327.DisplayProtocol, 1500);
            String chosenProtocol = response
                    .replace(Protocols.Elm327.Responses.Auto, "")
                    .replace(",", "").trim();

            callback.ConnectionCallbackAction("Chosen protocol: " + chosenProtocol);
            Log.i(TAG, "Elm327Cable: protocol chosen: " + chosenProtocol);
            if (!response.contains(Protocols.Elm327.Responses.Auto))
            {
                info.Description = "Displayed protocol did not mention auto";
                Log.w(TAG, "Elm327Cable: displayed protocol did not mention auto");
                //return false;
            }

            Protocol = Protocols.NameToProtocol(chosenProtocol);
            info.Protocol = Protocol;
            info.AutoProtocolSet = true;

            // everything is good to go
            mInitialized = true;

            // fully initialized, the fourth step is the final step
            info.Description = "Connected";
            Log.i(TAG, "Elm327Cable: connected!");
        }
        else {
            callback.ConnectionCallbackAction("Could not reset ELM device, received:\r\n" + response.replace(Protocols.Elm327.EndOfLine, "\r\n"));
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
                    Log.v(TAG, "readUntil reached expected end \"" + end + "\"");
                    break;
                }

                // increment the count and sleep for an interval
                checkCount++;
                Thread.sleep(checkInterval);

            } while (checkCount < totalChecks);
        }
        catch (Exception ex) {
            Log.w(TAG, "Timeout occurred during readUntil for \"" + end + "\"", ex);
            return null;
        }

        // if we could not read all of the data then something went wrong,
        // such as reaching the timeout when expecting a definite end to the packet
        if (checkCount >= totalChecks && end != null && !end.isEmpty()) {
            throw new TimeoutException("Timeout occurred during read");
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
            Log.i(TAG, "SendElmInitString: response: " + response);

            return response;
        }
        catch (Exception ex)
        {
            Log.w(TAG, "SendElmInitString: could not send Elm init string: " + data, ex);
            return "";
        }
    }

    protected String SendCommand(String data, int sleepMilliseconds)
    {
        return SendCommand(data, sleepMilliseconds, Protocols.Elm327.Prompt);
    }

    @Override
    public String Communicate(ParameterIdentification pid)
    {
        // only the J1850 protocol needs to set the header,
        // CAN and others do not have the same set-header commands
        if (Protocol == Protocols.Protocol.J1850)
        {
            // check if the header needs to be set
            if (pid.Header != null && !pid.Header.isEmpty())
            {
                if (!lastFrameHeader.equals(pid.Header))
                {
                    String response = SendCommand(Protocols.Elm327.SetFrameHeader(pid.Header), 750);
                    if (!response.contains(Protocols.Elm327.Responses.OK))
                    {
                        Log.w(TAG, "Communicate: could not set frame header for PID\r\n" + pid.toString());
                        return null;
                    }

                    lastFrameHeader = pid.Header;
                }
            }
            else if (!lastFrameHeader.equals(Protocols.J1850.Headers.Default))
            {
                String response = SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.Default), 750);
                if (!response.contains(Protocols.Elm327.Responses.OK))
                {
                    Log.w(TAG, "Communicate: could not set default frame header for PID\r\n" + pid.toString());
                    return null;
                }

                lastFrameHeader = Protocols.J1850.Headers.Default;
            }
        }

        // send the pid value first
        if (Send(pid))
        {
            // if sending was successful then try to receive
            return Receive();
        }

        // sending did not work, return false
        Log.w(TAG, "Communicate() did not send properly, returning null");
        return null;
    }

    @Override
    public boolean Send(ParameterIdentification pid)
    {
        return Send(pid.Pack());
    }

    @Override
    public boolean Send(String data)
    {
        if (cableConnection != null && cableConnection.isConnected())
        {
            Log.i(TAG, "Send: sending data " + data);
            try {
                data += Protocols.Elm327.EndOfLine;
                cableConnection.write(data.getBytes());
                BytesSent += data.length();
                return true;
            }
            catch (Exception ex)
            {
                Log.e(TAG, "Send: error sending data \"" + data + "\"", ex);
                needsReconnect = true;
            }
        }

        return false;
    }

    @Override
    public String Receive(int timeoutMilliseconds)
    {
        try
        {
            String response = readUntil(Protocols.Elm327.Prompt, timeoutMilliseconds);
            response = response.replace(Protocols.Elm327.Prompt, ""); // remove the prompt character
            Log.i(TAG, "Receive: " + response);

            if (response.contains(Protocols.Elm327.Responses.NoData) ||
                response.contains(Protocols.Elm327.Responses.Searching) ||
                response.contains(Protocols.Elm327.Responses.Stopped))
            {
                Log.w(TAG, "Receive: received invalid response for a PID: \"" + response + "\"");
                return null;
            }

            BytesReceived += response.length();

            return response;
        }
        catch (Exception ex)
        {
            // do nothing, timeout occurred and null should be returned
            Log.w(TAG, "Receive: timeout attempting read of PID", ex);
        }

        return null;
    }

    @Override
    public String RequestVIN()
    {
        return mode9.RequestVIN(this);
    }

    @Override
    public void ClearTroubleCodes()
    {
        Communicate(mode4);
    }

    @Override
    public List<DiagnosticTroubleCode> RequestTroubleCodes()
    {
        List<DiagnosticTroubleCode> codes = new ArrayList<DiagnosticTroubleCode>();

        // turn adaptive timing off so we can get all of the codes without missing them on slow responses
        SendCommand(Protocols.Elm327.AdaptiveTimingOff, 1000);

        codes.addAll(mode3.RequestTroubleCodes(this));
        codes.addAll(mode7.RequestTroubleCodes(this));
        codes.addAll(modeA.RequestTroubleCodes(this));

        // turn adaptive timing back on for pids
        SendCommand(Protocols.Elm327.AdaptiveTimingOn,1000);

        return codes;
    }

    @Override
    public List<Map.Entry<DiagnosticTroubleCode, String>> RequestAllDtcStatuses()
    {
        List<Map.Entry<DiagnosticTroubleCode, String>> statuses = new ArrayList<Map.Entry<DiagnosticTroubleCode, String>>();

        SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.Default), 1000);   statuses.addAll(mode19.RequestAllDtcStatuses(this));
        SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.BCM), 1000);       statuses.addAll(mode19.RequestAllDtcStatuses(this));
        SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.PCM), 1000);       statuses.addAll(mode19.RequestAllDtcStatuses(this));
        SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.TCM), 1000);       statuses.addAll(mode19.RequestAllDtcStatuses(this));
        SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.AirBag), 1000);    statuses.addAll(mode19.RequestAllDtcStatuses(this));

        return statuses;
    }

}

package beze.link.obd2.cables;

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
import beze.link.obd2.cables.connections.SimulatedConnection;

public class Elm327CableSimulator extends Elm327Cable
{
    private static final String TAG = Globals.TAG_BASE + "Elm327CableSimulator";

    /// <summary>
    /// True if the simulator should return trouble codes and false otherwise. The default is true.
    /// </summary>
    public boolean SimulateTroubleCodes = true;

    /// <summary>
    /// Creates a new instance of CustomCable.
    /// </summary>
    /// <param name="port"> The port the cable is connected to. </param>
    /// <param name="timeoutMilliseconds"> The timeout to use for communication with the cable. </param>
    public Elm327CableSimulator()//, Elm327Cable.ConnectionCallback callback)
    {
        CableType = Type.Simulated;
        mInitialized = false; // default to false

        // default cable info
        info = new Elm327Cable.CableInfo();

        HyperLog.i(TAG, "Elm327Cable: discovering ELM version");

        // detect what type of cable is connected
        String response = "ELM327 v1.5 Simulated";
        if (response.contains(Protocols.Elm327.Header))
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
            }

            // turn echo off
            HyperLog.i(TAG, "Elm327Cable: turning echo off");

            response = "OK";
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                HyperLog.e(TAG, "Elm327Cable: could not turn echo off");
                return;
            }

            info.EchoOff = true;

            HyperLog.i(TAG, "Elm327Cable: turning auto protocol on");
            response = "OK";
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                HyperLog.e(TAG, "Elm327Cable: could not set protocol to Auto");
                return;
            }

            HyperLog.i(TAG, "Elm327Cable: forcing a search for existing protocols");
            response = "SEARCHING...";
            if (response == null || response.isEmpty())
            {
                HyperLog.e(TAG, "Elm327Cable: could not force an auto protocol search");
                return;
            }

            response = "AUTO,J1850";
            String chosenProtocol = response.replace(Protocols.Elm327.Responses.Auto, "").replace(",", "").trim();
            HyperLog.i(TAG, "Elm327Cable: protocol chosen: " + chosenProtocol);
            if (!response.contains(Protocols.Elm327.Responses.Auto))
            {
                HyperLog.e(TAG, "Elm327Cable: displayed protocol did not mention auto");
                return;
            }

            Protocol = Protocols.NameToProtocol(chosenProtocol);
            info.Protocol = Protocol;
            info.AutoProtocolSet = true;

            cableConnection = new SimulatedConnection();

            // everything is good to go
            mInitialized = true;
            mOpen = true;

            // fully initialized, the fourth step is the final step
            info.Description = "Connected!";
            HyperLog.i(TAG, "Elm327Cable: connected!");
        }
    }

    @Override
    protected String SendCommand(String data, int sleepMilliseconds)
    {
        return Protocols.Elm327.Responses.OK;
    }

    @Override
    public String Communicate(ParameterIdentification pid)
    {
        return Communicate(pid, 1500);
    }

    @Override
    public String Communicate(ParameterIdentification pid, int timeout)
    {
        // check if the header needs to be set
        if (pid.Header != null && !pid.Header.isEmpty())
        {
            String response = SendCommand(Protocols.Elm327.SetFrameHeader(pid.Header), 750);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                HyperLog.w(TAG, "Communicate: could not set frame header for PID\r\n" + pid.toString());
                return null;
            }
        }
        else if (!lastFrameHeader.equals(Protocols.J1850.Headers.Default))
        {
            String response = SendCommand(Protocols.Elm327.SetFrameHeader(Protocols.J1850.Headers.Default), 750);
            if (!response.contains(Protocols.Elm327.Responses.OK))
            {
                HyperLog.w(TAG, "Communicate: could not set default frame header for PID\r\n" + pid.toString());
                return null;
            }
        }

        return pid.SimulatedResponse(this.Protocol);
    }

    @Override
    public List<DiagnosticTroubleCode> RequestTroubleCodes()
    {
        if (SimulateTroubleCodes)
        {
            return super.RequestTroubleCodes();
        }
        else
        {
            return new ArrayList<DiagnosticTroubleCode>();
        }
    }

    @Override
    public HashMap<String, DiagnosticTroubleCode> RequestAllDtcStatuses()
    {
        if (SimulateTroubleCodes)
        {
            return super.RequestAllDtcStatuses();
        }
        else
        {
            return new HashMap<String, DiagnosticTroubleCode>();
        }
    }

}

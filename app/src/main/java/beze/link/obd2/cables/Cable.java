package beze.link.obd2.cables;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beze.link.Globals;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.Protocols;
import beze.link.obd2.cables.connections.CableConnection;


public abstract class Cable
{
    private static final String TAG = Globals.TAG_BASE + "Cable";

    public class CableInfo
    {
        public String Version;
        public boolean AutoProtocolSet;
        public Protocols.Protocol Protocol;
        public boolean EchoOff;
        public String Description;

        /// <summary>
        /// Creates a default CableInfo object.
        /// </summary>
        public CableInfo()
        {
            Version =  "";
            AutoProtocolSet = false;
            Protocol = Protocols.Protocol.None;
            EchoOff = false;
            Description = "";
        }
    }

//    public delegate void ConnectionCallback(int step, CableInfo info);

    public enum Type
    {
        PassThrough,
        Elm327,
        Simulated
    }


    public Type CableType;

    /// <summary>
    /// How many bytes have been sent to the cable.
    /// </summary>
    public long BytesSent;

    /// <summary>
    /// How many bytes have been received from the cable.
    /// </summary>
    public long BytesReceived;

    /// <summary>
    /// Gets a value indicating the open or closed status of the cable connection.
    /// </summary>
    /// <returns> True if the serial port is open; otherwise, false. The default is false. </returns>
    public boolean IsOpen() { return mOpen; }

    public boolean IsInitialized() { return mInitialized; }

    public Protocols.Protocol Protocol;

    public List<Pair> TroubleCodeDescriptions;

    protected String lastFrameHeader = "";
    protected BluetoothDevice btDevice;
    protected CableConnection cableConnection;
    public CableInfo info = null;
    protected boolean needsReconnect;

    /// <summary>
    /// Creates a new instance of Cable.
    /// </summary>
    /// <param name="timeoutMilliseconds"> The timeout to give </param>
    protected Cable()
    {
        this.cableConnection = null;
        this.btDevice = null;
        BytesSent = 0;
        BytesReceived = 0;
        needsReconnect = false;
        CableType = Type.PassThrough; // default to being a pass-through cable
        TroubleCodeDescriptions = new ArrayList<Pair>();
    }

    /// <returns> True if the cable needs to be reconnected, and false otherwise. </returns>
    synchronized
    public boolean NeedsReconnect()
    {
        return needsReconnect;
    }

    /// <summary>
    /// Closes and disposes the cable connection.
    /// </summary>
    /// <remarks>
    /// Once this function is called the cable instance can no longer be used.
    /// </remarks>
    synchronized
    public void Close()
    {
        if (cableConnection != null)
        {
            try {
                cableConnection.close();
            }
            catch (Exception ex) {
                Log.w(TAG, "Close: could not properly close cable connection socket");
                ex.printStackTrace();
            }
            finally {
                cableConnection = null;
            }
        }
    }

    /// <summary>
    /// Communicates a parameter identification to the cable.
    /// </summary>
    /// <param name="pid"> The ParameterIdentification object to communicate to the cable. </param>
    /// <returns> The response if one is expected, and null otherwise. </returns>
    public abstract String Communicate(ParameterIdentification pid);

    /// <summary>
    /// Communicates a parameter identification to the cable.
    /// </summary>
    /// <param name="pid"> The ParameterIdentification object to communicate to the cable. </param>
    /// <param name="timeout"> The timeout, in milliseconds, to wait until force returning. </param>
    /// <returns> The response if one is expected, and null otherwise. </returns>
    public abstract String Communicate(ParameterIdentification pid, int timeout);

    /// <summary>
    /// Sends the given data bytes through the cable.
    /// </summary>
    /// <param name="pid"> The ParameterIdentification object to send. </param>
    /// <returns> True if the data was sent successfully and false otherwise. </returns>
    public abstract boolean Send(ParameterIdentification pid);

    /// <summary>
    /// Sends the given data bytes through the cable.
    /// </summary>
    /// <param name="data"> The data to send to the cable. </param>
    /// <returns> True if the data was sent successfully and false otherwise. </returns>
    public abstract boolean Send(String data);

    /// <summary>
    /// Receives data from the cable.
    /// </summary>
    /// <param name="timeoutMilliseconds"> The timeout to use instead of the one passed through the constructor. </param>
    /// <returns> The response if one is expected, and null otherwise. </returns>
    public abstract String Receive(int timeoutMilliseconds);

    synchronized
    public String Receive() { return this.Receive(1500); }

    /// <summary>
    /// Requests the PCM clears trouble codes from the vehicle.
    /// </summary>
    public abstract void ClearTroubleCodes();

    /// <summary>
    /// Requests diagnostic trouble codes from the vehicle.
    /// </summary>
    /// <returns> The list of diagnostic trouble codes as Strings, e.g. "P0176". </returns>
    public abstract List<DiagnosticTroubleCode> RequestTroubleCodes();

    /// <summary>
    /// Requests diagnostic trouble code statuses from the vehicle.
    /// </summary>
    /// <returns> The list of diagnostic trouble code and their associated statuses. </returns>
    public abstract HashMap<String, DiagnosticTroubleCode> RequestAllDtcStatuses();

    /// <summary>
    /// Requests the VIN from the ECU.
    /// </summary>
    /// <returns> The VIN as a String. </returns>
    public abstract String RequestVIN();


    protected boolean mInitialized = false;
    protected boolean mOpen = false;

}


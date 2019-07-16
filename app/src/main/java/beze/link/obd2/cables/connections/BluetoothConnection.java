package beze.link.obd2.cables.connections;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import beze.link.Globals;

public class BluetoothConnection extends CableConnection
{
    private static final String TAG = Globals.TAG_BASE + "BluetoothConnection";
    protected BluetoothSocket socket;
    protected BluetoothDevice device;
    protected InputStream inStream;
    protected OutputStream outStream;

    public BluetoothConnection(BluetoothDevice device)
    {
        cableType = Type.Bluetooth;

        try
        {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            this.device = device;
            socket = device.createRfcommSocketToServiceRecord(uuid);
            if (connect())
            {
                inStream = socket.getInputStream();
                outStream = socket.getOutputStream();
            }
            else
            {
                inStream = null;
                outStream = null;
                HyperLog.w(TAG, "Could not connect to device");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean connect()
    {
        try
        {
            socket.connect();
            return true;
        }
        catch (Exception e)
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                }catch (Exception inner) {} // do nothing, just try to close the socket
            }

            // assign the socket object to null no matter what, something bad happened
            socket = null;

            HyperLog.v(TAG, "Failed to connect bluetooth socket", e);
            return false;
        }
    }

    @Override
    public void close()
    {

    }

    @Override
    public int available()
    {
        try
        {
            if (isConnected() && inStream != null)
            {
                return inStream.available();
            }
            else
            {
                HyperLog.w(TAG,"input stream is null or not connected");
            }
        }
        catch (Exception e)
        {
            HyperLog.e(TAG, "Could not get input stream available size", e);
        }

        return 0;
    }

    @Override
    public boolean isConnected()
    {
        return socket != null && socket.isConnected();
    }

    @Override
    public int read(byte[] buffer)
    {
        try
        {
            if (inStream != null)
            {
                return inStream.read(buffer);
            }
            else
            {
                HyperLog.w(TAG, "input stream is null");
            }
        }
        catch (Exception e)
        {
            HyperLog.e(TAG, "Cannot read from socket", e);
        }

        return 0;
    }

    @Override
    public void write(byte[] buffer)
    {
        try
        {
            if (outStream != null)
            {
                outStream.write(buffer);
            }
            else
            {
                HyperLog.w(TAG, "output stream is null");
            }
        }
        catch (Exception e)
        {
            HyperLog.e(TAG, "Could not write to socket", e);
        }
    }
}

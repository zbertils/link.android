package beze.link.obd2.cables;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

import beze.link.Globals;
import beze.link.obd2.cables.connections.BluetoothConnection;

public class Elm327BluetoothCable extends Elm327Cable
{
    private static final String TAG = Globals.TAG_BASE + "Elm327BluetoothCable";

    public Elm327BluetoothCable(BluetoothDevice btDevice, IConnectionCallback callback)
    {
        super(callback);
        cableConnection = new BluetoothConnection(btDevice);
        if (cableConnection.isConnected())
        {
            mOpen = true;
            initialize();
        }
        else
        {
            mOpen = false;
        }
    }

}

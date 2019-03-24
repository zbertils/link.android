package beze.link.util;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import beze.link.Globals;

public class ConnectDeviceWorker extends WorkerThread
{

    private static final String TAG = Globals.TAG_BASE + "ConnectDeviceWorker";

    @Override
    protected void doWork()
    {
        boolean work = true;
        do
        {
            if (Globals.cable == null || !Globals.cable.IsOpen())
            {
                Globals.cable = null;
                Set<BluetoothDevice> pairedDevices = Globals.btAdapter.getBondedDevices();
                if (pairedDevices.size() > 0)
                {
                    List<String> btBondedDevices = new ArrayList<String>();
                    for (BluetoothDevice device : pairedDevices)
                    {
                        btBondedDevices.add(device.getName());
                    }
                }
            }

            try
            {
                Thread.sleep(500);
            }
            catch (Exception ex)
            {
                work = false;
                Log.e(TAG, "Exception sleeping, exiting connection thread", ex);
            }

        } while (work);

    }

}

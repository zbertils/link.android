package beze.link.util;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import beze.link.Globals;
import beze.link.fragments.CableInteractionFragment;
import beze.link.obd2.cables.Cable;
import beze.link.obd2.cables.Elm327BluetoothCable;

public class ConnectDeviceWorker extends WorkerThread
{

    private static final String TAG = Globals.TAG_BASE + "ConnectDeviceWorker";
    private String deviceName = null;


    public void start(String deviceName)
    {
        this.deviceName = deviceName;
        super.start();
    }

    @Override
    protected void doWork()
    {
        boolean work = true;
        do
        {
            if (Globals.cable == null || !Globals.cable.IsOpen())
            {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Globals.mainActivity.getApplicationContext());
                boolean simulateData = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_SIMULATE_DATA, true);

                if (!simulateData)
                {
                    // find the BluetoothDevice object associated with the last connect
                    BluetoothDevice selectedDevice = null;
                    Set<BluetoothDevice> pairedDevices = Globals.btAdapter.getBondedDevices();
                    for (BluetoothDevice device : pairedDevices)
                    {
                        String name = device.getName();
                        if (name.contains(deviceName))
                        {
                            selectedDevice = device;
                            Log.d(TAG, "connectCable: selected device was " + device.getName());
                            break;
                        }
                    }

                    if (selectedDevice != null)
                    {
                        int attemptCount = 0;

                        do
                        {
                            Cable newCable = null;
                            try
                            {
                                // close any previously existing connection
                                if (newCable != null)
                                {
                                    newCable.Close();
                                    newCable = null;
                                }

                                newCable = new Elm327BluetoothCable(selectedDevice, null);
                                if (newCable.IsInitialized())
                                {
                                    // toast needs to be run on the ui thread
                                    Globals.mainActivity.runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            Toast.makeText(Globals.mainActivity, "Successfully connected to " + deviceName, Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    Globals.cable = newCable;

                                    Log.i(TAG, "Connection to " + deviceName + " successful");
                                    break;
                                }
                            }
                            catch (Exception ex)
                            {
                                Log.e(TAG, "connectCable: could not connect to remote device (attempt " + (attemptCount + 1), ex);
                                ex.printStackTrace();

                                if (newCable != null)
                                {
                                    newCable.Close();
                                    newCable = null;
                                }
                            }
                            ++attemptCount;
                        } while (attemptCount < 3);
                    }
                }

                // simulated data, create and "connect" the device
                else
                {
                    Globals.connectSimulatedCable();
                }
            }

            // if the cable was connected then call the active fragment's callback
            if (Globals.cable != null)
            {
                CableInteractionFragment cableFragment = Globals.currentCableStateCallback.get();
                if (cableFragment != null)
                {
                    cableFragment.onCableStateChanged();
                }

                Globals.startPidValidationWorker(); // start validating pids right away
            }

            // already connected, just sleep for a little bit in case reconnection becomes necessary
//            else
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (Exception ex)
                {
                    work = false;
                    Log.e(TAG, "Exception sleeping, exiting connection thread", ex);
                }
            }

        } while (work);

    }

}

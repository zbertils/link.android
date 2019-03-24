package beze.link.fragments;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.android.beze.link.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import beze.link.Globals;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener
{

    private static final String TAG = Globals.TAG_BASE + ".Settings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey)
    {
        setPreferencesFromResource(R.xml.activity_settings, rootKey);
        ListPreference connectionDevice = (ListPreference) findPreference("connection_device");

        // set the on click listener for the connection device setting,
        // this will be used to dynamically show the connected device below the setting title
        connectionDevice.setOnPreferenceChangeListener(this);

        // dynamically build the connectable devices, this list could change between uses
        if (connectionDevice != null)
        {
            Set<BluetoothDevice> pairedDevices = Globals.btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0)
            {
                List<String> btBondedDevices = new ArrayList<String>();
                for (BluetoothDevice device : pairedDevices)
                {
                    btBondedDevices.add(device.getName());
                }

                CharSequence[] entries = btBondedDevices.toArray(new CharSequence[btBondedDevices.size()]);
                CharSequence[] entryValues = btBondedDevices.toArray(new CharSequence[btBondedDevices.size()]);

                connectionDevice.setEntries(entries);
                connectionDevice.setEntryValues(entryValues);

                Log.v(TAG, "Setting connectable devices: " + entryValues.toString());
            }
            else
            {
                Log.i(TAG, "No paired bluetooth devices");
            }

            // set the summary if a device is selected on start
            CharSequence selectedDevice = connectionDevice.getEntry();
            if (selectedDevice != null)
            {
                connectionDevice.setSummary(selectedDevice);
            }
            else
            {
                connectionDevice.setSummary("None selected");
            }
        }
        else
        {
            Log.e(TAG, "Could not get connection device preference object");
        }
    }

    public boolean onPreferenceChange(Preference pref, Object obj)
    {
        if (pref.equals(findPreference("connection_device")))
        {
            // set the summary if a device is selected
            ListPreference connectionDevice = (ListPreference) findPreference("connection_device");
            if (connectionDevice != null)
            {
                Log.i(TAG, "Setting summary to " + obj.toString());
                connectionDevice.setSummary(obj.toString());
            }
            else
            {
                Log.w(TAG,"Could not get connection device object");
            }
        }
        else
        {
            Log.i(TAG,"Given preference argument did not match a preference object");
        }

        return true; // true to update the preference with a new value, false otherwise
    }

}

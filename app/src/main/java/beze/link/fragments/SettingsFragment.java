package beze.link.fragments;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.app.PendingIntent;

import com.android.beze.link.MainActivity;
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

    public static class GraphSize
    {
        public static final String Small = "250";
        public static final String Medium = "500";
        public static final String Large = "750";

        public static String valueToString(String value)
        {
            switch (value)
            {
                case Small : return "Small";
                case Medium : return "Medium";
                case Large : return "Large";
                default : return "Small";
            }
        }
    }

    public static class GraphLength
    {
        public static final String Small = "100";
        public static final String Medium = "250";
        public static final String Large = "500";

        public static String valueToString(String value)
        {
            switch (value)
            {
                case Small : return "Small";
                case Medium : return "Medium";
                case Large : return "Large";
                default : return "Small";
            }
        }
    }

    private static final String TAG = Globals.TAG_BASE + ".Settings";

    DialogInterface.OnClickListener restartAlertClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // restart application
                    Intent mStartActivity = new Intent(getView().getContext(), MainActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(getView().getContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) getView().getContext().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // do nothing, user declined restart
                    break;
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey)
    {
        setPreferencesFromResource(R.xml.activity_settings, rootKey);
        ListPreference connectionDevice = (ListPreference) findPreference("connection_device");
        SwitchPreferenceCompat simulatePref = (SwitchPreferenceCompat) findPreference("pref_simulate_data");
        ListPreference graphSizesPref = (ListPreference) findPreference("pref_graph_sizes");
        ListPreference graphLengthsPref = (ListPreference) findPreference("pref_graph_lengths");

        // set the on click listener for the connection device setting,
        // this will be used to dynamically show the connected device below the setting title
        connectionDevice.setOnPreferenceChangeListener(this);
        simulatePref.setOnPreferenceChangeListener(this);
        graphSizesPref.setOnPreferenceChangeListener(this);
        graphLengthsPref.setOnPreferenceChangeListener(this);

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

        // build the possible graph sizes list
        if (graphSizesPref != null)
        {
            CharSequence[] entries =
                {
                        GraphSize.valueToString(GraphSize.Small),
                        GraphSize.valueToString(GraphSize.Medium),
                        GraphSize.valueToString(GraphSize.Large)
                };

            CharSequence[] entryValues =
                {
                        GraphSize.Small,
                        GraphSize.Medium,
                        GraphSize.Large
                };
            
            graphSizesPref.setEntries(entries);
            graphSizesPref.setEntryValues(entryValues);

            CharSequence selectedValue = graphSizesPref.getEntry();
            if (selectedValue != null)
            {
                graphSizesPref.setSummary(selectedValue);
            }
            else
            {
                graphSizesPref.setValue(GraphSize.Large);
                graphSizesPref.setSummary("Large");
            }
        }

        // build the possible graph lengths list
        if (graphLengthsPref != null)
        {
            CharSequence[] entries =
                {
                    GraphLength.valueToString(GraphLength.Small),
                    GraphLength.valueToString(GraphLength.Medium),
                    GraphLength.valueToString(GraphLength.Large)
                };

            CharSequence[] entryValues =
                {
                        GraphLength.Small,
                        GraphLength.Medium,
                        GraphLength.Large
                };

            graphLengthsPref.setEntries(entries);
            graphLengthsPref.setEntryValues(entryValues);

            CharSequence selectedValue = graphLengthsPref.getEntry();
            if (selectedValue != null)
            {
                graphLengthsPref.setSummary(selectedValue);
            }
            else
            {
                graphLengthsPref.setValue(GraphLength.Medium);
                graphLengthsPref.setSummary("Medium");
            }
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
        else if (pref.equals(findPreference("pref_graph_sizes")))
        {
            // set the summary if a value is selected
            ListPreference graphSize = (ListPreference) findPreference("pref_graph_sizes");
            if (graphSize != null)
            {
                Log.i(TAG, "Setting summary to " + obj.toString());
                graphSize.setSummary(GraphSize.valueToString(obj.toString()));
            }
            else
            {
                Log.w(TAG,"Could not get graph sizes preference object");
            }
        }
        else if (pref.equals(findPreference("pref_graph_lengths")))
        {
            // set the summary if a value is selected
            ListPreference graphLengths = (ListPreference) findPreference("pref_graph_lengths");
            if (graphLengths != null)
            {
                Log.i(TAG, "Setting summary to " + obj.toString());
                graphLengths.setSummary(GraphLength.valueToString(obj.toString()));
            }
            else
            {
                Log.w(TAG,"Could not get graph length preference object");
            }
        }
        else if (pref.equals(findPreference("pref_simulate_data")))
        {
            Log.v(TAG, "Asking user to restart app");
            AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
            builder.setMessage("Turning simulation on or off requires restarting the app.\r\n\r\nRestart now?")
                    .setPositiveButton("Yes", restartAlertClickListener)
                    .setNegativeButton("No", restartAlertClickListener)
                    .show();
        }
        else
        {
            Log.i(TAG,"Given preference argument did not match a preference object");
        }

        return true; // true to update the preference with a new value, false otherwise
    }

}

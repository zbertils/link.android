package beze.link.fragments;


import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.support.v7.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import beze.link.Globals;
import beze.link.R;
import beze.link.obd2.Protocols;
import beze.link.obd2.cables.Elm327Cable;
import beze.link.obd2.cables.IConnectionCallback;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends Fragment implements AdapterView.OnItemSelectedListener, AdapterView.OnClickListener, IConnectionCallback, Runnable
{

    private static final String TAG = Globals.TAG + "ConnectFragment";
    private String selectedItemName = "";
    private static final String SimulatedCableName = "SIMULATED CABLE";


    public ConnectFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void ConnectionCallbackAction(final String description)
    {
        Globals.mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
                status.setText(description);
                status.invalidate();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        return view;
    }

    private void setDisconnectedState()
    {
        TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
        TextView version = (TextView) getActivity().findViewById(R.id.textViewConnectDeviceVersion);
        TextView protocol = (TextView) getActivity().findViewById(R.id.textViewConnectProtocol);
        Button btnConnect = (Button) getActivity().findViewById(R.id.btnConnect);

        btnConnect.setText("Connect");
        btnConnect.setBackgroundColor(Color.LTGRAY);
        status.setText("Disconnected");
        version.setText("NA");
        protocol.setText("NA");
        status.setTextColor(Color.BLACK);
    }

    private void setConnectedState()
    {
        TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
        TextView version = (TextView) getActivity().findViewById(R.id.textViewConnectDeviceVersion);
        TextView protocol = (TextView) getActivity().findViewById(R.id.textViewConnectProtocol);
        Button btnConnect = (Button) getActivity().findViewById(R.id.btnConnect);

        version.setText(Globals.cable.info.Version);
        protocol.setText(Globals.cable.info.Protocol.toString());
        status.setText(Globals.cable.info.Description.replace(Protocols.Elm327.EndOfLine, "\r\n"));

        btnConnect.setText("DISCONNECT");
        btnConnect.setBackgroundColor(Color.GREEN);
        status.setTextColor(Color.GREEN);
    }

    @Override
    public void run()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean simulateData = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_SIMULATE_DATA, true);

        final TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
        final TextView version = (TextView) getActivity().findViewById(R.id.textViewConnectDeviceVersion);
        final TextView protocol = (TextView) getActivity().findViewById(R.id.textViewConnectProtocol);
        final Button btnConnect = (Button) getActivity().findViewById(R.id.btnConnect);

        String buttonState = btnConnect.getText().toString().toUpperCase();
        if (buttonState.equals("CONNECT"))
        {
            if (!simulateData)
            {

                BluetoothDevice selectedDevice = null;
                Set<BluetoothDevice> pairedDevices = Globals.btAdapter.getBondedDevices();
                for (BluetoothDevice device : pairedDevices)
                {
                    String name = device.getName();
                    if (name.equals(selectedItemName))
                    {
                        selectedDevice = device;
                        Log.d(TAG, "onClick: selected device was " + device.getName());
                        break;
                    }
                }

                if (selectedDevice != null)
                {
                    int attemptCount = 0;

                    do
                    {
                        try
                        {
                            // close any previously existing connection
                            if (Globals.cable != null)
                            {
                                Globals.cable.Close();
                                Globals.cable = null;
                            }

                            Globals.cable = new Elm327Cable(selectedDevice, this);
                            if (Globals.cable.IsInitialized())
                            {
                                Globals.appState.LastConnectedDeviceName = selectedDevice.getName();
                                break;
                            }
                        } catch (Exception ex)
                        {
                            Log.e(TAG, "onClick: could not connect to remote device");
                            ex.printStackTrace();

                            if (Globals.cable != null)
                            {
                                Globals.cable.Close();
                                Globals.cable = null;
                            }
                        }
                        attemptCount++;
                    } while (!Globals.cable.IsInitialized() && attemptCount < 3);
                }
            }

            // simulated data, create and "connect" the device
            else
            {
                Globals.appState.LastConnectedDeviceName = SimulatedCableName;
                Globals.connectSimulatedCable();
            }

            if (Globals.cable != null)
            {
                if (Globals.cable.IsInitialized())
                {
                    Globals.mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(Globals.appContext, "Connected to \"" + Globals.appState.LastConnectedDeviceName + "\" successfully!", Toast.LENGTH_LONG).show();
                            setConnectedState();
                        }
                    });


                } else if (Globals.cable.IsOpen())
                {
                    Globals.mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            status.setText("Uninitialized");
                            Toast.makeText(Globals.appContext, "WARNING: Connected but not initialized!", Toast.LENGTH_LONG).show();
                        }
                    });
                } else
                {
                    Globals.mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(Globals.appContext, "ERROR: Could not connect to remote device!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }

        // disconnecting the device
        else
        {
            if (Globals.cable != null)
            {
                Globals.cable.Close();
                Globals.cable = null;
            }

            setDisconnectedState();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // set the onClick listener programmatically, if it is in the xml it needs to be in the activity source,
        // it is easier and better maintained to stay in the fragment the button belongs to
        Button connectButton = getActivity().findViewById(R.id.btnConnect);
        connectButton.setOnClickListener(this);

        Spinner connectSpinner = (Spinner) getActivity().findViewById(R.id.spinnerBtDevices);
        boolean lastConnectedDeviceStillExists = false;

        Set<BluetoothDevice> pairedDevices = Globals.btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            List<String> btBondedDevices = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices)
            {
                btBondedDevices.add(device.getName());
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, btBondedDevices);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            connectSpinner.setAdapter(spinnerAdapter);
            connectSpinner.setOnItemSelectedListener(this);

            // set the last connected device if it's still available
            int lastSelectedIndex = btBondedDevices.indexOf(Globals.appState.LastConnectedDeviceName);
            if (lastSelectedIndex >= 0)
            {
                selectedItemName = Globals.appState.LastConnectedDeviceName; // set the selected item name since it still exists
                connectSpinner.setSelection(lastSelectedIndex);
                lastConnectedDeviceStillExists = true;
            }
        } else
        {
            Log.w(TAG, "onStart: no paired devices found");
        }

        // setup the view based on the current state
        TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
        if (Globals.cable != null)
        {
            if (Globals.cable.IsInitialized())
            {
                setConnectedState();
            } else if (Globals.cable.IsOpen())
            {
                status.setText("Uninitialized");
                Toast.makeText(Globals.appContext, "WARNING: Connected but not initialized!", Toast.LENGTH_LONG).show();
            } else
            {
                Toast.makeText(Globals.appContext, "ERROR: Could not connect to remote device!", Toast.LENGTH_LONG).show();
            }
        }

        // not connected, set disconnected state and then attempt reconnect if user selected the option to in settings
        else
        {
            setDisconnectedState();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            boolean reconnect = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_RECONNECT_AT_START, true);
            if (reconnect)
            {
                // reconnect if the device still exists (still paired), or the last device was a simulated cable
                if (lastConnectedDeviceStillExists || Globals.appState.LastConnectedDeviceName.equals(SimulatedCableName))
                {

                    Toast.makeText(Globals.appContext, "Reconnecting to " + Globals.appState.LastConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    Button btnConnect = (Button) getActivity().findViewById(R.id.btnConnect);
                    btnConnect.performClick();
                }
            }
            else if (!Globals.appState.LastConnectedDeviceName.isEmpty())
            {
                Toast.makeText(Globals.appContext, "Device '" + Globals.appState.LastConnectedDeviceName + "' no longer exists", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
    {
        String item = adapter.getItemAtPosition(position).toString();
        selectedItemName = item;

        Log.i(TAG, "onItemSelected: selected position: " + position);
        Log.i(TAG, "onItemSelected: selected item name: " + item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> var1)
    {
        Log.i(TAG, "onNothingSelected: no bluetooth device was selected, doing nothing");
    }

    @Override
    public void onClick(View view)
    {
        Thread connect = new Thread(this);
        connect.start();
    }

}

package beze.link.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.beze.link.BuildConfig;
import com.android.beze.link.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import beze.link.Globals;
import beze.link.obd2.Protocols;
import beze.link.obd2.cables.IConnectionCallback;


public class ConnectBluetoothFragment extends Fragment implements AdapterView.OnClickListener, IConnectionCallback, Runnable
{

    private static final String TAG = Globals.TAG_BASE + "ConnectBluetoothFragment";

    public ConnectBluetoothFragment()
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
                FragmentActivity act = getActivity();
                if (act != null)
                {
                    TextView status = (TextView) act.findViewById(R.id.textViewConnectStatus);
                    status.setText(description);
                    status.invalidate();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect_bluetooth, container, false);

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
        protocol.setText(Globals.cable.info.ProtocolName);
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
        final String connection_device = sharedPref.getString(Globals.Preferences.KEY_PREF_BLUETOOTH_DEVICE, null);

        final TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
        final TextView version = (TextView) getActivity().findViewById(R.id.textViewConnectDeviceVersion);
        final TextView protocol = (TextView) getActivity().findViewById(R.id.textViewConnectProtocol);
        final Button btnConnect = (Button) getActivity().findViewById(R.id.btnConnect);

        String buttonState = btnConnect.getText().toString().toUpperCase();
        if (buttonState.equals("CONNECT"))
        {
            Globals.connectCable(connection_device, this);

            if (Globals.cable != null)
            {
                if (Globals.cable.IsInitialized())
                {
                    Globals.mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(Globals.appContext, "Connected to \"" + connection_device + "\" successfully!", Toast.LENGTH_LONG).show();

                            if (Globals.cable.info != null)
                            {
                                TextView debugText = (TextView) getActivity().findViewById(R.id.textViewConnectDebug);
                                debugText.setText(Globals.cable.info.Description);
                            }
                            setConnectedState();
                        }
                    });
                }
                else if (Globals.cable.IsOpen())
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
                }
                else
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
            Globals.disconnectCable();

            Globals.mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    setDisconnectedState();
                }
            });
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // the debug text should be displayed if in debug build, and invisible otherwise
        TextView debugText = (TextView) getActivity().findViewById(R.id.textViewConnectDebug);
        Button connectButton = getActivity().findViewById(R.id.btnConnect);

        if (debugText == null || connectButton == null)
        {
            Log.w(TAG, "Cannot start connect fragment, one of the GUI objects is null");
            return;
        }

        if (BuildConfig.DEBUG)
        {
            debugText.setVisibility(View.VISIBLE);
        }
        else
        {
            debugText.setVisibility(View.INVISIBLE);
        }

        // set the onClick listener programmatically, if it is in the xml it needs to be in the activity source,
        // it is easier and better maintained to stay in the fragment the button belongs to
        connectButton.setOnClickListener(this);

        // setup the view based on the current state
        TextView status = (TextView) getActivity().findViewById(R.id.textViewConnectStatus);
        if (Globals.cable != null)
        {
            if (Globals.cable.IsInitialized())
            {
                setConnectedState();
            }
            else if (Globals.cable.IsOpen())
            {
                status.setText("Uninitialized");
                Toast.makeText(Globals.appContext, "WARNING: Connected but not initialized!", Toast.LENGTH_LONG).show();
            }
            else
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
            boolean simulation = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_SIMULATE_DATA, true);
            final String connection_device = sharedPref.getString(Globals.Preferences.KEY_PREF_BLUETOOTH_DEVICE, null);
            if (reconnect)
            {
                // reconnect if the device still exists (still paired), or the last device was a simulated cable
                if (simulation)
                {
                    Toast.makeText(Globals.appContext, "Reconnecting to " + connection_device, Toast.LENGTH_SHORT).show();
                    connectButton.performClick();
                }
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        Thread connect = new Thread(this);
        connect.start();
    }
}

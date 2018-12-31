package beze.link.util;

import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.util.List;

import beze.link.Globals;
import com.android.beze.link.R;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.cables.Cable;

public class UpdatePidsWorker extends WorkerThread
{
    private final static String TAG = Globals.TAG_BASE + "UpdatePidsWorker";

    public UpdatePidsWorker(Cable obdCable, List<ParameterIdentification> pids, List<Pair<Double, ParameterIdentification>> decodedValues)
    {
        this.cable = obdCable;
        this.pids = pids;
        this.decodedValues = decodedValues;
    }


    @Override
    protected void doWork()
    {
        try
        {
            // default to the global cable if not given one
            if (cable == null)
            {
                cable = Globals.cable;
            }

            if (cable != null && cable.IsOpen() && pids.size() > 0)
            {
                while (!stopWork)
                {
                    // only iterate over the pids that are being logged
                    for (ParameterIdentification pid : pids)
                    {
                        String data = cable.Communicate(pid);
                        if (data != null && !data.isEmpty())
                        {
                            double value = pid.Unpack(data);
                            pid.setLastDecodedValue(value);
                        }
                        else
                        {
                            Log.w(TAG, "Could not update PID " + pid.getShortName());
                            pid.setLastDecodedValue(Double.NaN);
                        }

                        // if this is not null then tell the screen to update,
                        // this should be set around the time the thread starts
                        if (Globals.dataFragmentAdapter != null)
                        {
                            Globals.mainActivity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Globals.dataFragmentAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                        if (stopWork)
                        {
                            break;
                        }
                    }

                    // check if the cable connection is still good
                    if (Globals.cable.NeedsReconnect())
                    {
                        Toast.makeText(Globals.appContext, "", Toast.LENGTH_LONG).show();
                        Snackbar.make(Globals.mainActivity.findViewById(R.id.nav_view),
                                "ELM327 device not responding\nAttempting reconnect",
                                Snackbar.LENGTH_LONG).setAction("Action", null)
                                .show();

                        Globals.disconnectCable();
                        Globals.connectCable(Globals.appState.LastConnectedDeviceName, null);

                        if (!Globals.cable.IsInitialized() || !Globals.cable.IsOpen())
                        {
                            Log.e(TAG, "Could not reconnect to device " + Globals.appState.LastConnectedDeviceName);
                            stop();
                            Snackbar.make(Globals.mainActivity.findViewById(R.id.nav_view),
                                    "Could not auto reconnect device\nTry unplugging the device",
                                    Snackbar.LENGTH_LONG).setAction("Action", null)
                                    .show();
                        }
                    }
                }
            }
            else
            {
                Snackbar.make(Globals.mainActivity.findViewById(R.id.nav_view), "No PIDs selected!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                stop();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "doWork: encountered an error");
            Snackbar.make(Globals.mainActivity.findViewById(R.id.nav_view), "doWork: encountered an error", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            e.printStackTrace();
        }
    }

    /// <summary>
/// Sets the cable used by the worker thread.
/// </summary>
/// <param name="cable"> The cable to use. </param>
/// <remarks>
/// This function stops the worker thread if it is running and does not restart it.
/// </remarks>
    public void SetCable(Cable cable)
    {
        stop();
        join();
        this.cable = cable;
    }

    /// <summary>
/// Sets the PID list used by the worker thread.
/// </summary>
/// <param name="pids"> The PID list to use. </param>
/// <remarks>
/// This function stops the worker thread if it is running and does not restart it.
/// </remarks>
    public void SetPids(List<ParameterIdentification> pids)
    {
        stop();
        join();
        this.pids = pids;
    }

    private Cable cable = null;
    private List<ParameterIdentification> pids = null;
    private List<Pair<Double, ParameterIdentification>> decodedValues = null;

}

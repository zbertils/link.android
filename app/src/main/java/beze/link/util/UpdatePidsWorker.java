package beze.link.util;

import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
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

    public UpdatePidsWorker(List<ParameterIdentification> pids, List<Pair<Double, ParameterIdentification>> decodedValues)
    {
        this.pids = pids;
        this.decodedValues = decodedValues;
    }


    @Override
    protected void doWork()
    {
        try
        {
            if (pids.size() > 0)
            {
                while (!stopWork)
                {
                    if (Globals.cable != null)
                    {
                        // only iterate over the pids that are being logged
                        for (ParameterIdentification pid : pids)
                        {
                            if (pid != null)
                            {
//                                Log.v(TAG, "Fetching pid data for " + pid.Name);

                                // only attempt to communicate the pid if it is supported
                                if (pid.Supported != null && pid.Supported)
                                {
                                    String data = Globals.cable.Communicate(pid);
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
                                }
                            }
                            else
                            {
                                Log.w(TAG, "Trying to fetch null pid object");
                            }

                            if (stopWork)
                            {
                                break;
                            }
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
            Log.e(TAG, "doWork: encountered an error", e);
            e.printStackTrace();
        }
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

    private List<ParameterIdentification> pids = null;
    private List<Pair<Double, ParameterIdentification>> decodedValues = null;

}

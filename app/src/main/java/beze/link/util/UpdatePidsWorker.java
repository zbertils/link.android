package beze.link.util;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.util.List;

import beze.link.Globals;
import beze.link.R;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.cables.Cable;
import beze.link.obd2.cables.Elm327Cable;

import static beze.link.Globals.dataFragmentAdapter;

public class UpdatePidsWorker extends WorkerThread
{
    private final static String TAG = Globals.TAG + "UpdatePidsWorker";

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

//                            // if trying to log to file and the concurrent queue is available then do so
//                            // TODO
//                            if (Properties.Settings.Default.LogToFile)
//                            {
//                                if (decodedValues != null)
//                                {
//                                    //        decodedValues.Add(new Tuple<double, ParameterIdentification>(value, pid));
//                                    // FIXME
//                                }
//                                else if (decodedValues == null)
//                                {
//                                    //        Diagnostics.DiagnosticLogger.Log("Trying to save a decoded value onto concurrent queue that is null!");
//                                    Log.i(TAG, "doWork: trying to save a decoded value onto list that is null!");
//                                }
//                            }
                        }
                        else
                        {
                            Log.w(TAG, "Could not update PID " + pid.getShortName());
                            pid.setLastDecodedValue(Double.NaN);
                        }

                        // if this is not null then tell the screen to update,
                        // this should be set around the time the thread starts
                        if (Globals.dataFragmentAdapter != null) {
                            Globals.mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
                        Globals.cable.Close();
                        Globals.cable = null;
                        Toast.makeText(Globals.appContext, "ELM327 device stopped responding", Toast.LENGTH_LONG).show();
                        stopWork = true;
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

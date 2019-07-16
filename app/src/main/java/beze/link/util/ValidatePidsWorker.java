package beze.link.util;

import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

import java.util.logging.Logger;

import beze.link.Globals;
import beze.link.obd2.ParameterIdentification;

public class ValidatePidsWorker extends WorkerThread
{
    private static final String TAG = Globals.TAG_BASE + "ValidatePidsThread";

    @Override
    protected void doWork()
    {
        // wait for the
        while (Globals.cable == null)
        {
            try
            {
                Thread.sleep(1000);

                if (stopWork)
                {
                    return;
                }
            }
            catch (Exception e)
            {
                HyperLog.e(TAG, "Failed to sleep", e);
                return;
            }
        }

        try
        {
            // make sure the cable is ready to use
            if (Globals.cable != null)
            {
                for (ParameterIdentification pid : Globals.allPids)
                {
                    if (stopWork)
                    {
                        return;
                    }

                    // only update ones that have not already been validated
                    if (pid.Supported == null)
                    {
                        // default to unsupported, if a valid number is returned it will be updated
                        pid.Supported = false;

                        // determine if this pid is supported by communicating it
                        String ret = Globals.cable.Communicate(pid);
                        if (ret != null && !ret.isEmpty())
                        {
                            // check the unpacked number against possible error numbers
                            double value = pid.Unpack(ret);
                            if (!Double.isInfinite(value) &&
                                !Double.isNaN(value))
                            {
                                pid.Supported = true;
                            }
                        }

                        // update the pids view adapter if it is available
                        if (Globals.pidsFragmentAdapter != null)
                        {
                            Globals.mainActivity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Globals.pidsFragmentAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            HyperLog.e(TAG, "Failed to update supported state for pids", e);
        }
    }
}

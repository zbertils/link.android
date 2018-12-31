package beze.link.util;

import android.util.Log;

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
        while (Globals.cable == null || !Globals.cable.IsInitialized())
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failed to sleep", e);
                return;
            }
        }

        // make sure the cable is ready to use
        if (Globals.cable != null && Globals.cable.IsInitialized())
        {
            for (ParameterIdentification pid : Globals.allPids)
            {
                // determine if this pid is supported by communicating it
                String ret = Globals.cable.Communicate(pid);
                pid.Supported = ret != null && !ret.isEmpty();

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

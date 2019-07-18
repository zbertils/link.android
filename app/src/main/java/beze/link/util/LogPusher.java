package beze.link.util;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.hypertrack.hyperlog.HyperLog;

import beze.link.Globals;

public class LogPusher extends WorkerThread
{
    private final String TAG = Globals.TAG_BASE + "LogPusher";
    long maxLogCount = 500;
    LogPusherCallback callback = new LogPusherCallback();

    @Override
    protected void doWork()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Globals.mainActivity.getApplicationContext());
        boolean sendLogs = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_SEND_LOGCAT, false);
        HyperLog.v(TAG, "Logs will" + ((sendLogs) ? "" : " NOT") + " be sent to developer");

        while (!stopWork)
        {
            long count = HyperLog.getDeviceLogsCount();
            if (count > maxLogCount)
            {
                if (sendLogs)
                {
                    HyperLog.pushLogs(Globals.mainActivity, false, callback);

                    while (!callback.isResponseReceived())
                    {
                        // FIXME: there is a possibilty of losing logs if new logs are generated
                        // between the push and when the the success callback is received
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
                HyperLog.getDeviceLogs(true); // clear out old logs
                callback.reset();
            }
            else
            {
                try { Thread.sleep(100); } catch (Exception e) {}
            }
        }

        if (sendLogs)
        {
            HyperLog.pushLogs(Globals.mainActivity, false, callback);

            while (!callback.isResponseReceived())
            {
                // FIXME: there is a possibilty of losing logs if new logs are generated
                // between the push and when the the success callback is received
                try
                {
                    Thread.sleep(100);
                }
                catch (Exception e)
                {
                }
            }
        }
        HyperLog.getDeviceLogs(true); // clear out old logs
        callback.reset();

    }

}

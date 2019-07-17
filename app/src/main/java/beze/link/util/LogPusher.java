package beze.link.util;

import android.support.annotation.NonNull;

import com.hypertrack.hyperlog.HLCallback;
import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.hyperlog.error.HLErrorCode;
import com.hypertrack.hyperlog.error.HLErrorResponse;

import beze.link.Globals;

public class LogPusher extends WorkerThread
{

    long maxLogCount = 500;
    LogPusherCallback callback = new LogPusherCallback();

    @Override
    protected void doWork()
    {
        boolean waitForResponse = true;
        while (!stopWork)
        {
            long count = HyperLog.getDeviceLogsCount();
            if (count > maxLogCount)
            {
                waitForResponse = true;
                HyperLog.pushLogs(Globals.mainActivity, false, callback);

                while (!callback.isResponseReceived())
                {
                    // FIXME: there is a possibilty of losing logs if new logs are generated
                    // between the push and when the the success callback is received
                    try { Thread.sleep(100); } catch (Exception e) {}
                }
                HyperLog.getDeviceLogs(true); // clear out old logs
                callback.reset();
            }
            else
            {
                try { Thread.sleep(100); } catch (Exception e) {}
            }
        }

        waitForResponse = true;
        HyperLog.pushLogs(Globals.mainActivity, false, callback);

        while (!callback.isResponseReceived())
        {
            // FIXME: there is a possibilty of losing logs if new logs are generated
            // between the push and when the the success callback is received
            try { Thread.sleep(100); } catch (Exception e) {}
        }
        HyperLog.getDeviceLogs(true); // clear out old logs
        callback.reset();

    }

}

package beze.link.util;

import com.hypertrack.hyperlog.HyperLog;

import beze.link.Globals;

public class LogPusher extends WorkerThread
{

    long maxLogCount = 500;
    LogPusherCallback callback = new LogPusherCallback();

    @Override
    protected void doWork()
    {
        while (!stopWork)
        {
            long count = HyperLog.getDeviceLogsCount();
            if (count > maxLogCount)
            {
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

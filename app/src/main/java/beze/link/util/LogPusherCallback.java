package beze.link.util;

import android.support.annotation.NonNull;

import com.hypertrack.hyperlog.HLCallback;
import com.hypertrack.hyperlog.error.HLErrorResponse;

public class LogPusherCallback extends HLCallback
{
    private boolean responseReceived = false;

    public boolean isResponseReceived()
    {
        synchronized (LogPusherCallback.class)
        {
            return responseReceived;
        }
    }

    public void reset()
    {
        synchronized (LogPusherCallback.class)
        {
            responseReceived = false;
        }
    }

    @Override
    public void onSuccess(@NonNull Object response)
    {
        synchronized (LogPusherCallback.class)
        {
            responseReceived = true;
        }
    }

    @Override
    public void onError(@NonNull HLErrorResponse HLErrorResponse)
    {
        synchronized (LogPusherCallback.class)
        {
            responseReceived = true;
        }
    }
}

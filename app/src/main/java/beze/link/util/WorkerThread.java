package beze.link.util;

import android.util.Log;

import beze.link.Globals;

public abstract class WorkerThread implements Runnable
{
    private static final String TAG = Globals.TAG_BASE + "WorkerThread";
    private Thread workerThread;
    protected boolean stopWork;

    /// <summary>
    /// Instantiates a new instance of Worker.
    /// </summary>
    /// <param name="threadName"> The name to assign to the thread. </param>
    public WorkerThread()
    {
        // do nothing for now
        workerThread = new Thread(this);
        stopWork = false;
    }

    protected abstract void doWork();

    @Override
    public void finalize() {
        if (isAlive()) {
            stop();
            Log.e(TAG, "finalize: thread was still alive, stopping now");
        }
    }

    @Override
    public void run() {
//        while (!stopWork)
        {
            doWork();
        }
    }

    /// <summary>
    /// Starts the worker thread.
    /// </summary>
    synchronized public void start()
    {
        stopWork = false;
        workerThread.start();
    }


    /// <summary>
    /// Stops the worker thread.
    /// </summary>
    synchronized public void stop()
    {
        stopWork = true;
    }


    /// <summary>
    /// Blocks the calling thread until the worker has finished.
    /// </summary>
    synchronized public void join()
    {
        try {
            workerThread.join();
        }
        catch (Exception ex) {
            Log.e(TAG, "Join: workerThread join exception");
            ex.printStackTrace();
        }
    }


    /// <summary>
    /// Returns the state of the thread.
    /// </summary>
    synchronized public boolean isAlive()
    {
        return workerThread.isAlive();
    }


}

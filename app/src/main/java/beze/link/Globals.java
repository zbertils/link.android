package beze.link;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.cables.Cable;
import beze.link.obd2.cables.Elm327CableSimulator;
import beze.link.ui.DataRecyclerViewAdapter;
import beze.link.util.UpdatePidsWorker;

public class Globals {

    public static final String TAG = "link.dev.";
    private static final Double pidsJsonFileVersion = 1.0;

    public static Context appContext;
    public static Cable cable;
    public static List<ParameterIdentification> allPids;
    public static List<ParameterIdentification> shownPids = new ArrayList();
    public static Map<String, String> makes;
    public static Map<String, String> dtcDescriptions;
    public static RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder> dataFragmentAdapter = null;
    public static UpdatePidsWorker updateWorker = new UpdatePidsWorker(Globals.cable, Globals.shownPids, null);
    public static MainActivity mainActivity;
//    public static boolean restartWorker = false;
    public static BluetoothAdapter btAdapter;
    public static AppState appState;


    public static class Preferences {
        public static final String KEY_PREF_RECONNECT_AT_START = "pref_reconnect";
        public static final String KEY_PREF_SIMULATE_DATA = "pref_simulate_data";
        public static final String KEY_PREF_LOG_PIDS = "pref_log_pids";
        public static final String KEY_PREF_SHOW_PID_STREAM_VALUES = "pref_show_pid_stream_values";

    }


    public static String getStackTraceAndMessage(Exception ex) {
        StringWriter exWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(exWriter);
        ex.printStackTrace(printWriter);

        return ex.getMessage() + "\n\n" + exWriter.toString();
    }


    public static boolean loadPids(Activity main) {
        String jsonStr = "";
        try {
            InputStream stream = main.getAssets().open("pids.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            jsonStr = new String(buffer, "UTF-8");
        }
        catch (Exception ex) {
            Log.e(TAG, "loadPids: could not read pids.json");
            ex.printStackTrace();
            return false;
        }

        try {
            Globals.allPids = new ArrayList<ParameterIdentification>();
            JSONObject json = new JSONObject(jsonStr);
            Double version = json.getDouble("version");

            // TODO: check the version against the current and do something about it...
            if (version != pidsJsonFileVersion) {
                Log.w(TAG, String.format("loadPids: version does not match expected version, found %f expected %f", version, pidsJsonFileVersion));
            }

            // get the array of pids and parse them into ParameterIdentification objects
            JSONArray outer = json.getJSONArray("ParameterIdentification");
            for (int i = 0; i < outer.length(); i++)
            {
                JSONObject obj = outer.getJSONObject(i);
                ParameterIdentification pid = new ParameterIdentification(obj);
                Globals.allPids.add(pid);
            }

        }
        catch (Exception ex) {
            Log.e(TAG, "loadPids: could not parse pids.json");
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadDtcDescriptions(Activity main) {
        String jsonStr = "";
        try {
            InputStream stream = main.getAssets().open("dtcs.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            jsonStr = new String(buffer, "UTF-8");
        }
        catch (Exception ex) {
            Log.e(TAG, "loadDtcDescriptions: could not read dtcs.json");
            ex.printStackTrace();
            return false;
        }

        try {
            Globals.dtcDescriptions = new HashMap<String, String>();
            JSONObject json = new JSONObject(jsonStr);
            JSONObject pCodes = json.getJSONObject("P Codes");
            JSONObject bCodes = json.getJSONObject("B Codes");
            JSONObject cCodes = json.getJSONObject("C Codes");
            JSONObject uCodes = json.getJSONObject("U Codes");

            Iterator<?> pKeys = pCodes.keys();
            Iterator<?> bKeys = bCodes.keys();
            Iterator<?> cKeys = cCodes.keys();
            Iterator<?> uKeys = uCodes.keys();

            while (pKeys.hasNext())
            {
                String key = (String) pKeys.next();
                String description = pCodes.getString(key);
                Globals.dtcDescriptions.put(key, description);
            }

            while (bKeys.hasNext())
            {
                String key = (String) bKeys.next();
                String description = bCodes.getString(key);
                Globals.dtcDescriptions.put(key, description);
            }

            while (cKeys.hasNext())
            {
                String key = (String) cKeys.next();
                String description = cCodes.getString(key);
                Globals.dtcDescriptions.put(key, description);
            }

            while (uKeys.hasNext())
            {
                String key = (String) uKeys.next();
                String description = uCodes.getString(key);
                Globals.dtcDescriptions.put(key, description);
            }

        }
        catch (Exception ex) {
            Log.e(TAG, "loadDtcDescriptions: could not parse dtcs.json");
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadMakes(Activity main) {
        String jsonStr = "";
        try {
            InputStream stream = main.getAssets().open("makes.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            jsonStr = new String(buffer, "UTF-8");
        }
        catch (Exception ex) {
            Log.e(TAG, "loadMakes: could not read makes.json");
            ex.printStackTrace();
            return false;
        }

        try {
            Globals.makes = new HashMap<String, String>();
            JSONObject json = new JSONObject(jsonStr);
            JSONObject wmis = json.getJSONObject("WMI");

            Iterator<?> wmiKeys = wmis.keys();

            while (wmiKeys.hasNext())
            {
                String key = (String) wmiKeys.next();
                String make = wmis.getString(key);
                Globals.makes.put(key, make);
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "loadMakes: could not parse makes.json");
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Stops the PID update worker safely and gracefully
     */
    public static void stopPidUpdateWorker() {
        if (Globals.updateWorker != null && Globals.updateWorker.isAlive()) {
            Globals.updateWorker.stop();
            Globals.updateWorker.join();
            Globals.updateWorker = null;
        }
    }

    /**
     * Starts the PID update worker if the cable allows for it
     */
    public static void startPidUpdateWorker() {
        if (Globals.cable != null && Globals.cable.IsOpen()) {
            Globals.updateWorker = new UpdatePidsWorker(Globals.cable, Globals.shownPids, null);
            Globals.updateWorker.start();
        }
    }

    /**
     * Stops and starts the PID update worker safely and gracefully, if possible
     */
    public static void restartPidUpdateWorker() {
        stopPidUpdateWorker();
        startPidUpdateWorker();
    }

    public static void connectSimulatedCable() {
        Globals.cable = new Elm327CableSimulator();
        if (Globals.cable.IsInitialized()) {
            Globals.updateWorker.SetCable(Globals.cable);
        }
    }

    public static void showToast(final String string, final int length)
    {
        Globals.mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(Globals.appContext, string, length).show();
            }
        });
    }
}

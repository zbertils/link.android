package beze.link;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import junit.runner.Version;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class AppState {

    private static final String TAG = Globals.TAG + "AppState";
    private static final String appStateFileName = "appstate.json";

    public String Version = "1.0";
    public String LastConnectedDeviceName;
    public List<Short> LastSelectedPids;

    private AppState() {
        LastConnectedDeviceName = "";
        LastSelectedPids = new ArrayList<>();
    }

    public static AppState getAppState(Activity main) {
        try {
            InputStream inputStream = main.getApplicationContext().openFileInput(appStateFileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                String fileContents = stringBuilder.toString();

                Gson gson = new Gson();
                return gson.fromJson(fileContents, AppState.class);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "AppState read failed: " + e.toString());
        }

        return new AppState();
    }

    public void SaveState(Activity main) {
        try {
            Gson gson = new Gson();
            String obj = gson.toJson(this);

            OutputStreamWriter osw = new OutputStreamWriter(main.getApplicationContext().openFileOutput(appStateFileName, Context.MODE_PRIVATE));
            osw.write(obj);
            osw.close();
        }
        catch (Exception e) {
            Log.e(TAG, "AppState write failed: " + e.toString());
        }
    }
}

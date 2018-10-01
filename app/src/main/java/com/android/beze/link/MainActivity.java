package com.android.beze.link;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.support.v4.app.Fragment;

import beze.link.fragments.AdvancedFragment;
import beze.link.fragments.ConnectFragment;
import beze.link.fragments.DataFragment;
import beze.link.fragments.HomeFragment;
import beze.link.fragments.PidsFragment;
import beze.link.fragments.TroubleCodesFragment;
import beze.link.obd2.ParameterIdentification;
import beze.link.Globals;
import beze.link.AppState;
import beze.link.SettingsActivity;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = Globals.TAG_BASE + "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Globals.appContext = getApplicationContext();
        Globals.mainActivity = this;

        // get the previous application state and set it up
        Globals.appState = AppState.getAppState(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.activity_settings, false);

        // load the allPids from the resource file
        Globals.loadPids(this, Globals.Units.SAE);
        Globals.loadMakes(this);
        Globals.loadDtcDescriptions(this);

        // get the activity_settings that apply at start up
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Globals.appContext);
        boolean simulateData = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_SIMULATE_DATA, false);
        boolean connectOnStart = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_RECONNECT_AT_START, false);
        if (connectOnStart) {
            if (simulateData) {
                Globals.connectSimulatedCable();
            }
            else {
                // TODO: get the last connected bluetooth device
            }
        }

        Globals.btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Globals.btAdapter == null) {
            Snackbar.make(navigationView, "This device does not support bluetooth!", 1500);
        }
        else {
            Log.i(TAG, "onCreateView: default bluetooth adapter obtained");
        }

        // determine which page to start from when the application starts
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment, new ConnectFragment())
                    .commit();
        }

        // the list needs to be cleared, otherwise going to another activity like Settings
        // will cause the list to get populated twice when the user returns to the main activity
        Globals.shownPids.clear();

        // setup the last selected pids
        for (short pidNumber : Globals.appState.LastSelectedPids) {
            for (ParameterIdentification pid : Globals.allPids) {
                if (pid.PID == pidNumber) {
                    Globals.shownPids.add(pid);
                    pid.LogThisPID = true;
                    break;
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Globals.appState != null) {
            Globals.appState.SaveState(this);
        }
        else {
            Log.e(TAG, "onDestroy: Globals.appState is null, state not saved!");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!Globals.btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.i(TAG, "onStart: bluetooth is already enabled");
        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // when the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "onActivityResult: bluetooth was successfully enabled");
                } else {
                    // user did not enable Bluetooth or an error occurred
                    Log.d(TAG, "onActivityResult: bluetooth not enabled");
                    this.finish();
                }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // the check for null and being alive is already done inside stopPidUpdateWorker but we need to know if it should be restarted
//        if (Globals.updateWorker != null && Globals.updateWorker.isAlive()) {
//            stopPidUpdateWorker();
//            Globals.restartWorker = true;
//        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        // reset so screen can fall asleep again if not the data fragment
        WindowManager windowManager = getWindowManager();
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        switch (id) {
            case R.id.nav_home:
                setTitle("Home");
                fragment = new HomeFragment();
                break;
            case R.id.nav_data:
                setTitle("Data");
                fragment = new DataFragment();
                ((DataFragment)fragment).setData(Globals.shownPids);

                // prevent the screen from falling asleep on the data fragment if the user specified to
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean keepScreenAwake = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_PREVENT_SCREEN_SLEEP, true);
                if (keepScreenAwake)
                {
                    this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }

                break;
            case R.id.nav_trouble_codes:
                setTitle("Trouble Codes");
                fragment = new TroubleCodesFragment();
                break;
            case R.id.nav_connect:
                setTitle("Connect");
                fragment = new ConnectFragment();
                break;
            case R.id.nav_advanced:
                setTitle("Advanced");
                fragment = new AdvancedFragment();
                break;
            case R.id.nav_pids :
                setTitle("Select PIDS");
                fragment = new PidsFragment();
                ((PidsFragment)fragment).setData(Globals.allPids);
                break;
            default:
                fragment = null;
                break;
        }

        // can only show a fragment if a valid menu item was selected
        if (fragment != null) {

            // stop the pids worker no matter what, if navigating back to the data fragment it will start again
            Globals.stopPidUpdateWorker();

            // if the user navigated to the data fragment then start a new thread to update data
            if (fragment instanceof DataFragment) {
                Globals.startPidUpdateWorker();
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

package beze.link.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import beze.link.Globals;
import com.android.beze.link.R;

import beze.link.interfaces.ICableStateChange;
import beze.link.obd2.Vehicle;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends CableInteractionFragment implements Runnable
{
    static final String TAG = Globals.TAG_BASE + "HomeFragment";

    public HomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCableStateChanged()
    {
        if (Globals.cable != null)
        {
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run()
    {
        if (Globals.cable != null && Globals.cable.IsInitialized())
        {
            try
            {
                String vin = Globals.cable.RequestVIN();
                final Vehicle vehicle = new Vehicle(vin, Globals.makes);

                Globals.mainActivity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        TextView textViewVIN = (TextView) getActivity().findViewById(R.id.textViewVIN);
                        TextView textViewYear = (TextView) getActivity().findViewById(R.id.textViewYear);
                        TextView textViewManufacturer = (TextView) getActivity().findViewById(R.id.textViewManufacturer);
                        TextView textViewModel = (TextView) getActivity().findViewById(R.id.textViewModel);

                        try
                        {
                            textViewVIN.setText(vehicle.VIN);
                            textViewYear.setText(Integer.toString(vehicle.Year));
                            textViewManufacturer.setText(vehicle.Manufacturer);
                            textViewModel.setText(vehicle.Model);
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(Globals.mainActivity, "Oops! Could not get vehicle info. Refresh this page from the side bar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            catch (Exception ex)
            {
                Log.e(TAG, "Could not get vehicle info", ex);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Thread thread = new Thread(this);
        thread.start();
    }

}

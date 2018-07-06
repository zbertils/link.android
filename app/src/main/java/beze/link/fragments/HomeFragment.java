package beze.link.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import beze.link.Globals;
import com.android.beze.link.R;
import beze.link.obd2.Vehicle;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements Runnable
{


    public HomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void run()
    {
        final ProgressBar progressBar = (ProgressBar) Globals.mainActivity.findViewById(R.id.homeProgressBar);
        Globals.mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        if (Globals.cable != null && Globals.cable.IsInitialized())
        {
            String vin = Globals.cable.RequestVIN();
            final Vehicle vehicle = new Vehicle(vin, Globals.makes);

            Globals.mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    progressBar.setVisibility(View.GONE);

                    TextView textViewVIN = (TextView) getActivity().findViewById(R.id.textViewVIN);
                    TextView textViewYear = (TextView) getActivity().findViewById(R.id.textViewYear);
                    TextView textViewManufacturer = (TextView) getActivity().findViewById(R.id.textViewManufacturer);
                    TextView textViewModel = (TextView) getActivity().findViewById(R.id.textViewModel);

                    textViewVIN.setText(vehicle.VIN);
                    textViewYear.setText(Integer.toString(vehicle.Year));
                    textViewManufacturer.setText(vehicle.Manufacturer);
                    textViewModel.setText(vehicle.Model);
                }
            });
        }

        Globals.mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setVisibility(View.GONE);
            }
        });
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

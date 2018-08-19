package beze.link.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.beze.link.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import beze.link.Globals;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.ui.DtcStatusRecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class TroubleCodesStatusFragment extends Fragment implements Runnable{

    private static final String TAG = Globals.TAG_BASE + "TroubleCodesFragment";

    private RecyclerView mStatusDtcRecyclerView;
    private RecyclerView.Adapter mCurrentDtcAdapter;
    private RecyclerView.LayoutManager mCurrentDtcLayoutManager;
    private List<Map.Entry<DiagnosticTroubleCode, String>> currentCodes = new ArrayList<>(); // default to an empty list in case the cable is not open



    public TroubleCodesStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void run()
    {
        if (Globals.cable != null && Globals.cable.IsInitialized())
        {
            final ProgressBar progressBar = (ProgressBar) Globals.mainActivity.findViewById(R.id.dtcProgressBar);
            Globals.mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            currentCodes.addAll(Globals.cable.RequestAllDtcStatuses());

            Globals.mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (currentCodes.size() == 0)
                    {
                        Toast.makeText(Globals.appContext, "No trouble codes present!", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Globals.mainActivity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mCurrentDtcAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trouble_codes_status, container, false);

        // setup everything needed to display current trouble codes
        mStatusDtcRecyclerView = view.findViewById(R.id.currentDtcRecyclerView);
        mStatusDtcRecyclerView.setHasFixedSize(true);

        // create the recycler view managers
        mCurrentDtcLayoutManager = new LinearLayoutManager(getContext());
        mCurrentDtcAdapter = new DtcStatusRecyclerViewAdapter(currentCodes);

        // set managers for recycler view
        mStatusDtcRecyclerView.setLayoutManager(mCurrentDtcLayoutManager);
        mStatusDtcRecyclerView.setAdapter(mCurrentDtcAdapter);

        Thread thread = new Thread(this);
        thread.start();

        return view;
    }

}

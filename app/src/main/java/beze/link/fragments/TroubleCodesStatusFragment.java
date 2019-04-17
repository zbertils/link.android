package beze.link.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.beze.link.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import beze.link.Globals;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.Protocols;
import beze.link.ui.DtcStatusRecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class TroubleCodesStatusFragment extends CableInteractionFragment implements Runnable{

    private static final String TAG = Globals.TAG_BASE + "CodesStatusFragment";

    private RecyclerView mStatusDtcRecyclerView;
    private RecyclerView.Adapter mCurrentDtcAdapter;
    private RecyclerView.LayoutManager mStatusDtcLayoutManager;
    private List<DiagnosticTroubleCode> currentCodes = new ArrayList<>(); // default to an empty list in case the cable is not open

    public TroubleCodesStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCableStateChanged()
    {
        // do nothing for now
    }

    @Override
    public void run()
    {
        final TextView noStatusesTextView = (TextView) Globals.mainActivity.findViewById(R.id.noStatusesTextView);

        if (Globals.cable != null &&
            Globals.cable.Protocol == Protocols.Protocol.J1850)
        {
            // actually get the trouble code statuses
            currentCodes.addAll(Globals.cable.RequestAllDtcStatuses().values());

            Globals.mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (currentCodes.size() == 0)
                    {
                        Toast.makeText(Globals.appContext, "No code statuses present!", Toast.LENGTH_LONG).show();
                        if (noStatusesTextView != null)
                        {
                            noStatusesTextView.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        mCurrentDtcAdapter.notifyDataSetChanged();
                        if (noStatusesTextView != null)
                        {
                            noStatusesTextView.setVisibility(View.GONE);
                        }
                    }
                }
            });

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trouble_codes_status, container, false);

        // setup everything needed to display current trouble codes
        mStatusDtcRecyclerView = view.findViewById(R.id.statusDtcRecyclerView);
        mStatusDtcRecyclerView.setHasFixedSize(true);

        // create the recycler view managers
        mStatusDtcLayoutManager = new LinearLayoutManager(getContext());
        mCurrentDtcAdapter = new DtcStatusRecyclerViewAdapter(currentCodes);

        // set managers for recycler view
        mStatusDtcRecyclerView.setLayoutManager(mStatusDtcLayoutManager);
        mStatusDtcRecyclerView.setAdapter(mCurrentDtcAdapter);

        Thread thread = new Thread(this);
        thread.start();

        return view;
    }

}

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

import beze.link.Globals;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.ui.DtcRecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class TroubleCodesCurrentFragment extends Fragment implements View.OnClickListener, Runnable{

    private static final String TAG = Globals.TAG_BASE + "TroubleCodesFragment";

    private RecyclerView mCurrentDtcRecyclerView;
    private RecyclerView.Adapter mCurrentDtcAdapter;
    private RecyclerView.LayoutManager mCurrentDtcLayoutManager;
    private List<DiagnosticTroubleCode> currentCodes = new ArrayList<>(); // default to an empty list in case the cable is not open

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    if (Globals.cable != null && Globals.cable.IsInitialized()) {
                        Globals.cable.ClearTroubleCodes();
                        Toast.makeText(Globals.appContext, "Trouble codes cleared!", Toast.LENGTH_LONG).show();

                        // update the view to reflect no more trouble codes
                        currentCodes.clear();
                        mCurrentDtcAdapter.notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(Globals.appContext, "Cable is not initialized!", Toast.LENGTH_LONG).show();
                    }

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // do nothing, use declined
                    break;
            }
        }
    };

    public TroubleCodesCurrentFragment() {
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

            currentCodes.addAll(Globals.cable.RequestTroubleCodes());

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
        View view = inflater.inflate(R.layout.fragment_trouble_codes_current, container, false);

        // setup everything needed to display current trouble codes
        mCurrentDtcRecyclerView = view.findViewById(R.id.currentDtcRecyclerView);
        mCurrentDtcRecyclerView.setHasFixedSize(true);

        // create the recycler view managers
        mCurrentDtcLayoutManager = new LinearLayoutManager(getContext());
        mCurrentDtcAdapter = new DtcRecyclerViewAdapter(currentCodes);

        // set managers for recycler view
        mCurrentDtcRecyclerView.setLayoutManager(mCurrentDtcLayoutManager);
        mCurrentDtcRecyclerView.setAdapter(mCurrentDtcAdapter);

        Thread thread = new Thread(this);
        thread.start();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FloatingActionButton fab = getActivity().findViewById(R.id.fabClearDtc);
        fab.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fabClearDtc :
                AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
                builder.setMessage("Clearing trouble codes will reset the ECU, and should be done with the engine OFF and the key in the ON position\r\n\r\nContinue?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
                break;
            default:
                Log.w(TAG, String.format("onClick: received a click for an unknown view, id: %d", view.getId()));
                break;
        }
    }

}

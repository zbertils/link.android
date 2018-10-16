package beze.link.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import beze.link.Globals;
import com.android.beze.link.R;
import beze.link.obd2.ParameterIdentification;
import beze.link.ui.DataRecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class DataFragment extends Fragment {

    private static final String TAG = Globals.TAG_BASE + "DataFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    protected List<ParameterIdentification> data; // this needs to be set after being constructed

    public DataFragment() {
        // Required empty public constructor
    }

    public void setData(List<ParameterIdentification> pids)
    {
        data = pids;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_data, container, false); // Inflate the layout for this fragment
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        boolean showPidStreamValue = sharedPref.getBoolean(Globals.Preferences.KEY_PREF_SHOW_PID_STREAM_VALUES, true);

        mRecyclerView = view.findViewById(R.id.recyclerViewData);
        mRecyclerView.setHasFixedSize(true);

        // create the recycler view managers
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new DataRecyclerViewAdapter(Globals.shownPids, showPidStreamValue);

        // set managers for recycler view
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        Globals.dataFragmentAdapter = mAdapter;

        return view;
    }
}

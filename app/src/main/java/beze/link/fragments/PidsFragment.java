package beze.link.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import beze.link.R;
import beze.link.obd2.ParameterIdentification;
import beze.link.ui.PidsRecyclerViewAdapter;


public class PidsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    protected List<ParameterIdentification> data; // this needs to be set after being constructed

    public PidsFragment() {
        // Required empty public constructor
    }

    public void setData(List<ParameterIdentification> pids)
    {
        data = pids;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pids, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerViewPids);
        mRecyclerView.setHasFixedSize(true);

        // create the recycler view managers
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new PidsRecyclerViewAdapter(data);

        // set managers for recycler view
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

}

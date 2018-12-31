package beze.link.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.android.beze.link.R;

import beze.link.Globals;
import beze.link.obd2.ParameterIdentification;
import beze.link.ui.PidsRecyclerViewAdapter;


public class PidsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public PidsFragment() {
        // Required empty public constructor
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
        mAdapter = new PidsRecyclerViewAdapter(Globals.allPids);

        // set managers for recycler view
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        Globals.pidsFragmentAdapter = mAdapter;

        return view;
    }

}

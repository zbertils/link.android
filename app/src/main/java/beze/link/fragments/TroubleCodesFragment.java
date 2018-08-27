package beze.link.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import beze.link.Globals;
import com.android.beze.link.R;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.Protocols;
import beze.link.ui.TroubleCodePagerAdapter;
import beze.link.ui.DtcRecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class TroubleCodesFragment extends Fragment implements TabLayout.OnTabSelectedListener
{

    private static final String TAG = Globals.TAG_BASE + "TroubleCodesFragment";
    private ViewPager viewPager = null;

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (viewPager != null)
        {
            viewPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {
        // do nothing for now
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {
        // do nothing for now
    }

    public TroubleCodesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trouble_codes, container, false);

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.troubleCodeTabLayout);
        viewPager = (ViewPager) view.findViewById(R.id.troubleCodeViewPager);
        final TroubleCodePagerAdapter pagerAdapter = new TroubleCodePagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(this);

        return view;
    }

}

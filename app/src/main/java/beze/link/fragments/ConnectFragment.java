package beze.link.fragments;


import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.support.v7.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import beze.link.Globals;
import com.android.beze.link.R;
import beze.link.obd2.Protocols;
import beze.link.obd2.cables.Elm327Cable;
import beze.link.obd2.cables.IConnectionCallback;
import beze.link.ui.ConnectPagerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends Fragment implements TabLayout.OnTabSelectedListener
{

    private static final String TAG = Globals.TAG_BASE + "ConnectFragment";
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

    public ConnectFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.connectTabLayout);
        viewPager = (ViewPager) view.findViewById(R.id.connectViewPager);
        final ConnectPagerAdapter pagerAdapter = new ConnectPagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(this);

        return view;
    }
}

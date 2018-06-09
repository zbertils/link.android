package beze.link.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectWifiFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConnectWifiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectWifiFragment extends Fragment
{

    public ConnectWifiFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(com.android.beze.link.R.layout.fragment_connect_wifi, container, false);
    }
}

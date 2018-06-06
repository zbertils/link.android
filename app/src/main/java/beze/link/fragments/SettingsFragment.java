package beze.link.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.android.beze.link.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey)
    {
        setPreferencesFromResource(R.xml.activity_settings, rootKey);
    }

}

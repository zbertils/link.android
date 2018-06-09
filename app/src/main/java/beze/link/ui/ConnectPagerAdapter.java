package beze.link.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import beze.link.fragments.ConnectBluetoothFragment;
import beze.link.fragments.ConnectUSBFragment;
import beze.link.fragments.ConnectWifiFragment;

public class ConnectPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public ConnectPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ConnectBluetoothFragment tab1 = new ConnectBluetoothFragment();
                return tab1;
            case 1:
                ConnectWifiFragment tab2 = new ConnectWifiFragment();
                return tab2;
            case 2:
                ConnectUSBFragment tab3 = new ConnectUSBFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

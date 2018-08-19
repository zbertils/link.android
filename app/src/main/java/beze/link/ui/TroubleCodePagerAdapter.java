package beze.link.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import beze.link.fragments.TroubleCodesCurrentFragment;
import beze.link.fragments.TroubleCodesStatusFragment;

public class TroubleCodePagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public TroubleCodePagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                TroubleCodesCurrentFragment tab1 = new TroubleCodesCurrentFragment();
                return tab1;
            case 1:
                TroubleCodesStatusFragment tab2 = new TroubleCodesStatusFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

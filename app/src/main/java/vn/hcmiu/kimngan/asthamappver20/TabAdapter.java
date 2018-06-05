package vn.hcmiu.kimngan.asthamappver20;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by giasutinhoc.vn on 28/10/2017.
 */

public class TabAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> alFragments = new ArrayList<>();
    ArrayList<String> alTitles = new ArrayList<>();

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        alFragments.add(fragment);
        alTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return alFragments.get(position);
    }

    @Override
    public int getCount() {
        return alFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return alTitles.get(position);
    }
}

package vn.hcmiu.kimngan.asthamappver20;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by giasutinhoc.vn on 28/10/2017.
 */

public class TabAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> alFragments = new ArrayList<>();
    ArrayList<String> alTitles = new ArrayList<>();
    WeakReference<Activity> reference ;//= new WeakReference<Activity>(this);

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title,WeakReference<Activity> activity) {
        alFragments.add(fragment);
        alTitles.add(title);
        reference = activity;
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

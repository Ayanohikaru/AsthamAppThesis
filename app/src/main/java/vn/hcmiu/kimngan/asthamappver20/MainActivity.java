package vn.hcmiu.kimngan.asthamappver20;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        // Creating tabs
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConnectionFragment(), "Setup", reference);
        adapter.addFragment(new SensorFragment(), "Sensor", reference);
        adapter.addFragment(new AsthmaFragment(), "Asthma", reference);

        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
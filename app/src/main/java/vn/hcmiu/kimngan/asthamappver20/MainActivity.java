package vn.hcmiu.kimngan.asthamappver20;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Creating tabs
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConnectionFragment(), "Setup");
        adapter.addFragment(new SensorFragment(), "Sensor");
        adapter.addFragment(new AsthmaFragment(), "Asthma");

        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
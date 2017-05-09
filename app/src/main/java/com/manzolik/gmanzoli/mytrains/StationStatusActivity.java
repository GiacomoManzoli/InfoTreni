package com.manzolik.gmanzoli.mytrains;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.manzolik.gmanzoli.mytrains.fragments.StationStatusInfosFragment;
import com.manzolik.gmanzoli.mytrains.fragments.StationDescriptionFragment;
import com.manzolik.gmanzoli.mytrains.data.Station;

public class StationStatusActivity extends AppCompatActivity {
    private static final String TAG = StationStatusActivity.class.getSimpleName();

    public static final String INTENT_STATION = "intent_station";

    private Station mStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_tab_layout);

        mStation = (Station) getIntent().getSerializableExtra(INTENT_STATION);

        // Aggiorna il titolo dell'ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mStation.toString());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new StationStatusPageFragmentAdapter(getSupportFragmentManager(), mStation));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    /* Gestione dei pulsanti nella toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == android.R.id.home) {
            // Pulsante indietro
            onBackPressed();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
    }


    private class StationStatusPageFragmentAdapter extends FragmentPagerAdapter {
        private final String TAG = StationStatusPageFragmentAdapter.class.getSimpleName();
        private final int PAGE_COUNT = 3;

        private String mTitles[] = new String[] { "Descrizione", "Arrivi", "Partenze" };
        private Station mStation;

        StationStatusPageFragmentAdapter(FragmentManager fm, Station station) {
            super(fm);
            mStation = station;
        }

        @Override
        public Fragment getItem(int position) {
            if (BuildConfig.DEBUG) Log.d(this.TAG, "getItem");
            switch (position) {
                case 0:
                    return StationDescriptionFragment.newInstance(mStation);
                case 1:
                    return StationStatusInfosFragment.newInstanceArrivals(mStation);
                case 2:
                    return StationStatusInfosFragment.newInstanceDepartures(mStation);
                default:
                    return StationDescriptionFragment.newInstance(mStation);
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return mTitles[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}

package com.manzolik.gmanzoli.mytrains;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.components.TrainStatusFragment;
import com.manzolik.gmanzoli.mytrains.components.TrainStatusMapFragment;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainDepartureStationService;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusService;

public class TrainStatusActivity extends AppCompatActivity implements TrainStatusService.TrainStatusServiceListener {

    private static final String TAG = TrainStatusActivity.class.getSimpleName();

    public static final String INTENT_TRAIN = "train";

    private Train mTrain;
    private TrainStatus mTrainStatus;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout);


        Train train = (Train) getIntent().getSerializableExtra(INTENT_TRAIN);
        mTrain = train;

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Codice treno: " + train.getCode());
            Log.d(TAG, "Codice partenza: " + train.getDepartureStation().getCode());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTrain.toString());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTrainStatus = null;

        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(new TrainStatusPageFragmentAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mPager);


        TrainStatusService trainStatusService = new TrainStatusService();
        trainStatusService.getStatusForTrain(mTrain, this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onTrainStatusSuccess(TrainStatus ts) {
        TrainStatusPageFragmentAdapter adapter = (TrainStatusPageFragmentAdapter) mPager.getAdapter();
        adapter.updateFragmentsContent(ts);
    }

    @Override
    public void onTrainStatusFailure(Exception e) {

    }

    private class TrainStatusPageFragmentAdapter extends FragmentPagerAdapter {
        private final String TAG = TrainStatusPageFragmentAdapter.class.getSimpleName();
        private final int PAGE_COUNT = 2;

        private String mTitles[] = new String[] { "Stato", "Mappa" };

        private TrainStatusFragment mTrainStatusFragment;
        private TrainStatusMapFragment mTrainStatusMapFragment;

        private TrainStatus mStatus;

        TrainStatusPageFragmentAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            if (BuildConfig.DEBUG) Log.d(this.TAG, "getItem");
            switch (position) {
                case 0:
                    if (mStatus != null) {
                        mTrainStatusFragment = TrainStatusFragment.newInstance(mStatus);
                    } else {
                        mTrainStatusFragment = TrainStatusFragment.newInstance();
                    }
                    return mTrainStatusFragment;
                case 1:
                    mTrainStatusMapFragment = TrainStatusMapFragment.newInstance();
                    return mTrainStatusMapFragment;
            }
            return null;
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

        void updateFragmentsContent(TrainStatus status) {
            this.mStatus = status;
            if (mTrainStatusFragment != null) mTrainStatusFragment.updateStatus(status);
            if (mTrainStatusFragment != null) mTrainStatusMapFragment.updateStatus(status);

        }
    }
}

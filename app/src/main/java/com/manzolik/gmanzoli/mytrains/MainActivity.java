package com.manzolik.gmanzoli.mytrains;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.manzolik.gmanzoli.mytrains.drawer.CustomDrawerAdapter;
import com.manzolik.gmanzoli.mytrains.drawer.CustomDrawerItem;
import com.manzolik.gmanzoli.mytrains.notifications.SchedulingAlarmReceiver;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Variabili per la gestione del drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    // AlarmReceiver per disattivare l'allarme periodico mentre l'activity è in primo piano
    private SchedulingAlarmReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReceiver = new SchedulingAlarmReceiver();

        // Configurazione del drawer
        ArrayList<CustomDrawerItem> dataList = new ArrayList<>();
        dataList.add(new CustomDrawerItem(getString(R.string.ft_monitor), R.mipmap.ic_train_grey_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_quick_search), R.mipmap.ic_search_grey_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_manage), R.mipmap.ic_notification_grey_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_settings), R.mipmap.ic_settings_grey_24dp));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.main_left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new CustomDrawerAdapter(
                this,
                R.layout.custom_drawer_item,
                dataList));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (BuildConfig.DEBUG) Log.d(TAG, "drawerList - Item clicked");
                updateFragment(position);
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        // Configurazione del pulsante
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            /* Called when drawer is closed */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                invalidateOptionsMenu();
                syncState();
            }

            /* Called when a drawer is opened */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Impostazione della toolbar
        if (toolbar != null){
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.syncState();
        updateFragment(0);
        drawerList.setItemChecked(0,true);
        drawerList.setSelection(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Disattiva l'allarme finché il l'activity esiste
        if (BuildConfig.DEBUG) Log.d(TAG, "Allarme periodico disabilitato");
        mReceiver.stopRepeatingAlarm(this);
        // Cancella le eventuali notifiche presenti
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Se necessario riattiva l'allarme periodico
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean alarmEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_ENABLED, false);
        if (BuildConfig.DEBUG) Log.d(TAG, String.format("Notifiche abilitate: %b",alarmEnabled));
        if (alarmEnabled) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Allarme periodico riabilitato");
            mReceiver.startRepeatingAlarm(this);
        }
    }

    // Gestione del tap del burger
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public void updateFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment;
        String fragmentTitle;
        if (position == 0) {
            fragment = TrainRemindersStatusFragment.newInstance();
            fragmentTitle = getString(R.string.ft_monitor);
        }else if (position == 1) {
            fragment = QuickSearchFragment.newInstance();
            fragmentTitle = getString(R.string.ft_quick_search);
        } else if (position == 2) {
            fragment = ManageReminderFragment.newInstance();
            fragmentTitle = getString(R.string.ft_manage);
        } else if (position == 3) {
            fragment = new SettingsFragment();
            fragmentTitle = getString(R.string.ft_settings);
        } else {
            fragment = TrainRemindersStatusFragment.newInstance();
            fragmentTitle = "";
        }

        // Aggiorna il titolo
        getSupportActionBar().setTitle(fragmentTitle);
        //Replace fragment
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.main_content_frame, fragment);
        ft.commit();
    }

}

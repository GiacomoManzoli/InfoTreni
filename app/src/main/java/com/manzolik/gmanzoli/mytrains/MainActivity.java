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
    private static final String STATE_SELECTED_FRAGMENT = "sel_fragment";

    // Variabili per la gestione del drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    // AlarmReceiver per disattivare l'allarme periodico mentre l'activity è in primo piano
    private SchedulingAlarmReceiver mReceiver;

    private int mSelectedFragment = 0;

    /*
    * INZIO - GESTIONE LIFECYCLE
    * */

    /*
    * onCreate: Configura il drawer menù e l'action bar
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recupero lo stato relativo al Fragment precedentemente selezionato
        if (savedInstanceState != null) {
            mSelectedFragment = savedInstanceState.getInt(STATE_SELECTED_FRAGMENT, 0);
        }

        mReceiver = new SchedulingAlarmReceiver();

        // Configurazione del drawer
        ArrayList<CustomDrawerItem> dataList = new ArrayList<>();
        dataList.add(new CustomDrawerItem(getString(R.string.ft_monitor), R.mipmap.ic_train_grey_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_quick_search), R.mipmap.ic_search_grey_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_manage), R.mipmap.ic_notification_grey_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_settings), R.mipmap.ic_settings_grey_24dp));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.main_left_drawer);

        // Adapter per la lista del drawer
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

        // Configurazione del pulsante "burger"
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

        updateFragment(mSelectedFragment); // Visualizza il fragment selezionato (di default 0)

        drawerList.setItemChecked(0,true);
        drawerList.setSelection(0);
    }

    /* onPostCreate: Sincronizza lo stato del drawer */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /*
    * onStart: Disabilita l'allarme periodico per le notifiche
    * */
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

    /*
    * onStop: Se le notifiche sono attive, abilita l'allarme periodico.
    * Questo perché l'allarme viene disattivato finché l'activity è visibile.
    * */
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

    /*
    * onSaveInstance: Salva l'infomrazione relativa al Fragment selezionato.
    * Così facendo quando viene ruotato il dispositivo, viene visualizzato il Fragment
    * precedentemente selezionato
    * */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_FRAGMENT, mSelectedFragment);
    }

     /*
    * FINE - GESTIONE LIFECYCLE
    * */

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


    /*
    * updateFragment: Aggiorna il Fragment visualizzato in base a cosa è stato selezionato
    * dall'utente nel drawer menu.
    * */
    private void updateFragment(int position) {
        mSelectedFragment = position;
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment;
        String fragmentTitle;
        if (position == 0) {
            fragment = TrainRemindersStatusFragment.newInstance();
            fragmentTitle = getString(R.string.ft_monitor);
        } else if (position == 1) {
            fragment = QuickSearchFragment.newInstance();
            fragmentTitle = getString(R.string.ft_quick_search);
        } else if (position == 2) {
            fragment = ManageReminderFragment.newInstance();
            fragmentTitle = getString(R.string.ft_manage);
        } else if (position == 3) {
            fragment = new SettingsFragment();
            fragmentTitle = getString(R.string.ft_settings);
        } else {
            // Caso di default:
            fragment = TrainRemindersStatusFragment.newInstance();
            fragmentTitle = "";
            mSelectedFragment = 0;
        }

        // Aggiorna il titolo
        getSupportActionBar().setTitle(fragmentTitle);
        //Replace fragment
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.main_content_frame, fragment);
        ft.commit();
    }

}

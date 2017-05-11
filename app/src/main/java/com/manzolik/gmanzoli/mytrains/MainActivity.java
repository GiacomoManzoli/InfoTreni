package com.manzolik.gmanzoli.mytrains;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.maps.MapView;
import com.manzolik.gmanzoli.mytrains.adapters.CustomDrawerAdapter;
import com.manzolik.gmanzoli.mytrains.adapters.CustomDrawerItem;
import com.manzolik.gmanzoli.mytrains.fragments.ManageReminderFragment;
import com.manzolik.gmanzoli.mytrains.fragments.NewsFragment;
import com.manzolik.gmanzoli.mytrains.fragments.QuickSearchFragment;
import com.manzolik.gmanzoli.mytrains.fragments.QuickSearchStationFragment;
import com.manzolik.gmanzoli.mytrains.fragments.SettingsFragment;
import com.manzolik.gmanzoli.mytrains.fragments.TrainRemindersStatusFragment;
import com.manzolik.gmanzoli.mytrains.receivers.SchedulingAlarmReceiver;
import com.manzolik.gmanzoli.mytrains.utils.MaintenanceUtils;
import com.manzolik.gmanzoli.mytrains.utils.NetworkUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String STATE_SELECTED_FRAGMENT = "sel_fragment";

    // Variabili per la gestione del drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

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
        // Aggiuta i dati delle stazioni
        MaintenanceUtils.startMaintenance(this);

        setContentView(R.layout.activity_main);

        // Recupero lo stato relativo al Fragment precedentemente selezionato
        if (savedInstanceState != null) {
            mSelectedFragment = savedInstanceState.getInt(STATE_SELECTED_FRAGMENT, 0);
        }


        // Configurazione del drawer
        ArrayList<CustomDrawerItem> dataList = new ArrayList<>();
        dataList.add(new CustomDrawerItem(getString(R.string.ft_monitor), R.mipmap.ic_train_black_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_quick_search), R.mipmap.ic_search_black_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_quick_station_search), R.mipmap.ic_station_black_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_manage), R.mipmap.ic_notification_black_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_news), R.mipmap.ic_news_black_24dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_settings), R.mipmap.ic_settings_black_24dp));


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
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (NetworkUtils.isNetworkConnected(this)) {
            if (savedInstanceState == null) {
                // Devo creare il fragment nuovo solo se non sto ripristinando uno stato
                updateFragment(mSelectedFragment); // Visualizza il fragment selezionato (di default 0)
            } else {
                String fragmentTitle;
                switch (mSelectedFragment) {
                    case 0:
                        fragmentTitle = getString(R.string.ft_monitor);
                        break;
                    case 1:
                        fragmentTitle = getString(R.string.ft_quick_search);
                        break;
                    case 2:
                        fragmentTitle = getString(R.string.ft_quick_station_search);
                        break;
                    case 3:
                        fragmentTitle = getString(R.string.ft_manage);
                        break;
                    case 4:
                        fragmentTitle = getString(R.string.ft_news);
                        break;
                    case 5:
                        fragmentTitle = getString(R.string.ft_settings);
                        break;
                    default:
                        fragmentTitle = "";
                        mSelectedFragment = 0;
                        break;
                }
                // Aggiorna il titolo
                getSupportActionBar().setTitle(fragmentTitle);
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Nessuna connessione ad internet")
                    .setMessage("L'applicazione potrebbe non funzionare in modo corretto.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this, NoConnectivityActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
        }

        drawerList.setItemChecked(0, true);
        drawerList.setSelection(0);
    }


    /* onPostCreate: Sincronizza lo stato del drawer */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle("Geolocalizzazione non permessa")
                    .setMessage("Alcune funzionalità dell'applicazione richiedono la geolocalizzazione " +
                            "che al momento è disabilitata.\n" +
                            "Utilizza la prossima finestra di dialogo per abilitarla. \n" +
                            "Non sei obbligato, l'applicazione è in grado di funzionare lo stesso.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        }
                    }).setIcon(android.R.drawable.ic_dialog_info).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permesso concesso
                    Toast.makeText(this, "Le modifiche saranno attive al prossimo riavvio dell'applicazione", Toast.LENGTH_LONG)
                            .show();
                    SharedPreferences preferences = PreferenceManager
                            .getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(SettingsFragment.NOTIFICATION_LOCATION_FILTERING, true);
                    editor.putBoolean(SettingsFragment.REMINDER_SORTING, true);
                    editor.apply();
                } else {
                    // Permesso non concesso
                    SharedPreferences preferences = PreferenceManager
                            .getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(SettingsFragment.NOTIFICATION_LOCATION_FILTERING, false);
                    editor.putBoolean(SettingsFragment.REMINDER_SORTING, false);
                    editor.apply();
                }
            }
        }
    }

    /*
    * onStart: Disabilita l'allarme periodico per le notifiche
    * */
    @Override
    protected void onStart() {
        super.onStart();
        // Disattiva l'allarme finché il l'activity esiste
        if (BuildConfig.DEBUG) Log.d(TAG, "Allarme periodico disabilitato");
        SchedulingAlarmReceiver.stopRepeatingAlarm(this);
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

        // Controllo anche se c'è la connessione attiva prima di attivare l'allarme
        if (alarmEnabled) {
            if (NetworkUtils.isNetworkConnected(this)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Allarme periodico riabilitato");
                SchedulingAlarmReceiver.startRepeatingAlarm(this);
            } else {
                if (BuildConfig.DEBUG) Log.e(TAG, "Allarme non riabilitato per mancanza di connessione");
            }
        }
    }

    /*
    * onSaveInstance: Salva l'infomrazione relativa al Fragment selezionato.
    * Così facendo quando viene ruotato il dispositivo, viene visualizzato il Fragment
    * precedentemente selezionato
    * */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        switch (position) {
            case 0:
                fragment = TrainRemindersStatusFragment.newInstance();
                fragmentTitle = getString(R.string.ft_monitor);
                break;
            case 1:
                fragment = QuickSearchFragment.newInstance();
                fragmentTitle = getString(R.string.ft_quick_search);
                break;
            case 2:
                fragment = QuickSearchStationFragment.newInstance();
                fragmentTitle = getString(R.string.ft_quick_station_search);
                break;
            case 3:
                fragment = ManageReminderFragment.newInstance();
                fragmentTitle = getString(R.string.ft_manage);
                break;
            case 4:
                fragment = NewsFragment.newInstance();
                fragmentTitle = getString(R.string.ft_news);
                break;
            case 5:
                fragment = new SettingsFragment();
                fragmentTitle = getString(R.string.ft_settings);
                break;
            default:
                // Caso di default:
                fragment = TrainRemindersStatusFragment.newInstance();
                fragmentTitle = "";
                mSelectedFragment = 0;
                break;
        }

        // Aggiorna il titolo
        getSupportActionBar().setTitle(fragmentTitle);
        //Replace fragment
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.main_content_frame, fragment);
        ft.commit();
    }
}

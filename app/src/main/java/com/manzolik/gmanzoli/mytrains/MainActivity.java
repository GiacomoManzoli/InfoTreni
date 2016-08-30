package com.manzolik.gmanzoli.mytrains;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Variabili per la gestione del drawer
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Configurazione del drawer

        ArrayList<CustomDrawerItem> dataList = new ArrayList<>();
        dataList.add(new CustomDrawerItem(getString(R.string.ft_monitor), R.mipmap.ic_train_black_48dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_manage), R.mipmap.ic_notifications_black_48dp));
        dataList.add(new CustomDrawerItem(getString(R.string.ft_settings), R.mipmap.ic_settings_black_48dp));

        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawerList = (ListView) findViewById(R.id.main_left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new CustomDrawerAdapter(
                this,
                R.layout.custom_drawer_item,
                dataList));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Item clicked");
                updateFragment(position);
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        // Configurazione del pulsante
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

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
        drawerLayout.addDrawerListener(drawerToggle);

        // Impostazione della toolbar
        if (toolbar != null){
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle.syncState();
        updateFragment(0);
        drawerList.setItemChecked(0,true);
        drawerList.setSelection(0);

    }

    // Gestione del tap del burger
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    public void updateFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment;
        String fragmentTitle;
        if (position == 0) {
            fragment = TrainStatusFragment.newInstance();
            fragmentTitle = getString(R.string.ft_monitor);
        } else if (position == 1) {
            fragment = ManageReminderFragment.newInstance();
            fragmentTitle = getString(R.string.ft_manage);
        } else if (position == 2) {
            fragment = new SettingsFragment();
            fragmentTitle = getString(R.string.ft_settings);
        } else {
            fragment = TrainStatusFragment.newInstance();
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

package com.manzolik.gmanzoli.mytrains;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.manzolik.gmanzoli.mytrains.data.Station;

public class AddReminderActivity extends AppCompatActivity
    implements SelectTrainFragment.OnTrainSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment selectTrainFragment = SelectTrainFragment.newInstance();
        String fragmentTitle = "Crea nuovo avviso";

        // Aggiorna il titolo
        getSupportActionBar().setTitle(fragmentTitle);
        //Replace fragment
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.add_activity_content_frame, selectTrainFragment);
        ft.commit();
    }

    @Override
    public void onTrainSelected(int trainCode, Station departureStation) {
        // Metodo invocato quando l'utente ha scelto correttamente il treno
        // mostra il frammento successivo
        Fragment fragment = ConfigReminderFragment.newInstance(trainCode, departureStation);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.add_activity_content_frame, fragment)
                // Add this transaction to the back stack
                .addToBackStack("detail")
                .commit();

        /*FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.add_activity_content_frame, fragment);
        ft.commit();*/
    }


}

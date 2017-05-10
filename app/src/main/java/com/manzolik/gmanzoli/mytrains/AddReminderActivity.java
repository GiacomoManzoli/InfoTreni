package com.manzolik.gmanzoli.mytrains;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.fragments.ConfigReminderFragment;
import com.manzolik.gmanzoli.mytrains.fragments.FindTrainFragment;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;

public class AddReminderActivity extends AppCompatActivity
    implements FindTrainFragment.OnTrainFoundListener,
        ConfigReminderFragment.ConfigReminderListener{

    private static final String TAG = AddReminderActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_fragment);

        String fragmentTitle = getString(R.string.at_new_reminder);

        // Aggiorna il titolo dell'ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(fragmentTitle);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Creo il fragment solo se non c'è una savedInstance, ovvero solo al primo onCreate
        // Così facendo al cambio di layout, rimane visualizzato il fragment che precedentemente
        // era visualizzato
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FindTrainFragment findTrainFragment = FindTrainFragment.newInstance();

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, findTrainFragment);
            ft.commit();
        }
    }


    /* Gestione dei pulsanti nella toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == android.R.id.home) {
            // Pulsante indietro
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                if (BuildConfig.DEBUG) Log.i(TAG, "C'è un Fragment, pop del backstack");
                fm.popBackStack();

            } else {
                if (BuildConfig.DEBUG) Log.i(TAG, "Ultimo fragment, comportamento di default");
                return super.onOptionsItemSelected(item);
            }
        }

        /* con return false la propagazione dell'evento continua e viene invocato onOptions...
        * del fragment contenuto*/
        return false;
    }

    /*
    * FindTrainFragment.OnTrainFoundListener:
    * callback che viene invocata quando l'utente ha correttamente selezionato
    * un treno.
    * Viene sostituito il fragment con quello per la configurazione di un reminder
    * */
    @Override
    public void onTrainFound(Train train) {
        ConfigReminderFragment fragment = ConfigReminderFragment.newInstance(train);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                // Add this transaction to the back stack
                .addToBackStack("detail")
                .commit();
    }

    /* ConfigTrainReminder.ConfigReminderListener */
    @Override
    public void onConfirmReminder(TrainReminder trainReminder) {
        // callback che viene invocata quando l'utente conferma l'aggiunta del reminder
        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this);
        if (trainReminderDAO.insertReminder(trainReminder)) {
            if (BuildConfig.DEBUG) Log.v(TAG, "Reminder aggiunto");
            Toast.makeText(this, "Reminder aggiunto", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (BuildConfig.DEBUG) Log.e(TAG, "Non è stato aggiunto il reminder");
        }
    }

    @Override
    public void onAbortReminder() {
        // callback che viene invocata quando l'utente annulla la creazione del reminder
        if (BuildConfig.DEBUG) Log.v(TAG, "Inserimento del reminder annullato");
    }

}

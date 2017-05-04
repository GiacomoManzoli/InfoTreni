package com.manzolik.gmanzoli.mytrains;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.components.ConfigReminderFragment;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;

public class EditReminderActivity extends AppCompatActivity
        implements ConfigReminderFragment.ConfigReminderListener{

    private static final String TAG = EditReminderActivity.class.getSimpleName();

    public static final String INTENT_REMINDER = "reminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_edit_reminder);


        String fragmentTitle = getString(R.string.at_edit_reminder);

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
            TrainReminder reminder = (TrainReminder) getIntent().getSerializableExtra(INTENT_REMINDER);
            FragmentManager fragmentManager = getSupportFragmentManager();
            ConfigReminderFragment fragment = ConfigReminderFragment.newInstance(reminder);

            //Replace fragment
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.edit_activity_content_frame, fragment);
            ft.commit();
        }

    }

    /* Gestione dei pulsanti nella toolbar */
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

    /* ConfigTrainReminder.ConfigReminderListener */
    @Override
    public void onConfirmReminder(TrainReminder trainReminder) {
        // callback che viene invocata quando l'utente conferma l'aggiunta del reminder
        if (BuildConfig.DEBUG) Log.v(TAG, "Modifica del reminder confermata");
        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this);
        if (trainReminderDAO.updateReminder(trainReminder)) {
            if (BuildConfig.DEBUG) Log.v(TAG, "Reminder modificato");
            Toast.makeText(this, "Reminder modificato", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (BuildConfig.DEBUG) Log.e(TAG, "Non è stato modificato il reminder");
        }
    }

    @Override
    public void onAbortReminder() {
        // callback che viene invocata quando l'utente annulla la creazione del reminder
        if (BuildConfig.DEBUG) Log.v(TAG, "Modifica del reminder annullata");
    }
}

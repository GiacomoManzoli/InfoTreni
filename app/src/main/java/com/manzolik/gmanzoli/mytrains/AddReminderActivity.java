package com.manzolik.gmanzoli.mytrains;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainDepartureStationService;
import com.manzolik.gmanzoli.mytrains.service.TrainDepartureStationServiceCallback;
import com.manzolik.gmanzoli.mytrains.service.TrainStopsService;
import com.manzolik.gmanzoli.mytrains.service.TrainStopsServiceCallback;

import java.util.Calendar;
import java.util.List;

public class AddReminderActivity extends AppCompatActivity
    implements TrainDepartureStationServiceCallback, TrainStopsServiceCallback{


    private int trainCode;
    private Station trainDeparture;
    private Calendar startTime;
    private Calendar endTime;
    private String selectedStationName;

    private Spinner spinner;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);



        System.out.println("ADD REMINDER VIEW - created");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        // Event Handler per la comparsa dei due time-picker
        Button startButton = (Button) findViewById(R.id.add_reminder_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddReminderActivity.this, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        Toast.makeText(AddReminderActivity.this, String.format("%d %d", hourOfDay, minute), Toast.LENGTH_LONG).show();
                    }
                }, 0,0, true);
                timePickerDialog.show();
            }
        });
        Button endButton = (Button) findViewById(R.id.add_reminder_end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddReminderActivity.this, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime = Calendar.getInstance();
                        endTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                        Toast.makeText(AddReminderActivity.this, String.format("%d %d", hourOfDay, minute), Toast.LENGTH_LONG).show();
                    }
                }, 0,0, true);
                timePickerDialog.show();
            }
        });

        // Event handler per il pulsante ok nella ricerca del treno
        // TODO: far comparire prima solo la selezione del treno e poi le altre opzioni.
        EditText trainCodeTextEdit = (EditText) findViewById(R.id.add_reminder_train_code);
        trainCodeTextEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    EditText view = (EditText) v;
                    TrainDepartureStationService tds = new TrainDepartureStationService(new StationDAO(AddReminderActivity.this));
                    String tCode = view.getText().toString();

                    trainCode = Integer.parseInt(tCode);

                    System.out.println("TRAIN CODE:" + tCode);
                    tds.getDepartureStations(trainCode, AddReminderActivity.this);
                    dialog = new ProgressDialog(AddReminderActivity.this);
                    dialog.setMessage("Recupero i dati del treno...");
                    dialog.show();

                }
                return false;
            }
        });

        // Configurazione dello spinner

        spinner = (Spinner) findViewById(R.id.add_reminder_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, new String[]{"Nessun treno selezionato"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.printf("SELEZIONATO %d %d %n", position, id);
                selectedStationName = (String) spinner.getAdapter().getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                System.out.println("Aborted");
            }
        });

    }

    /*
    * GESTIONE DEL MENU
    * */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_reminder_confirm){
            // Creazione del reminder
            // TODO: validare i campi dati


            StationDAO stationDAO = new StationDAO(AddReminderActivity.this);
            Station targetStation = stationDAO.getStationFromName(selectedStationName);

            TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this);
            trainReminderDAO.insertReminder(trainCode,trainDeparture.getID(),startTime,endTime,targetStation.getID());
            Toast.makeText(AddReminderActivity.this, "Reminder aggiunto", Toast.LENGTH_SHORT).show();
            finish();
        } else if (item.getItemId() == android.R.id.home){
            // Codice per usare il tasto indietro fisico
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
    *   CALLBACK PER LA STAZIONE DI PARTENZA
    * */

    @Override
    public void trainDepartureStationServiceCallbackSuccess(List<Station> stations) {
        if (stations.size() > 1) return; // TODO: gestire la presenza di più di una stazione

        Station departureStation = stations.get(0);
        trainDeparture = departureStation;
        TrainStopsService tss = new TrainStopsService();
        tss.getTrainStops(trainCode, departureStation.getCode(), this);
    }

    @Override
    public void trainDepartureStationServiceCallbackFailure(Exception exc) {
        System.err.println(exc.getMessage());
    }

    /*
    *   CALLBACK PER LE STAZIONI INTERMEDIE
    * */

    @Override
    public void trainStopsServiceCallbackSuccess(List<String> stationNamesList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, stationNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        dialog.hide();
    }

    @Override
    public void trainStopsServiceCallbackFailure(Exception exc) {
        System.err.println(exc.getMessage());
    }
}

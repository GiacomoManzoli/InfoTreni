package com.manzolik.gmanzoli.mytrains;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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

    final static String NO_STATION_SELECTED = "Nessun treno selezionato";

    private int trainCode;
    private Station trainDeparture;
    private Calendar startTime;
    private Calendar endTime;
    private String selectedStationName;

    private Spinner spinner;
    private ProgressDialog dialog;
    private Button startButton;
    private Button endButton;
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
        startButton = (Button) findViewById(R.id.add_reminder_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddReminderActivity.this,R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        String timeString = String.format("%02d:%02d", hourOfDay, minute);
                        startButton.setText(timeString);
                    }
                }, 0,0, true);
                timePickerDialog.setTitle("Ora inzio");
                timePickerDialog.show();
            }
        });
        endButton = (Button) findViewById(R.id.add_reminder_end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddReminderActivity.this,R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime = Calendar.getInstance();
                        endTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                        String timeString = String.format("%02d:%02d", hourOfDay, minute);
                        endButton.setText(timeString);
                    }
                }, 0,0, true);
                timePickerDialog.setTitle("Ora fine");
                timePickerDialog.show();
            }
        });

        // Event handler per il pulsante ok nella ricerca del treno
        EditText trainCodeTextEdit = (EditText) findViewById(R.id.add_reminder_train_code);
        trainCodeTextEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, new String[]{NO_STATION_SELECTED});
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

        spinner.setEnabled(false);
        startButton.setEnabled(false);
        endButton.setEnabled(false);
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
            if (startTime == null){
                Toast.makeText(this, "Non è stato selezionato un orario di inzio", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (endTime == null){
                Toast.makeText(this, "Non è stato selezionato un orario di fine", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (startTime.getTimeInMillis() == endTime.getTimeInMillis()){
                Toast.makeText(this, "L'orario di inizio coincide con quello di fine", Toast.LENGTH_SHORT).show();
                return true;
            }


            StationDAO stationDAO = new StationDAO(AddReminderActivity.this);
            Station targetStation = stationDAO.getStationFromName(selectedStationName);

            TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this);
            trainReminderDAO.insertReminder(trainCode, trainDeparture.getID(), startTime, endTime, targetStation.getID());

            Toast.makeText(AddReminderActivity.this, "Reminder aggiunto", Toast.LENGTH_SHORT).show();
            finish();
        } else if (item.getItemId() == android.R.id.home){
            // Codice per usare il tasto indietro fisico
            //NavUtils.navigateUpFromSameTask(this);
            onBackPressed(); // Possono esserci più parent per questa activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
    *   CALLBACK PER LA STAZIONE DI PARTENZA
    * */

    @Override
    public void trainDepartureStationServiceCallbackSuccess(final List<Station> stations) {

        if (stations.size() > 1) {
            final String[] stationNames = new String[stations.size()];
            for(int i = 0; i < stationNames.length; i++){
                stationNames[i] = stations.get(i).getName();
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(AddReminderActivity.this);
            builder.setTitle("Seleziona la stazione di partenza").setItems(stationNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    trainDeparture = stations.get(which);
                    TrainStopsService tss = new TrainStopsService();
                    tss.getTrainStops(trainCode, trainDeparture.getCode(), AddReminderActivity.this);
                    spinner.setEnabled(true);
                    startButton.setEnabled(true);
                    endButton.setEnabled(true);
                }
            });
            builder.show();

        }else {
            trainDeparture = stations.get(0);
            TrainStopsService tss = new TrainStopsService();
            tss.getTrainStops(trainCode, trainDeparture.getCode(), this);
            spinner.setEnabled(true);
            startButton.setEnabled(true);
            endButton.setEnabled(true);
        }
    }

    @Override
    public void trainDepartureStationServiceCallbackFailure(Exception exc) {

        try {
            throw exc;
        } catch (TrainDepartureStationService.TrainNotFoundException e) {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage(exc.getMessage());
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }catch (Exception e) {
            System.err.println(exc.getMessage());
        } finally {
            dialog.hide();

        }
    }

    /*
    *   CALLBACK PER LE STAZIONI INTERMEDIE
    * */

    @Override
    public void trainStopsServiceCallbackSuccess(List<String> stationNamesList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stationNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        dialog.hide();
    }

    @Override
    public void trainStopsServiceCallbackFailure(Exception exc) {
        System.err.println(exc.getMessage());
    }
}

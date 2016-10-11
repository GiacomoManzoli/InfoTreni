package com.manzolik.gmanzoli.mytrains;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainStopsService;

import java.util.Calendar;
import java.util.List;


public class ConfigReminderFragment extends Fragment
    implements TrainStopsService.TrainStopsServiceListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TRAIN_CODE = "train_code";
    private static final String TRAIN_DEPARTURE = "train_departure";
    final static String NO_STATION_SELECTED = "Seleziona stazione da notificare";


    private int trainCode;
    private Station trainDepartureStation;
    private Calendar startTime;
    private Calendar endTime;
    private String selectedStationName;
    private boolean shouldShowProgressDialog;


    private Spinner spinner; // Spinner contenente le varie tappe effettuate dal treno
    private ProgressDialog dialog;
    private Button startButton; // Bottone per la scelta del tempo d'inizio
    private Button endButton; // Bottone per la scelta del tempo di fine
    private Button trainButtonView; // Bottone che visualizza il codice del treno



    public ConfigReminderFragment() {
        // Required empty public constructor
    }

    public static ConfigReminderFragment newInstance(int trainCode, Station departureStation) {
        ConfigReminderFragment fragment = new ConfigReminderFragment();
        Bundle args = new Bundle();
        args.putInt(TRAIN_CODE, trainCode);
        args.putSerializable(TRAIN_DEPARTURE, departureStation);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trainCode = getArguments().getInt(TRAIN_CODE);
            trainDepartureStation = (Station) getArguments().getSerializable(TRAIN_DEPARTURE);

            TrainStopsService trainStopsService = new TrainStopsService();
            trainStopsService.getTrainStops(trainCode, trainDepartureStation.getCode(), this);
            // Non posso renderizzare subito il progressDialog, devo aspettare onCreateView
            shouldShowProgressDialog = true;

        }


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config_reminder, container, false);

        // Visualizza il codice del treno selezionato
        trainButtonView = (Button) view.findViewById(R.id.config_reminder_fragment_train_button);
        trainButtonView.setText(String.format("%d - %s", trainCode, trainDepartureStation.getName()));



        // Event Handler per la comparsa dei due time-picker
        startButton = (Button) view.findViewById(R.id.config_reminder_fragment_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener(){
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
        endButton = (Button) view.findViewById(R.id.config_reminder_fragment_end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime = Calendar.getInstance();
                        endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                        String timeString = String.format("%02d:%02d", hourOfDay, minute);
                        endButton.setText(timeString);
                    }
                }, 0, 0, true);
                timePickerDialog.setTitle("Ora fine");
                timePickerDialog.show();
            }
        });

        // Configurazione dello spinner

        spinner = (Spinner) view.findViewById(R.id.config_reminder_fragment_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_spinner_layout, new String[]{NO_STATION_SELECTED});
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

        if (shouldShowProgressDialog){
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Recupero le fermate del treno...");
            dialog.show();
        }

        return view;
    }


    /*
    * Gestione del menu
    * */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        // http://stackoverflow.com/questions/30847096/android-getmenuinflater-in-a-fragment-subclass-cannot-resolve-method
        inflater.inflate(R.menu.menu_add_reminder, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_reminder_confirm){
            // Creazione del reminder
            if (selectedStationName.equals(NO_STATION_SELECTED)){
                Toast.makeText(getActivity(), "Non è stata selezionata una stazione da nofiticare", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (startTime == null){
                Toast.makeText(getActivity(), "Non è stato selezionato un orario di inzio", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (endTime == null){
                Toast.makeText(getActivity(), "Non è stato selezionato un orario di fine", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (startTime.getTimeInMillis() == endTime.getTimeInMillis()){
                Toast.makeText(getActivity(), "L'orario di inizio coincide con quello di fine", Toast.LENGTH_SHORT).show();
                return true;
            }


            StationDAO stationDAO = new StationDAO(getActivity());
            Station targetStation = stationDAO.getStationFromName(selectedStationName);

            TrainReminderDAO trainReminderDAO = new TrainReminderDAO(getActivity());
            trainReminderDAO.insertReminder(trainCode, trainDepartureStation.getID(), startTime, endTime, targetStation.getID());

            Toast.makeText(getActivity(), "Reminder aggiunto", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else if (item.getItemId() == android.R.id.home){
            // Codice per usare il tasto indietro fisico
            getActivity().onBackPressed();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    *   CALLBACK PER LE STAZIONI INTERMEDIE
    *   TrainStopsService.TrainStopsServiceListener
    * */

    @Override
    public void onTrainStopsSuccess(List<String> stationNamesList) {
        stationNamesList.add(0,NO_STATION_SELECTED);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.custom_spinner_layout, stationNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (dialog != null) {
            dialog.hide();
        }
    }

    @Override
    public void onTrainStopsFailure(Exception exc) {
        if (dialog != null) {
            dialog.hide();
        }
        System.err.println(exc.getMessage());
    }
}

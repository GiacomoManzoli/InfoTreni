package com.manzolik.gmanzoli.mytrains.components;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainStopsService;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ConfigReminderFragment extends Fragment
    implements TrainStopsService.TrainStopsServiceListener,
View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = ConfigReminderFragment.class.getSimpleName();

    private static final String ARG_TRAIN = "train";
    private static final String ARG_START_TIME = "stime";
    private static final String ARG_END_TIME = "etime";
    private static final String ARG_STATION_NAME = "sname";
    private static final String ARG_REMINDER = "treminder";

    final static String NO_STATION_SELECTED = "Seleziona stazione da notificare";

    private TrainReminder mTrainReminder; // Reminder da aggiungere
    private Train mTrain; // Treno per il quale creare il reminder
    private Calendar mStartTime; // Orario d'inizio delle notifiche
    private Calendar mEndTime; // Orario di fine delle notifiche
    private String mSelectedStationName; // Stazione di riferimento per le notifiche


    private Spinner mSpinner; // Spinner contenente le varie tappe effettuate dal treno
    private Button mStartButton; // Bottone per la scelta del tempo d'inizio
    private Button mEndButton; // Bottone per la scelta del tempo di fine


    // Configurazione del TimePickerDialog
    private static final int SELECTING_TIME_START = 0;
    private static final int SELECTING_TIME_END = 1;
    private int mCurrentTimeMode;


    private ConfigReminderListener mListener;
    private TimePickerDialog mTimePickerDialog;
    private ProgressDialog mDialog;

    public ConfigReminderFragment() {
        // Required empty public constructor
    }

    /*
    * Factory method per la costruzione "in aggiunta", richiede l'informazione minima riguardo
    * il treno da monitorare
    * */
    public static ConfigReminderFragment newInstance(Train train) {
        ConfigReminderFragment fragment = new ConfigReminderFragment();
        Bundle args = new Bundle();
        // reminder "fake" che fa da segna posto per quello in creaizone
        TrainReminder reminder = new TrainReminder(-1, train, null, null, null);
        args.putSerializable(ARG_REMINDER, reminder);
        fragment.setArguments(args);
        return fragment;
    }

    /*
    * Factory method per la costruzione "in modifica", richiede un reminder per popolare tutto il
    * form
    * */
    public static ConfigReminderFragment newInstance(TrainReminder trainReminder) {
        ConfigReminderFragment fragment = new ConfigReminderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, trainReminder);
        fragment.setArguments(args);
        return fragment;
    }

    /*
    *  onCreate - Recupera le informazioni dallo stato salvato o dal bundle, dando la precedenza
    *  allo stato salvato
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);

        // Prima prova a ripristinare lo stato precedente
        // se non riesce utilizza gli argomenti passati dal costruttore
        if (savedInstanceState != null) {
            mTrainReminder = (TrainReminder) savedInstanceState.getSerializable(ARG_REMINDER);


           /* mTrain = (Train) savedInstanceState.getSerializable(ARG_TRAIN);
            mStartTime = (Calendar) savedInstanceState.getSerializable(ARG_START_TIME);
            mEndTime = (Calendar) savedInstanceState.getSerializable(ARG_END_TIME);
            mSelectedStationName = savedInstanceState.getString(ARG_STATION_NAME);*/
        } else if (getArguments() != null) {
            mTrainReminder = (TrainReminder) getArguments().getSerializable(ARG_REMINDER);
        }

        if (mTrainReminder != null) {
            mTrain = mTrainReminder.getTrain();
            mStartTime = mTrainReminder.getStartTime();
            mEndTime = mTrainReminder.getEndTime();
            Station targetStation = mTrainReminder.getTargetStation();
            if (targetStation != null) {
                mSelectedStationName = targetStation.getName();
            } else {
                mSelectedStationName = NO_STATION_SELECTED;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_config_reminder, container, false);

        // Visualizza il codice del treno selezionato
        Button trainButtonView = (Button) view.findViewById(R.id.config_reminder_fragment_train_button);
        trainButtonView.setText(String.format("%s - %s", mTrain.getCode(), mTrain.getDepartureStation().getName()));


        // Event Handler per la comparsa dei due time-picker
        mStartButton = (Button) view.findViewById(R.id.config_reminder_fragment_start_button);
        mStartButton.setOnClickListener(this);
        if (mStartTime != null) {
            int hourOfDay = mStartTime.get(Calendar.HOUR_OF_DAY);
            int minute = mStartTime.get(Calendar.MINUTE);
            mStartButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }
        mEndButton = (Button) view.findViewById(R.id.config_reminder_fragment_end_button);
        mEndButton.setOnClickListener(this);
        if (mEndTime != null) {
            int hourOfDay = mEndTime.get(Calendar.HOUR_OF_DAY);
            int minute = mEndTime.get(Calendar.MINUTE);
            mEndButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }

        // Configurazione dello spinner
        mSpinner = (Spinner) view.findViewById(R.id.config_reminder_fragment_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_spinner_layout, new String[]{ NO_STATION_SELECTED});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (BuildConfig.DEBUG) Log.d(TAG, String.format("Selezionato %d %d %n", position, id));
                String selectedName = (String) mSpinner.getAdapter().getItem(position);
                if (!selectedName.equals(NO_STATION_SELECTED)) {
                    mSelectedStationName = selectedName;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Nessuna stazione selezionata");
            }
        });

        return view;
    }

    /*
    * onStart: recupera le informazioni riguardo le fermate effettaute dal treno
    * */
    @Override
    public void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart");
        TrainStopsService trainStopsService = new TrainStopsService();
        trainStopsService.getTrainStops(mTrain.getCode(), mTrain.getDepartureStation().getCode(), this);
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Recupero le fermate del treno...");
        mDialog.show();
    }

    /*
    * onPause: se sono configurati e aperti dismette i vari dialog per evitare memory leak
    * */
    @Override
    public void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mTimePickerDialog != null && mTimePickerDialog.isShowing()) {
            mTimePickerDialog.dismiss();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(ARG_REMINDER, mTrainReminder);

        outState.putSerializable(ARG_TRAIN, mTrain);
        outState.putSerializable(ARG_START_TIME, mStartTime);
        outState.putSerializable(ARG_END_TIME, mEndTime);
        outState.putString(ARG_STATION_NAME, mSelectedStationName);
    }

    /*
    * Gestione del menu:
    * - creazione
    * - esecuzione dell'azione:
    *   - check dei dati inseriti
    *   - chiamata della callback
    *   - gestione del pulsante indietro
    * */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_config_reminder, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == R.id.config_reminder_confirm){
            if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected - Confirm action");
            // Controllo dei dati
            if (mSelectedStationName.equals(NO_STATION_SELECTED)){
                Toast.makeText(getActivity(), "Non è stata selezionata una stazione da nofiticare", Toast.LENGTH_SHORT).show();
            } else if (mStartTime == null){
                Toast.makeText(getActivity(), "Non è stato selezionato un orario di inizio", Toast.LENGTH_SHORT).show();
            } else if (mEndTime == null){
                Toast.makeText(getActivity(), "Non è stato selezionato un orario di fine", Toast.LENGTH_SHORT).show();
            } else if (mStartTime.getTimeInMillis() == mEndTime.getTimeInMillis()){
                Toast.makeText(getActivity(), "L'orario di inizio coincide con quello di fine", Toast.LENGTH_SHORT).show();
            } else {
                StationDAO stationDAO = new StationDAO(getActivity());
                Station targetStation = stationDAO.getStationFromName(mSelectedStationName);

                mTrainReminder.setStartTime(mStartTime);
                mTrainReminder.setEndTime(mEndTime);
                mTrainReminder.setTargetStation(targetStation);

                if (mListener != null) mListener.onConfirmReminder(mTrainReminder);
            }
        } else if (item.getItemId() == android.R.id.home){
            if (BuildConfig.DEBUG) Log.d(TAG, "onOptionsItemSelected - home button");
            // Se l'utente preme il tasto indietro, annulla l'operazione
            if (mListener != null) mListener.onAbortReminder();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true; // Evento gestito, ferma la propagazione
    }

    /*
    * Handler dei click sui vari pulsanti
    * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.config_reminder_fragment_start_button:
                mCurrentTimeMode = ConfigReminderFragment.SELECTING_TIME_START;
                showTimePicker("Ora inizio", mStartTime);

                break;
            case R.id.config_reminder_fragment_end_button:
                mCurrentTimeMode = ConfigReminderFragment.SELECTING_TIME_END;
                showTimePicker("Ora fine", mEndTime);
                break;
        }
    }

    /*
    * Crea e visualizza un TimePickerDialog
    * */
    private void showTimePicker(String title, Calendar defaultValue) {
        int hourOfDay = 0;
        int minute = 0;
        if (defaultValue != null) {
            hourOfDay = defaultValue.get(Calendar.HOUR_OF_DAY);
            minute = defaultValue.get(Calendar.MINUTE);
        }
        mTimePickerDialog = new TimePickerDialog(
                getActivity(),
                R.style.TimePickerTheme,
                this, // event handler
                hourOfDay,
                minute,
                true);
        mTimePickerDialog.setTitle(title);
        mTimePickerDialog.show();
    }

    /*
    * Handler per il TimePickerDialog*/
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

        switch (mCurrentTimeMode) {
            case ConfigReminderFragment.SELECTING_TIME_START:
                mStartTime = Calendar.getInstance();
                mStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mStartTime.set(Calendar.MINUTE, minute);
                mStartButton.setText(timeString);
                break;
            case ConfigReminderFragment.SELECTING_TIME_END:
                mEndTime = Calendar.getInstance();
                mEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mEndTime.set(Calendar.MINUTE, minute);
                mEndButton.setText(timeString);
                break;
        }
    }


    /*
    *   TrainStopsService.TrainStopsServiceListener
    *   callaback per le stazioni intermedie
    * */
    @Override
    public void onTrainStopsSuccess(List<String> stationNamesList) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        stationNamesList.add(0, NO_STATION_SELECTED);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.custom_spinner_layout, stationNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);
        if (mSelectedStationName != null && !mSelectedStationName.equals("")) {
            int pos = stationNamesList.indexOf(mSelectedStationName);
            if (pos != -1) {
                mSpinner.setSelection(pos);
            }
        }
    }
    @Override
    public void onTrainStopsFailure(Exception exc) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (BuildConfig.DEBUG) Log.e(TAG, exc.getMessage());
    }


    /* Metodi per la gestione del listener */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConfigReminderListener) {
            mListener = (ConfigReminderListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " deve implementare ConfigReminderListener");
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Listener: "+ mListener.getClass().getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /* Listener per gli eventi del fragment*/
    public interface ConfigReminderListener {
        void onConfirmReminder(TrainReminder trainReminder);
        void onAbortReminder();
    }
}

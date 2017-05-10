package com.manzolik.gmanzoli.mytrains.fragments;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.FindStationActivity;
import com.manzolik.gmanzoli.mytrains.NoConnectivityActivity;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TravelSolution;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainDAO;
import com.manzolik.gmanzoli.mytrains.http.TrainDepartureStationService;
import com.manzolik.gmanzoli.mytrains.http.TravelSolutionsService;


import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/*
 * Fragment che permette all'utente di scegliere un treno
 * */

// Gestire meglio la rotazione http://stackoverflow.com/questions/13305861/fool-proof-way-to-handle-fragment-on-orientation-change

public class FindTrainFragment extends DialogFragment
        implements TrainDepartureStationService.TrainDepartureStationServiceListener,
        TravelSolutionsService.TravelSolutionsServiceListener,
        View.OnClickListener,
        View.OnKeyListener {

    private static final String TAG = FindTrainFragment.class.getSimpleName();

    // savedInstance key
    private static final String KEY_TRAIN_CODE = "train_code";
    private static final String KEY_DEPARTURE_TIME_HOUR = "departure_time_h";
    private static final String KEY_DEPARTURE_TIME_MINUTE = "departure_time_m";
    private static final String KEY_DEPARTURE_ID = "departure_id";
    private static final String KEY_ARRIVAL_ID = "arrival_id";

    // request code per FindStationActivity
    private static final int REQUEST_DEPARTURE = 1;
    private static final int REQUEST_ARRIVAL = 2;

    // Campi dati utili per l'UI
    private String mTrainCode; // Codice del treno selezionato dall'utente
    private Station mSearchDepartureStation;
    private Station mSearchArrivalStation;
    private Calendar mDepartureTime;

    List<TravelSolution.SolutionElement> mTrains; // Lista di possibili treni
    List<String> mTrainsString; // Lista dei possibili treni da utilizzare come data source per permettere all'utente di scegliere il codice del treno


    private OnTrainFoundListener mListener;

    private EditText mTrainCodeTextEdit;
    private ProgressDialog mDialog;
    private ViewGroup mContainer;

    public FindTrainFragment() {
        // Required empty public constructor
    }

    public static FindTrainFragment newInstance() {
        FindTrainFragment fragment = new FindTrainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /* Fragment Lifecycle*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        if (savedInstanceState != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Carico lo stato...");
            mTrainCode = savedInstanceState.getString(KEY_TRAIN_CODE);

            int depHour = savedInstanceState.getInt(KEY_DEPARTURE_TIME_HOUR,-1);
            int depMinute = savedInstanceState.getInt(KEY_DEPARTURE_TIME_MINUTE,-1);
            if (depHour != -1 && depMinute != -1) {
                mDepartureTime = Calendar.getInstance();
                mDepartureTime.set(Calendar.HOUR_OF_DAY, depHour);
                mDepartureTime.set(Calendar.MINUTE, depMinute);
            }

            StationDAO stationDao = new StationDAO(getContext());
            int depId = savedInstanceState.getInt(KEY_DEPARTURE_ID);
            if (depId != -1 ) mSearchDepartureStation = stationDao.getStationFromId(depId);
            int arrId = savedInstanceState.getInt(KEY_ARRIVAL_ID);
            if (arrId != -1 ) mSearchDepartureStation = stationDao.getStationFromId(arrId);


        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Non c'è uno stato da ripristinare");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
        if (mDialog != null){
            mDialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_train, container, false);
        mContainer = container;

        mTrainCodeTextEdit = (EditText) view.findViewById(R.id.find_train_fragment_train_code_text);
        mTrainCodeTextEdit.setOnKeyListener(this);

        // Configurazione del pulsante Go
        ImageButton goButton = (ImageButton) view.findViewById(R.id.find_train_fragment_go_button);
        goButton.setOnClickListener(this);

        // Configurazione pulsante per la stazione di partenza
        Button departureStationButton = (Button) view.findViewById(R.id.find_train_fragment_dep_station);
        departureStationButton.setOnClickListener(this);

        // Configurazione pulsante per la stazione di arrivo
        Button arrivalStationButton = (Button) view.findViewById(R.id.find_train_fragment_arr_station);
        arrivalStationButton.setOnClickListener(this);

        // Configurazione del pulsante per il time picker
        Button timePickerButton = (Button) view.findViewById(R.id.find_train_fragment_time_button);
        timePickerButton.setOnClickListener(this);

        // Configurazione del pulsante per la ricerca del codice
        Button findButton = (Button) view.findViewById(R.id.find_train_fragment_find);
        findButton.setOnClickListener(this);

        // Se ho già a disposizione i dati, ripopolo il form
        if (mTrainCode != null && !mTrainCode.equals("")){
            mTrainCodeTextEdit.setText(mTrainCode);
        }
        if (mSearchArrivalStation != null) {
            arrivalStationButton.setText(mSearchArrivalStation.getName());
        }
        if (mSearchDepartureStation != null) {
            departureStationButton.setText(mSearchDepartureStation.getName());
        }
        if (mDepartureTime != null) {
            SimpleDateFormat format = new SimpleDateFormat( "HH:mm", Locale.getDefault());
            timePickerButton.setText(format.format(mDepartureTime.getTime()));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "Salvo lo stato...");

        // onSaveInstance può essere invocato anche prima di onCreateView (se la view non viene mai
        // creata).
        // Questo si verifica se c'è un'altra Activity/Fragment sopra questo fragment e si verifica
        // più di un cambio di configurazione.
        if (mTrainCodeTextEdit != null) {
            mTrainCode = mTrainCodeTextEdit.getText().toString();
        }
        outState.putString(KEY_TRAIN_CODE, mTrainCode);

        if (mDepartureTime != null) {
            outState.putInt(KEY_DEPARTURE_TIME_HOUR, mDepartureTime.get(Calendar.HOUR_OF_DAY));
            outState.putInt(KEY_DEPARTURE_TIME_MINUTE, mDepartureTime.get(Calendar.MINUTE));

        } else {
            outState.putInt(KEY_DEPARTURE_TIME_HOUR, -1);
            outState.putInt(KEY_DEPARTURE_TIME_MINUTE, -1);
        }

        int depId = (mSearchDepartureStation != null)? mSearchDepartureStation.getId() : -1;
        outState.putInt(KEY_DEPARTURE_ID, depId);
        int arrId = (mSearchArrivalStation != null)? mSearchArrivalStation.getId() : -1;
        outState.putInt(KEY_DEPARTURE_ID, arrId);
    }

    /*
    * View.OnKeyListener
    * gestione della pressione del tasto Ok sulla tastiera del telefono.
    * Alla pressione del tasto Ok viene fatta partire la ricerca
    */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.getId() == R.id.find_train_fragment_train_code_text) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {

                String tCode = ((EditText)v).getText().toString();
                if (tCode.equals("")) {
                    return false;
                }
                selectTrain(tCode);
            }
        }
        return false;
    }

    /*
    * View.onClickListener
    * fa uno switch dei possibili elementi della view del fragment e chiama il gestore corretto
    */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.find_train_fragment_dep_station:
                onClickSelectStation(v, R.id.find_train_fragment_dep_station);
                break;
            case R.id.find_train_fragment_arr_station:
                onClickSelectStation(v, R.id.find_train_fragment_arr_station);
                break;
            case R.id.find_train_fragment_time_button:
                onClickTimer((Button)v.findViewById(R.id.find_train_fragment_time_button));
                break;
            case R.id.find_train_fragment_go_button:
                onClickGo();
                break;
            case R.id.find_train_fragment_find:
                onClickFind();
                break;
        }
    }

    /*
    * Handler per il click sui pulsanti per la selezione della stazione di partenza/arrivo
    * */
    private void onClickSelectStation(View v, final int buttonId) {
        String fragmentTitle = "";
        int requestCode = 0;
        switch (buttonId) {
            case R.id.find_train_fragment_dep_station:
                fragmentTitle = "Stazione di partenza";
                requestCode = REQUEST_DEPARTURE;
                break;
            case R.id.find_train_fragment_arr_station:
                fragmentTitle = "Stazione di arrivo";
                requestCode = REQUEST_ARRIVAL;
                break;
        }

        Intent i = new Intent(getContext(), FindStationActivity.class);
        i.putExtra(FindStationActivity.INTENT_TITLE, fragmentTitle);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Station station = (Station) data.getSerializableExtra(FindStationActivity.SELECTED_STATION);
            if (BuildConfig.DEBUG) Log.d(TAG,"Stazione selezionata: "+station.toString());

            int buttonId = 0; // buttonId del bottone da aggioranre
            switch (requestCode) {
                case REQUEST_DEPARTURE:
                    mSearchDepartureStation = station;
                    buttonId = R.id.find_train_fragment_dep_station;
                    break;
                case REQUEST_ARRIVAL:
                    mSearchArrivalStation = station;
                    buttonId = R.id.find_train_fragment_arr_station;
                    break;
            }
            View v = getView();
            if (v != null) {
                Button button = (Button)v.findViewById(buttonId);
                button.setText(station.getName());
            }
        }
    }

    /*
        * Handler per il click sul pulsante per la scelta dell'orario
        * */
    private void onClickTimer(final Button timerButton) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDepartureTime = Calendar.getInstance();
                mDepartureTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDepartureTime.set(Calendar.MINUTE, minute);
                String timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                timerButton.setText(timeString);
            }
        }, 0, 0, true);
        timePickerDialog.setTitle("Orario di partenza");
        timePickerDialog.show();
    }

    /* Gestione del click sul pulsante "Go"
    * quando l'utente preme questo pulsante ha inserito il codice del treno
    * quindi è solo necessario recuperare i dati del treno*/
    private void onClickGo() {
        String tCode = mTrainCodeTextEdit.getText().toString();
        if (tCode.equals("")) {
            return;
        }
        // Nascondo la tastiera (se presente)
        InputMethodManager inputMethodManager = (InputMethodManager) FindTrainFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            IBinder windowToken = currentFocus.getWindowToken();
            if (windowToken != null) {
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
            }
        }
        // Recupera i dati del treno
        selectTrain(tCode);
    }

    /* Gestione del click sul pulsante Find
    * quando l'utente ha premuto questo pulsante dovrebbe aver inserito le informazioni necessarie
    * per trovare il treno, parte quindi la ricerca con TravelSolutionService.
    * Una volta trovate le soluzioni viene mostrato (nella callback) un Dialog per
    * la selezione del codice e una volta selezionato il codice viene effettuata la chiamata a
    * selectTrain(trainCode) */
    private void onClickFind() {
        TravelSolutionsService travelSolutionsService = new TravelSolutionsService();
        if (mSearchDepartureStation == null || mSearchArrivalStation == null || mDepartureTime == null){
            Toast.makeText(getActivity(), "Non sono stati inseriti tutti i dati necessari per cercare il treno", Toast.LENGTH_SHORT).show();
        } else {
            travelSolutionsService.findSolutions(mSearchDepartureStation,
                    mSearchArrivalStation,
                    mDepartureTime,
                    5,
                    this);
            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage("Cerco i treni per la tratta...");
            mDialog.show();
        }
    }

    /* Metodo che viene invocato quando l'utente ha inserito il codice del treno */
    private void selectTrain(String trainCode){
        mTrainCode = trainCode;
        // Faccio partire la richiesta per recuperare l'informazione relativa alla stazione di partenza
        TrainDepartureStationService tds = new TrainDepartureStationService(new StationDAO(getActivity()));
        tds.getDepartureStations(trainCode, this);
        // Mostro il progress dialog
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Recupero i dati del treno...");
        mDialog.show();
    }

    /*
    *   CALLBACK PER LA STAZIONE DI PARTENZA
    *   Viene invocata quando la ricerca della stazione di partenza per `mTrainCode` è stata
    *   completata con successo.
    *   TrainDepartureStationService.TrainDepartureStationServiceListener
    * */
    @Override
    public void onTrainDepartureStationSuccess(List<Station> stationList) {
        final List<Station> stations = stationList;
        mDialog.dismiss();
        if (stations.size() > 1) {
            // Se ci sono più stazioni viene mostrato un dialog che permette all'utente di scegliere
            // quella corretta.
            // Si, nel database di Trenitalia ci sono più treni con lo stesso codice.
            final String[] stationNames = new String[stations.size()];
            for(int i = 0; i < stationNames.length; i++){
                stationNames[i] = stations.get(i).getName();
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            builder.setTitle("Seleziona la stazione di partenza")
                    .setItems(stationNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmTrainSelection(mTrainCode, stations.get(which));
                }
            }).show();

        }else {
            confirmTrainSelection(mTrainCode, stations.get(0));
        }
    }

    /*
    * Metodo che viene invocato per selezionare effettivamente in treno.
    * In questo momento il treno è univocamente identificato dalla sua stazione di partenza.
    * Reminder: Il codice del treno non identifica in modo univoco una tratta. Ci sono tratte
    * diverse che hanno lo stesso codice.
    * */
    private void confirmTrainSelection(String trainCode, Station departureStation) {
        TrainDAO trainDAO = new TrainDAO(getActivity());
        Train train = trainDAO.insertTrainIfNotExists(trainCode, departureStation.getId());
        if (mListener != null) {
            mListener.onTrainFound(train);
        }
    }

    @Override
    public void onTrainDepartureStationFailure(Exception exc) {
        mDialog.dismiss();
        try {
            throw exc;
        } catch (TrainDepartureStationService.TrainNotFoundException e) {
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            builder.setMessage(exc.getMessage());
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        } catch (UnknownHostException e) {
            // No internet connection
            Intent i = new Intent(getContext(), NoConnectivityActivity.class);
            startActivity(i);
            getActivity().finish();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
        }
    }


    /*
    *   TravelSolutionsService.TravelSolutionsServiceListener
    * */

    @Override
    @SuppressWarnings("unchecked")
    public void onTravelSolutionsSuccess(List<TravelSolution> solutions) {
        mDialog.dismiss();
        mTrains = new ArrayList<>();
        for (TravelSolution ts: solutions) {
            mTrains.addAll(ts.getElements());
        }
        // Metodo poco ortodosso per rimuovere eventuali duplicati
        Set<TravelSolution.SolutionElement> s = new LinkedHashSet<>(mTrains);
        mTrains = new ArrayList<>(s);

        mTrainsString = new ArrayList<>();
        for (TravelSolution.SolutionElement se:mTrains) {
            mTrainsString.add(se.toString());
        }

        TrainListFragment df = TrainListFragment.newInstance((ArrayList<String>) mTrainsString);
        df.setOnTrainSelectedListener(new TrainListFragment.OnTrainSelectedListener() {
            @Override
            public void onTrainSelected(int position, String string) {
                selectTrain(mTrains.get(position).getTrainCode());
            }
        });
        df.show(getFragmentManager(), "chooseTrain");
    }



    @Override
    public void onTravelSolutionsFailure(Exception exc) {
        mDialog.dismiss();
        try {
            throw exc;
        } catch (TrainDepartureStationService.TrainNotFoundException e) {
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            builder.setMessage(exc.getMessage());
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        } catch (UnknownHostException e) {
            // No internet connection
            Intent i = new Intent(getContext(), NoConnectivityActivity.class);
            startActivity(i);
            getActivity().finish();
        } catch (TravelSolutionsService.NoSolutionsFoundException e) {
            Toast.makeText(getContext(), "Nono sono stati trovati treni che effettuano la tratta" +
                    " selezionata", Toast.LENGTH_LONG)
                    .show();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
        }
    }

    /*
    * Metodi per la gestione del listener
    * */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Questo Fragment può essere visualizzato sia dentro un altro Fragment(QuickSearchFragment)
        // che dentro un'Activity (AddReminderActivity)
        // Quindi nella scelta del listener viene prima controllato se è presente un ParentFragment
        // e nel caso viene utilizzato quello, altrimenti viene utilizzata l'Activity.

        // http://stackoverflow.com/questions/39491655/communication-between-nested-fragments-in-android

        Fragment parentFragment = getParentFragment();

        if (parentFragment != null && parentFragment instanceof OnTrainFoundListener) {
            mListener = (OnTrainFoundListener) parentFragment;
        } else if (context instanceof OnTrainFoundListener) {
            mListener = (OnTrainFoundListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " deve implementare OnTrainFoundListener");
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Listener: "+ mListener.getClass().getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    // Callback da chiamare quando viene selezionato correttamente un treno
    public interface OnTrainFoundListener {
        void onTrainFound(Train train);
    }
}

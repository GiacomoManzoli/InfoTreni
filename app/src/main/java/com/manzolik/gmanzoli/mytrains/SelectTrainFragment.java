package com.manzolik.gmanzoli.mytrains;


import android.app.TimePickerDialog;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.TravelSolution;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainDepartureStationService;
import com.manzolik.gmanzoli.mytrains.service.TravelSolutionsService;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment che permette all'utente di scegliere un treno
 * */

// Gestire meglio la rotazione http://stackoverflow.com/questions/13305861/fool-proof-way-to-handle-fragment-on-orientation-change

public class SelectTrainFragment extends DialogFragment
    implements TrainDepartureStationService.TrainDepartureStationServiceListener,
               TravelSolutionsService.TravelSolutionsServiceListener {

    private int trainCode; // Codice del treno selezionato
    private Station trainDeparture; // Stazione di partenza del treno selezionato
    List<TravelSolution.SolutionElement> trains; // Lista di possibili treni
    List<String> trainsString; // Lista dei possibili treni da utilizzare come data source per permettere all'utente di scegliere il codice del treno


    private Station searchDepartureStation;
    private Station searchArrivalStation;
    private Calendar departureTime;

    private OnTrainSelectedListener mListener;

    private ProgressDialog dialog;

    public SelectTrainFragment() {
        // Required empty public constructor
    }

    public static SelectTrainFragment newInstance() {
        SelectTrainFragment fragment = new SelectTrainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_train, container, false);

        // Handler per la ricerca del codice del treno alla pressione del tasto "Ok" della tastiera
        // che compare quanto l'utente seleziona la TextView
        EditText trainCodeTextEdit = (EditText) view.findViewById(R.id.select_train_fragment_train_code_text);
        trainCodeTextEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    String tCode = ((EditText)v).getText().toString();
                    if (tCode.equals("")) {
                        return false;
                    }
                    selectTrain(Integer.parseInt(tCode));
                }
                return false;
            }
        });

        final ImageButton goButton = (ImageButton) view.findViewById(R.id.select_train_fragment_go_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText v = (EditText) SelectTrainFragment.this.getView().findViewById(R.id.select_train_fragment_train_code_text);
                String tCode = v.getText().toString();
                if (tCode.equals("")) {
                    return;
                }
                // Nascondo la tastiera (se presente)

                InputMethodManager inputMethodManager = (InputMethodManager) SelectTrainFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                IBinder windowToken = SelectTrainFragment.this.getActivity().getCurrentFocus().getWindowToken();
                if (windowToken != null) {
                    inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
                }


                selectTrain(Integer.parseInt(tCode));

            }
        });

        // Configurazione pulsante per la stazione di partenza
        final Button departureStationButton = (Button) view.findViewById(R.id.select_train_fragment_dep_station);
        departureStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FindStationFragment dialogFragment = FindStationFragment.newInstance("Stazione di partenza");
                dialogFragment.setOnStationSelectedListener(new FindStationFragment.OnStationSelectedListener() {
                    @Override
                    public void onStationSelected(Station station) {
                        searchDepartureStation = station;
                        departureStationButton.setText(station.getName());
                    }
                });
                dialogFragment.show(getFragmentManager().beginTransaction(), "FindDept");
            }
        });

        // Configurazione pulsante per la stazione di arrivo
        final Button arrivalStationButton = (Button) view.findViewById(R.id.select_train_fragment_arr_station);
        arrivalStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FindStationFragment dialogFragment = FindStationFragment.newInstance("Stazione di arrivo");
                dialogFragment.setOnStationSelectedListener(new FindStationFragment.OnStationSelectedListener() {
                    @Override
                    public void onStationSelected(Station station) {
                        searchArrivalStation = station;
                        arrivalStationButton.setText(station.getName());
                    }
                });
                dialogFragment.show(getFragmentManager().beginTransaction(),"FindArr");
            }
        });

        // Configurazione del pulsante per il time picker
        final Button timePickerButton = (Button) view.findViewById(R.id.select_train_fragment_time_button);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        departureTime = Calendar.getInstance();
                        departureTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        departureTime.set(Calendar.MINUTE, minute);
                        String timeString = String.format("%02d:%02d", hourOfDay, minute);
                        timePickerButton.setText(timeString);
                    }
                }, 0, 0, true);
                timePickerDialog.setTitle("Orario di partenza");
                timePickerDialog.show();
            }
        });

        // Configurazione del pulsante per la ricerca del codice
        final Button findButton = (Button) view.findViewById(R.id.select_train_fragment_find);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TravelSolutionsService travelSolutionsService = new TravelSolutionsService();
                if (searchDepartureStation == null || searchArrivalStation == null || departureTime == null){
                    Toast.makeText(getActivity(), "Non sono stati inseriti tutti i dati necessari per cercare il treno", Toast.LENGTH_SHORT).show();
                } else {
                    travelSolutionsService.findSolutions(searchDepartureStation, searchArrivalStation, departureTime, 5, SelectTrainFragment.this);
                    dialog = new ProgressDialog(getActivity());
                    dialog.setMessage("Cerco i treni per la tratta...");
                    dialog.show();
                }

            }
        });
        // Se ho già a disposizione i dati, ripopolo il form
        if (trainCode != 0){
            trainCodeTextEdit.setText(String.format("%d", trainCode));
        }
        if (searchArrivalStation != null) {
            arrivalStationButton.setText(searchArrivalStation.getName());
        }
        if (searchDepartureStation != null) {
            departureStationButton.setText(searchDepartureStation.getName());
        }
        if (departureTime != null) {
            SimpleDateFormat format = new SimpleDateFormat( "HH:mm", Locale.getDefault());
            timePickerButton.setText(format.format(departureTime));
        }

        return view;
    }

    private void selectTrain(int trainCode){
        this.trainCode = trainCode;
        // Faccio partire la richiesta
        TrainDepartureStationService tds = new TrainDepartureStationService(new StationDAO(getActivity()));
        tds.getDepartureStations(trainCode, SelectTrainFragment.this);
        // Mostro il progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Recupero i dati del treno...");
        dialog.show();

    }


    /*
    *   CALLBACK PER LA STAZIONE DI PARTENZA
    *   Viene invocata quando la ricerca del codice del treno viene completata con successo.
    *   TrainDepartureStationService.TrainDepartureStationServiceListener
    * */

    @Override
    public void onTrainDepartureStationSuccess(List<Station> stationList) {
        final List<Station> stations = stationList;
        dialog.dismiss();
        if (stations.size() > 1) {
            // Se ci sono più stazioni viene mostrato un dialog che permette all'utente di scegliere
            // quella corretta.
            // Si, nel database di Trenitalia ci sono più treni con lo stesso codice.
            final String[] stationNames = new String[stations.size()];
            for(int i = 0; i < stationNames.length; i++){
                stationNames[i] = stations.get(i).getName();
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            builder.setTitle("Seleziona la stazione di partenza").setItems(stationNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    trainDeparture = stations.get(which);
                    if (mListener != null) {
                        mListener.onTrainSelected(trainCode, trainDeparture);
                    }
                    dismiss();
                }
            });
            builder.show();

        }else {
            trainDeparture = stations.get(0);
            if (mListener != null) {
                mListener.onTrainSelected(trainCode, trainDeparture);
            }
            dismiss();
        }
    }

    @Override
    public void onTrainDepartureStationFailure(Exception exc) {
        dialog.dismiss();
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
        }catch (Exception e) {
            System.err.println(exc.getMessage());
        }
    }


    /*
    *   TravelSolutionsService.TravelSolutionsServiceListener
    * */

    @Override
    @SuppressWarnings("unchecked")
    public void onTravelSolutionsSuccess(List<TravelSolution> solutions) {
        dialog.dismiss();
        trains = new ArrayList<>();
        for (TravelSolution ts: solutions) {
            trains.addAll(ts.getElements());
        }
        // Metodo poco ortodosso per rimuovere eventuali duplicati
        Set<TravelSolution.SolutionElement> s = new LinkedHashSet<>(trains);
        trains = new ArrayList<>(s);

        trainsString = new ArrayList<>();
        for (TravelSolution.SolutionElement se:trains) {
            trainsString.add(se.toString());
        }

        TrainListFragment df = TrainListFragment.newInstance((ArrayList<String>)trainsString);
        df.setOnTrainSelectedListener(new TrainListFragment.OnTrainSelectedListener() {
            @Override
            public void onTrainSelected(int position, String string) {
                selectTrain(trains.get(position).getTrainCode());
            }
        });
        df.show(getFragmentManager().beginTransaction(), "chooseTrain");
    }

    @Override
    public void onTravelSolutionsFailure(Exception exc) {
        dialog.dismiss();
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
        }catch (Exception e) {
            System.err.println(exc.getMessage());
        }
    }

    /*
    * Metodi per la gestione del listener
    * */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTrainSelectedListener) {
            mListener = (OnTrainSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTrainSelectedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    // Callback da chiamare quando viene selezionato correttamente un treno
    public interface OnTrainSelectedListener {
        void onTrainSelected(int trainCode, Station departureStation);
    }
}

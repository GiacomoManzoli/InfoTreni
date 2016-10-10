package com.manzolik.gmanzoli.mytrains;


import android.app.TimePickerDialog;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.manzolik.gmanzoli.mytrains.components.FindTrainFragment;
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

public class SelectTrainFragment extends DialogFragment
        implements FindTrainFragment.OnTrainFoundListener{

    private OnTrainSelectedListener mListener;



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

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FindTrainFragment fragment = FindTrainFragment.newInstance();
        fragment.setOnTrainSelectedListener(this);

        //Replace fragment
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.select_train_fragment_main_frame, fragment);
        ft.commit();

        return view;
    }


    /*
    * FindTrainFragment.OnTrainFoundListener
    * */
    @Override
    public void onTrainFound(int trainCode, Station departureStation) {
        if (mListener != null){
            mListener.onTrainSelected(trainCode, departureStation);
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

package com.manzolik.gmanzoli.mytrains.components;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import java.util.List;

public class FindStationFragment extends DialogFragment {

    private static final String ARG_FRAGMENT_TITLE = "FRAGMENT_TITLE";

    private ListView mResultsList;

    private List<String> mFilteredList;
    private StationDAO stationDAO;

    private String mFragmentTitle;

    private OnStationSelectedListener mListener;

    public FindStationFragment() {
        // Required empty public constructor
    }

    public static FindStationFragment newInstance() {
        return newInstance("");
    }

    public static FindStationFragment newInstance(String title) {
        FindStationFragment fragment = new FindStationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FRAGMENT_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null){
            mFragmentTitle = args.getString(ARG_FRAGMENT_TITLE);
        }

        stationDAO = new StationDAO(getActivity());
        mFilteredList = stationDAO.findStationsNameByName("");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Imposta il titolo del dialog o lo nasconde
        if ( ! mFragmentTitle.equals("")){
            getDialog().setTitle(mFragmentTitle);
        } else {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_find_station, container, false);

        EditText stationInputText = (EditText) view.findViewById(R.id.find_station_text);
        stationInputText.setFocusable(true);
        stationInputText.setFocusableInTouchMode(true);
        stationInputText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(stationInputText, InputMethodManager.SHOW_FORCED);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        stationInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Non fa niente
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Non fa niente
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mFilteredList = stationDAO.findStationsNameByName(editable.toString());
                mResultsList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mFilteredList));
            }
        });

        mResultsList = (ListView) view.findViewById(R.id.find_station_list);
        mResultsList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mFilteredList));
        mResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // i -> posizione dell'item dell'adapter che è stato premuto
                if (mListener != null){
                    String stationName = (String) adapterView.getItemAtPosition(i);
                    Station s = stationDAO.getStationFromName(stationName);
                    mListener.onStationSelected(s);
                    dismiss();
                }
            }
        });
        return view;
    }


    /*
    * Gestione del listener
    * Non viene utilizzato onAttach perché trattadosi di un Dialog risulta più semplice
    * aggiungere il listener in un secondo momento.
    * */

   public void setOnStationSelectedListener(OnStationSelectedListener listener){
       mListener = listener;
   }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnStationSelectedListener {
        void onStationSelected(Station station);
    }
}

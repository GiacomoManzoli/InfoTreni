package com.manzolik.gmanzoli.mytrains;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import java.util.List;

public class FindStationFragment extends DialogFragment {

    private EditText stationInputText;
    private ListView resultsList;

    private List<String> filterdList;
    private StationDAO stationDAO;

    private OnStationSelectedListener mListener;

    public FindStationFragment() {
        // Required empty public constructor
    }


    public static FindStationFragment newInstance() {
        FindStationFragment fragment = new FindStationFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stationDAO = new StationDAO(getActivity());
        filterdList = stationDAO.findStationsNameByName("");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_find_station, container, false);

        stationInputText = (EditText) view.findViewById(R.id.find_station_text);
        stationInputText.setFocusable(true);
        stationInputText.setFocusableInTouchMode(true);
        stationInputText.requestFocus();
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
                filterdList = stationDAO.findStationsNameByName(editable.toString());
                resultsList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, filterdList));
            }
        });

        resultsList = (ListView) view.findViewById(R.id.find_station_list);
        resultsList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, filterdList));
        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

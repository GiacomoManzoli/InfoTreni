package com.manzolik.gmanzoli.mytrains.components;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.manzolik.gmanzoli.mytrains.R;

import java.util.ArrayList;
import java.util.List;


public class TrainListFragment extends DialogFragment {

    private static final String TRAINS_LIST = "trains_list";

    private List<String> trainsString;

    private OnTrainSelectedListener mListener;

    public TrainListFragment() {
        // Required empty public constructor
    }

    public static TrainListFragment newInstance(ArrayList<String> trains) {
        TrainListFragment fragment = new TrainListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(TRAINS_LIST, trains);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trainsString = getArguments().getStringArrayList(TRAINS_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Treni trovati");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_train_list, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.train_list_fragment_list);
        listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, trainsString));

        // Handler per la selezione di un elemento della lista
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null){
                    mListener.onTrainSelected(i, (String)adapterView.getItemAtPosition(i));
                }
                dismiss();
            }
        });
        return view;

    }


    public void setOnTrainSelectedListener(OnTrainSelectedListener listener){
        mListener = listener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnTrainSelectedListener {
        void onTrainSelected(int position, String string);
    }
}

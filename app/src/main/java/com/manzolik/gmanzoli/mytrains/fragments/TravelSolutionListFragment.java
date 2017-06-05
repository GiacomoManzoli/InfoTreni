package com.manzolik.gmanzoli.mytrains.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.adapters.TravelSolutionListAdapter;
import com.manzolik.gmanzoli.mytrains.data.TravelSolution;

import java.util.ArrayList;
import java.util.List;


public class TravelSolutionListFragment extends Fragment implements
        TravelSolutionListAdapter.OnTrainSelectListener {

    private static final String ARG_SOLUTION_LIST = "trains_list";
    private static final String TAG = TravelSolutionListFragment.class.getSimpleName();

    private List<TravelSolution> mSolutions;
    private TrainSelectedListener mListener;

    private RecyclerView mRecyclerView;

    public TravelSolutionListFragment() {
        // Required empty public constructor
    }

    public static TravelSolutionListFragment newInstance(ArrayList<TravelSolution> trains) {
        TravelSolutionListFragment fragment = new TravelSolutionListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SOLUTION_LIST, trains);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //noinspection unchecked
            mSolutions = (List<TravelSolution>) getArguments().getSerializable(ARG_SOLUTION_LIST);
            if (mSolutions == null) {
                if (BuildConfig.DEBUG) Log.e(TAG, "mSolutions è null");
                mSolutions = new ArrayList<>();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_travel_solution_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        TravelSolutionListAdapter adapter = new TravelSolutionListAdapter(mSolutions);
        mRecyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                llm.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnTrainSelectListener(this);

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        TravelSolutionListAdapter adapter = (TravelSolutionListAdapter) mRecyclerView.getAdapter();
        adapter.setOnTrainSelectListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        TravelSolutionListAdapter adapter = (TravelSolutionListAdapter) mRecyclerView.getAdapter();
        adapter.removeOnTrainSelectListener();
    }

    @Override
    public void onTrainSelected(String trainCode) {
        if (mListener != null) {
            mListener.onTrainSelected(trainCode);
        }
    }


    /* Metodi per la gestione del listener */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TrainSelectedListener) {
            mListener = (TrainSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " deve implementare TrainSelectedListener");
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Listener: "+ mListener.getClass().getSimpleName());
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface TrainSelectedListener {
        void onTrainSelected(String trainCode);
    }
}

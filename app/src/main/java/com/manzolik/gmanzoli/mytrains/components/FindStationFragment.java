package com.manzolik.gmanzoli.mytrains.components;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import java.util.List;

public class FindStationFragment extends Fragment
        implements StationDAO.OnFindStationNameAsyncListener,
        TextWatcher,
        AdapterView.OnItemClickListener,
        TextView.OnEditorActionListener {

    private static final String TAG = FindStationFragment.class.getSimpleName();
    private static final String ARG_STATION_TEXT = "arg_station_text";

    private ListView mResultsList;
    private ProgressBar mProgress;
    private TextView mNotFoundView;
    private EditText mStationInputText;

    private StationDAO mStationDAO;

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
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStationDAO = new StationDAO(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_station, container, false);


        mProgress = (ProgressBar) view.findViewById(R.id.find_station_progress_bar);
        mNotFoundView = (TextView) view.findViewById(R.id.find_station_not_found_text);

        mStationInputText = (EditText) view.findViewById(R.id.find_station_edit_text);
        mStationInputText.setFocusable(true);
        mStationInputText.setFocusableInTouchMode(true);
        mStationInputText.requestFocus();
        mStationInputText.setOnEditorActionListener(this);
        mStationInputText.addTextChangedListener(this);
        mStationInputText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mStationInputText.setImeOptions(EditorInfo.IME_ACTION_GO);

        mResultsList = (ListView) view.findViewById(R.id.find_station_list);
        mResultsList.setOnItemClickListener(this);

        /* Restore dello stato precedente*/
        String stationInputText = "";
        if (savedInstanceState != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView - Recupero lo stato precedente");
            stationInputText = savedInstanceState.getString(ARG_STATION_TEXT, "");
            mStationInputText.setText(stationInputText);
        }
        startAsyncLoad(stationInputText);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(ARG_STATION_TEXT, mStationInputText.getText().toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mStationInputText.getWindowToken(), 0);
    }

    private void startAsyncLoad(String partialStationName) {
        mStationDAO.findStationsNameByNameAsync(partialStationName, this);

        mResultsList.setVisibility(View.GONE);
        mNotFoundView.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFindStationName(List<String> names) {
        mProgress.setVisibility(View.GONE);

        if (names.size() > 0) {
            mResultsList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, names));
            mResultsList.setVisibility(View.VISIBLE);
            mNotFoundView.setVisibility(View.GONE);
        } else {
            mResultsList.setVisibility(View.GONE);
            mNotFoundView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Non fa niente
        if (BuildConfig.DEBUG) Log.v(TAG, "beforeTextChanged "+ s.toString());

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (BuildConfig.DEBUG) Log.v(TAG, "onTextChanged "+ s.toString());
        startAsyncLoad(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Non fa niente
        if (BuildConfig.DEBUG) Log.v(TAG, "afterTextChanged "+ s.toString());
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v.getId() == R.id.find_station_edit_text) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onEditorAction ENTER");

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    // In landscape la tastiera compre la lista, quindi piuttosto che
                    // selezionare il primo elemento conviene nascondere la tastiera
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } else {
                    Adapter adapter = mResultsList.getAdapter();

                    if (adapter.getCount() > 0){
                        String name = (String) adapter.getItem(0);
                        Station s = mStationDAO.getStationFromName(name);
                        if (s != null && mListener != null) {
                            mListener.onStationSelected(s);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null){
            String stationName = (String) parent.getItemAtPosition(position);
            Station s = mStationDAO.getStationFromName(stationName);
            mListener.onStationSelected(s);
        }
    }


    /*
    * Metodi per la gestione del listener
    * */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Questo Fragment può essere visualizzato sia dentro un altro Fragment
        // che dentro un'Activity
        // Quindi nella scelta del listener viene prima controllato se è presente un ParentFragment
        // e nel caso viene utilizzato quello, altrimenti viene utilizzata l'Activity.

        // http://stackoverflow.com/questions/39491655/communication-between-nested-fragments-in-android

        Fragment parentFragment = getParentFragment();

        if (parentFragment != null && parentFragment instanceof OnStationSelectedListener) {
            mListener = (OnStationSelectedListener) parentFragment;
        } else if (context instanceof OnStationSelectedListener) {
            mListener = (OnStationSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " deve implementare OnStationSelectedListener");
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Listener: "+ mListener.getClass().getSimpleName());
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

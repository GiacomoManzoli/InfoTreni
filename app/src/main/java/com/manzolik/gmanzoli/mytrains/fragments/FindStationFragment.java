package com.manzolik.gmanzoli.mytrains.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.fragments.main.SettingsFragment;
import com.manzolik.gmanzoli.mytrains.utils.LocationUtils;

import java.util.Calendar;
import java.util.List;

public class FindStationFragment extends Fragment
        implements StationDAO.OnFindStationNameAsyncListener,
        TextWatcher,
        AdapterView.OnItemClickListener,
        TextView.OnEditorActionListener, View.OnClickListener,
        StationDAO.OnFindNearestStationAsyncListener, LocationListener {

    private static final String TAG = FindStationFragment.class.getSimpleName();
    private static final String ARG_STATION_TEXT = "arg_station_text";

    private ListView mAllStationList; // ListView con tutte le stazioni
    private ProgressBar mProgress;
    private TextView mNotFoundView;
    private EditText mStationInputText;
    private Button mGeohintButton;
    private ProgressBar mGeohintProgress;
    private View mFavoritesView;
    private ListView mFavoriteStationList;


    private StationDAO mStationDAO;
    private Station mNearestStation;
    private Location mLastLocation;

    private OnStationSelectedListener mListener;

    public FindStationFragment() {
        // Required empty public constructor
    }


    public static FindStationFragment newInstance() {
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

        // Parte relativa alla lista con tutte le stazioni
        setupAllStationsviews(view, savedInstanceState);

        // Parte relativa alla lista dei preferiti
        setupFavoritesViews(view);

        // Gestione del geohint
        setupGeohintViews(view);

        return view;
    }

    private void setupAllStationsviews(View view, Bundle savedInstanceState) {
        mProgress = (ProgressBar) view.findViewById(R.id.all_station_progress_bar);
        mNotFoundView = (TextView) view.findViewById(R.id.no_station_text);

        mStationInputText = (EditText) view.findViewById(R.id.find_station_edit_text);
        mStationInputText.setFocusable(true);
        mStationInputText.setFocusableInTouchMode(true);
        mStationInputText.requestFocus();
        mStationInputText.setOnEditorActionListener(this);
        mStationInputText.addTextChangedListener(this);
        mStationInputText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mStationInputText.setImeOptions(EditorInfo.IME_ACTION_GO);

        mAllStationList = (ListView) view.findViewById(R.id.all_station_list);
        mAllStationList.setOnItemClickListener(this);
        // La popolazione di questa ^ lista viene fatta in modo asincrono
        /* Restore dello stato precedente*/
        String stationInputText = "";
        if (savedInstanceState != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView - Recupero lo stato precedente");
            stationInputText = savedInstanceState.getString(ARG_STATION_TEXT, "");
            mStationInputText.setText(stationInputText);
        }
        startAsyncLoad(stationInputText);
    }
    private void setupFavoritesViews(View view) {
        mFavoriteStationList = (ListView) view.findViewById(R.id.favorite_station_list);
        mFavoritesView = view.findViewById(R.id.favorites_view);
        mFavoriteStationList.setOnItemClickListener(this);
    }
    private void setupGeohintViews(View view) {
        View geohintView = view.findViewById(R.id.geohint_view);
        mGeohintButton = (Button) view.findViewById(R.id.geohint_button);
        mGeohintButton.setOnClickListener(this);
        mGeohintProgress = (ProgressBar) view.findViewById(R.id.geohint_progress);
        TextView geohintText = (TextView) view.findViewById(R.id.geohint_text);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean geofilteringEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_LOCATION_FILTERING, false);

        if (geofilteringEnabled){
            Location lastLocation = LocationUtils.getLastLocation(getContext());
            if (lastLocation != null) {
                mLastLocation = lastLocation;
                geohintText.setText("Stazione più vicina:");

                if (Calendar.getInstance().getTimeInMillis()
                        - lastLocation.getTime() > 30000) {
                    // L'ultima posizione nota risale a più di 5 minuti fa, mi metto in ascolto
                    // per eventuali aggiornamenti.
                    LocationUtils.requestSingleUpdate(getContext(), this);
                }

                mGeohintButton.setVisibility(View.GONE);
                mGeohintProgress.setVisibility(View.VISIBLE);
                StationDAO stationDAO = new StationDAO(getContext());
                stationDAO.findNearestStationAsync(lastLocation, this);
            } else {
                geohintText.setText("Stazione più vicina non disponibile");
                mGeohintButton.setVisibility(View.GONE);
                mGeohintProgress.setVisibility(View.GONE);
            }
        } else {
            geohintView.setVisibility(View.GONE);
        }
    }

    private void updateFavoritesView() {
        List<String> favorites = mStationDAO.getFavoriteStationNames();
        if (favorites.size() > 0) {
            mFavoritesView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> favoritesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, favorites);
            mFavoriteStationList.setAdapter(favoritesAdapter);
        } else {
            mFavoritesView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Devo ricarcaricare le info sulle stazioni preferite
        updateFavoritesView();
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

    /*
    * Caricamento asincrono di tutte le stazioni
    * */
    private void startAsyncLoad(String partialStationName) {
        mStationDAO.findStationsNameByNameAsync(partialStationName, this);
        mAllStationList.setVisibility(View.GONE);
        mNotFoundView.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
    }

    /*
    * Callback del caricamento asincrono di tutti i nomi
    * */
    @Override
    public void onFindStationName(List<String> names) {
        mProgress.setVisibility(View.GONE);
        if (names.size() > 0) {
            mAllStationList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, names));
            mAllStationList.setVisibility(View.VISIBLE);
            mNotFoundView.setVisibility(View.GONE);
        } else {
            mAllStationList.setVisibility(View.GONE);
            mNotFoundView.setVisibility(View.VISIBLE);
        }
    }

    /*
    * Filtering della lista con tutte le stazioni in base al testo
    * */
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

    /*
    * Pulsante Enter della tastiera
    * */
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
                    Adapter adapter = mAllStationList.getAdapter();

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



    /*
    * Click sulla lista dei nomi delle stazioni (sia preferiti che tutte)
    * */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null){
            String stationName = (String) parent.getItemAtPosition(position);
            Station s = mStationDAO.getStationFromName(stationName);
            mListener.onStationSelected(s);
        }
    }

    /*
    * View.OnClick
    * Selezione del geohint
    * */
    @Override
    public void onClick(View v) {
        if (v.getId() == mGeohintButton.getId() && mNearestStation != null && mListener != null) {
            mListener.onStationSelected(mNearestStation);
        }
    }

    /*
     * Callback per la ricerca asincrona della stazione più vicina
     * */
    @Override
    public void onFindNearestStation(Station station) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Stazione più vicina: " + station.toString());
        mGeohintProgress.setVisibility(View.GONE);
        mGeohintButton.setVisibility(View.VISIBLE);
        mGeohintButton.setText(station.getName());
        mNearestStation = station;
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


    /*
    * LocationListener
    * */

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && location != mLastLocation) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Nuova posizione: " + location.toString());
            StationDAO stationDAO = new StationDAO(getContext());
            stationDAO.findNearestStationAsync(location, this);
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Nuova location null o non cambiata");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Non fa niente
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Non fa niente
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Non fa niente
    }


    public interface OnStationSelectedListener {
        void onStationSelected(Station station);
    }
}

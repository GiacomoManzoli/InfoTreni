package com.manzolik.gmanzoli.mytrains;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.components.TrainStatusListAdapter;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.notifications.SchedulingAlarmReceiver;
import com.manzolik.gmanzoli.mytrains.service.TrainReminderStatusService;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment che visualiizza una lista di TrainStatus
 * */



public class TrainRemindersStatusFragment extends Fragment
        implements TrainReminderStatusService.TrainReminderStatusServiceListener,
        TrainStatusListAdapter.OnStatusSelectListener{

    private static final String TAG = TrainRemindersStatusFragment.class.getSimpleName();
    private RecyclerView mTrainStatusListView;
    private TextView mLastUpdateTimeTextView;
    private TextView mTrainFoundTextView;

    private ProgressDialog mDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;



    public TrainRemindersStatusFragment() {
        // Required empty public constructor
    }

    public static TrainRemindersStatusFragment newInstance() {
        TrainRemindersStatusFragment fragment = new TrainRemindersStatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inserimento dei dati di debug
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        if(!prefs.getBoolean("debugData", false)) {

            // DEBUG DATA

            StationDAO stationDAO = new StationDAO(this.getActivity());
            Station bolognaCentrale = stationDAO.getStationFromCode("S05043");
            Station veneziaSL = stationDAO.getStationFromCode("S02593");
            Station rovigo = stationDAO.getStationFromCode("S05706");
            Station padova = stationDAO.getStationFromCode("S02581");
            Station romaTib = stationDAO.getStationFromCode("S08217");

            TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this.getActivity());
            // Venezia --> Bologna
            trainReminderDAO.insertReminder("2233", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 11, 40), new GregorianCalendar(2016, 3, 1, 12, 20), padova.getId());
            trainReminderDAO.insertReminder("2235", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 12, 40), new GregorianCalendar(2016, 3, 1, 13, 20), padova.getId());
            trainReminderDAO.insertReminder("2237", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 13, 40), new GregorianCalendar(2016, 3, 1, 14, 20), padova.getId());
            trainReminderDAO.insertReminder("2239", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 14, 40), new GregorianCalendar(2016, 3, 1, 15, 20), padova.getId());
            trainReminderDAO.insertReminder("2241", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 15, 40), new GregorianCalendar(2016, 3, 1, 16, 20), padova.getId());
            trainReminderDAO.insertReminder("2243", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 16, 40), new GregorianCalendar(2016, 3, 1, 17, 20), padova.getId());
            trainReminderDAO.insertReminder("2245", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 17, 40), new GregorianCalendar(2016, 3, 1, 18, 20), padova.getId());

            // Bologna --> Venezia
            trainReminderDAO.insertReminder("2222", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 6, 20), new GregorianCalendar(2016, 3, 1, 7, 15), rovigo.getId());
            trainReminderDAO.insertReminder("2224", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 7, 20), new GregorianCalendar(2016, 3, 1, 8, 15), rovigo.getId());
            trainReminderDAO.insertReminder("2226", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 8, 20), new GregorianCalendar(2016, 3, 1, 9, 15), rovigo.getId());
            trainReminderDAO.insertReminder("2228", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 9, 20), new GregorianCalendar(2016, 3, 1, 10, 15), rovigo.getId());
            trainReminderDAO.insertReminder("2230", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 10, 20), new GregorianCalendar(2016, 3, 1, 11, 15), rovigo.getId());
            trainReminderDAO.insertReminder("2232", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 11, 20), new GregorianCalendar(2016, 3, 1, 12, 15), rovigo.getId());
            trainReminderDAO.insertReminder("2234", bolognaCentrale.getId(), new GregorianCalendar(2016, 3, 1, 13, 20), new GregorianCalendar(2016, 3, 1, 14, 15), rovigo.getId());


            // Altro
            trainReminderDAO.insertReminder("2233", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 0, 0), new GregorianCalendar(2016, 3, 1, 1, 0), padova.getId());
            trainReminderDAO.insertReminder("2233", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 0, 0), new GregorianCalendar(2016, 3, 1, 1, 0), padova.getId());
            trainReminderDAO.insertReminder("9455", veneziaSL.getId(), new GregorianCalendar(2016, 3, 1, 21, 10), new GregorianCalendar(2016, 3, 1, 1, 0), romaTib.getId());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("debugData", true);
            editor.apply();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_train_reminder_status, container, false);


        mTrainFoundTextView = (TextView) view.findViewById(R.id.train_status_activity_train_count);
        mLastUpdateTimeTextView = (TextView) view.findViewById(R.id.train_status_activity_last_update);
        mTrainStatusListView = (RecyclerView) view.findViewById(R.id.train_status_activity_train_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.train_status_activity_refresh);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TrainRemindersStatusFragment.this.loadData();
            }
        });

        mTrainStatusListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mTrainStatusListView.setLayoutManager(llm);

        TrainStatusListAdapter adapter = new TrainStatusListAdapter(new ArrayList<TrainStatus>());
        mTrainStatusListView.setAdapter(adapter);


        FloatingActionButton addFAB = (FloatingActionButton) view.findViewById(R.id.train_status_activity_add);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainRemindersStatusFragment.this.getActivity(), AddReminderActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    @Override
    public void onResume(){
        super.onResume();

        // Riabilita il listener
        TrainStatusListAdapter adapter = (TrainStatusListAdapter) mTrainStatusListView.getAdapter();
        adapter.setOnStatusSelectListener(this);

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Caricamento dei dati in corso...");
        mDialog.setProgressStyle(R.style.ProgressTheme);
        mDialog.show();


        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDialog.dismiss();
        ((TrainStatusListAdapter)mTrainStatusListView.getAdapter()).removeOnStatusSelectListener();
    }

    protected void loadData(){
        TrainReminderStatusService trenitaliaService = new TrainReminderStatusService();
        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this.getActivity());
        List<TrainReminder> rem = trainReminderDAO.getAllReminders();
        trenitaliaService.getTrainStatusList(rem,this);
    }


    @Override
    public void onStatusSelected(TrainStatus status) {
        Intent i = new Intent(getContext(), TrainStatusActivity.class);
        TrainDAO trainDAO = new TrainDAO(getContext());
        Train t = trainDAO.getTrainFromCode(status.getTrainCode(), status.getDepartureStationCode());
        i.putExtra(TrainStatusActivity.INTENT_TRAIN, t);
        startActivity(i);
    }

    /*
    * TrainReminderStatusService.TrainReminderStatusServiceListener
    * */
    @Override
    public void onTrainReminderStatusServiceSuccess(List<TrainStatus> trainStatuses) {
        //System.out.println(trains.size());
        mDialog.hide();
        mSwipeRefreshLayout.setRefreshing(false);
        //System.out.println(trains.toString());

        ((TrainStatusListAdapter )mTrainStatusListView.getAdapter()).setItems(trainStatuses);

        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());
        mLastUpdateTimeTextView.setText(String.format("Ultimo aggiornamento: %s", format.format(now.getTime())));

        mTrainFoundTextView.setText(String.format(Locale.getDefault(), "Treni monitorati: %d", trainStatuses.size()));
    }

    @Override
    public void onTrainReminderStatusServiceFailure(Exception exc) {
        mDialog.dismiss();
        mSwipeRefreshLayout.setRefreshing(false);
        try {
            throw exc;
        } catch (UnknownHostException e) {
            // No internet connection
            Intent i = new Intent(getContext(), NoConnectivityActivity.class);
            startActivity(i);
            getActivity().finish();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
        }

    }
}

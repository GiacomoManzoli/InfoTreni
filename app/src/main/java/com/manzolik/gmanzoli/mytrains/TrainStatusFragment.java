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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusService;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusServiceCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;



/*
* Nel caso si verifichi la situazione in cui questo frammento debba comunicare qualcosa all'attività
* che lo contiene e necessario implementare un listener:
*
* https://developer.android.com/training/basics/fragments/communicating.html
* http://stackoverflow.com/questions/24777985/how-to-implement-onfragmentinteractionlistener
* */


public class TrainStatusFragment extends Fragment implements TrainStatusServiceCallback {


    //private OnFragmentInteractionListener mListener;

    private RecyclerView trainStatusListView;
    private TextView lastUpdateTimeTextView;
    private TextView trainFoundTextView;

    private ProgressDialog dialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SchedulingAlarmReceiver receiver;

    public TrainStatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TrainStatusFragment.
     */

    public static TrainStatusFragment newInstance() {
        TrainStatusFragment fragment = new TrainStatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        receiver = new SchedulingAlarmReceiver();

        // Inserimento dei dati di debug
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        if(!prefs.getBoolean("firstTime", false)) {

            // DEBUG DATA

            StationDAO stationDAO = new StationDAO(this.getActivity());
            Station bolognaCentrale = stationDAO.getStationFromCode("S05043");
            Station veneziaSL = stationDAO.getStationFromCode("S02593");
            Station rovigo = stationDAO.getStationFromCode("S05706");
            Station padova = stationDAO.getStationFromCode("S02581");
            Station romaTib = stationDAO.getStationFromCode("S08217");

            TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this.getActivity());
            // Venezia --> Bologna
            trainReminderDAO.insertReminder(2233, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 11, 40), new GregorianCalendar(2016, 3, 1, 12, 20), padova.getID());
            trainReminderDAO.insertReminder(2235, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 12, 40), new GregorianCalendar(2016, 3, 1, 13, 20), padova.getID());
            trainReminderDAO.insertReminder(2237, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 13, 40), new GregorianCalendar(2016, 3, 1, 14, 20), padova.getID());
            trainReminderDAO.insertReminder(2239, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 14, 40), new GregorianCalendar(2016, 3, 1, 15, 20), padova.getID());
            trainReminderDAO.insertReminder(2241, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 15, 40), new GregorianCalendar(2016, 3, 1, 16, 20), padova.getID());
            trainReminderDAO.insertReminder(2243, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 16, 40), new GregorianCalendar(2016, 3, 1, 17, 20), padova.getID());
            trainReminderDAO.insertReminder(2245, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 17, 40), new GregorianCalendar(2016, 3, 1, 18, 20), padova.getID());

            // Bologna --> Venezia
            trainReminderDAO.insertReminder(2222, bolognaCentrale.getID(), new GregorianCalendar(2016, 3, 1, 6, 20), new GregorianCalendar(2016, 3, 1, 7, 15), rovigo.getID());
            trainReminderDAO.insertReminder(2224, bolognaCentrale.getID(), new GregorianCalendar(2016, 3, 1, 7, 20), new GregorianCalendar(2016, 3, 1, 8, 15), rovigo.getID());
            trainReminderDAO.insertReminder(2226, bolognaCentrale.getID(), new GregorianCalendar(2016, 3, 1, 8, 20), new GregorianCalendar(2016, 3, 1, 9, 15), rovigo.getID());
            trainReminderDAO.insertReminder(2228, bolognaCentrale.getID(), new GregorianCalendar(2016, 3, 1, 9, 20), new GregorianCalendar(2016, 3, 1, 10, 15), rovigo.getID());
            trainReminderDAO.insertReminder(2230, bolognaCentrale.getID(), new GregorianCalendar(2016, 3, 1, 10, 20), new GregorianCalendar(2016, 3, 1, 11, 15), rovigo.getID());
            trainReminderDAO.insertReminder(2232, bolognaCentrale.getID(), new GregorianCalendar(2016, 3, 1, 11, 20), new GregorianCalendar(2016, 3, 1, 12, 15), rovigo.getID());

            // Altro
            trainReminderDAO.insertReminder(2233, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 0, 0), new GregorianCalendar(2016, 3, 1, 1, 0), padova.getID());
            trainReminderDAO.insertReminder(2233, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 0, 0), new GregorianCalendar(2016, 3, 1, 1, 0), padova.getID());
            trainReminderDAO.insertReminder(9455, veneziaSL.getID(), new GregorianCalendar(2016, 3, 1, 21, 10), new GregorianCalendar(2016, 3, 1, 1, 0), romaTib.getID());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_train_status, container, false);


        trainFoundTextView = (TextView) view.findViewById(R.id.train_status_activity_train_count);
        lastUpdateTimeTextView = (TextView) view.findViewById(R.id.train_status_activity_last_update);
        trainStatusListView = (RecyclerView) view.findViewById(R.id.train_status_activity_train_list);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.train_status_activity_refresh);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TrainStatusFragment.this.loadData();
            }
        });

        trainStatusListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        trainStatusListView.setLayoutManager(llm);

        TrainStatusListAdapter adapter = new TrainStatusListAdapter(new ArrayList<TrainStatus>());
        trainStatusListView.setAdapter(adapter);

        FloatingActionButton addFAB = (FloatingActionButton) view.findViewById(R.id.train_status_activity_add);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainStatusFragment.this.getActivity(), AddReminderActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }




    @Override
    public void onResume(){
        super.onResume();

        NotificationManager notificationManager = (NotificationManager)this.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        // Disattiva le notifiche finché il frammento è visibile
        receiver.disableNotifications(this.getActivity());

        dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Caricamento dei dati in corso...");
        dialog.setProgressStyle(R.style.ProgressTheme);
        dialog.show();
        loadData();

    }

    @Override
    public void onStop() {
        super.onStop();
        // attiva le notifiche se queste sono abilitate
        receiver.enableNotifications(this.getActivity());

    }

    protected void loadData(){
        TrainStatusService trenitaliaService = new TrainStatusService();
        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(this.getActivity());
        List<TrainReminder> rem = trainReminderDAO.getAllReminders();
        trenitaliaService.getTrainStatusList(this, rem);
    }

    public void trainStatusServiceCallbackSuccess(List<TrainStatus> trains) {
        //System.out.println(trains.size());
        dialog.hide();
        swipeRefreshLayout.setRefreshing(false);
        //System.out.println(trains.toString());

        ((TrainStatusListAdapter )trainStatusListView.getAdapter()).setItems(trains);

        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());
        lastUpdateTimeTextView.setText(String.format("Ultimo aggiornamento: %s", format.format(now.getTime())));

        trainFoundTextView.setText(String.format("Treni monitorati: %d", trains.size()));
    }


    public void trainStatusServiceCallbackFailure(Exception exc) {
        // Do Stuff
        dialog.hide();
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this.getActivity(), exc.getMessage(), Toast.LENGTH_LONG).show();
    }
}

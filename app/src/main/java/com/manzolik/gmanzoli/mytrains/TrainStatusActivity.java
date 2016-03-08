package com.manzolik.gmanzoli.mytrains;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.service.TrenitaliaService;
import com.manzolik.gmanzoli.mytrains.service.TrenitaliaServiceCallback;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class TrainStatusActivity extends AppCompatActivity implements TrenitaliaServiceCallback {

    private RecyclerView trainStatusListView;
    private TextView lastUpdateTimeTextView;
    private TextView trainFoundTextView;

    private ProgressDialog dialog;
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_status);

        trainFoundTextView = (TextView) findViewById(R.id.train_status_activity_train_count);
        lastUpdateTimeTextView = (TextView) findViewById(R.id.train_status_activity_last_update);
        trainStatusListView = (RecyclerView) findViewById(R.id.train_status_activity_train_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.train_status_activity_refresh);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TrainStatusActivity.this.loadData();
            }
        });

        trainStatusListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        trainStatusListView.setLayoutManager(llm);

        TrainStatusListAdapter adapter = new TrainStatusListAdapter(new ArrayList<TrainStatus>());
        trainStatusListView.setAdapter(adapter);

        FloatingActionButton addFAB = (FloatingActionButton) findViewById(R.id.train_status_activity_add);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainStatusActivity.this, AddReminderActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
        loadData();

    }

    protected void loadData(){
        TrenitaliaService trenitaliaService = new TrenitaliaService();
        trenitaliaService.getTrainStatusList(this, TrainReminder.getDebugList());
        //TODO: recupeare i reminder memorizzati
        //TODO: spostare la chiamata da onCreate perché viene effettuata anche quando viene ruotato il telefono
    }

    public void serviceSuccess(List<TrainStatus> trains) {
        // Do stuff
        System.out.println(trains.size());
        dialog.hide();
        swipeRefreshLayout.setRefreshing(false);
        System.out.println(trains.toString());

        ((TrainStatusListAdapter )trainStatusListView.getAdapter()).setItems(trains);

        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());
        lastUpdateTimeTextView.setText(String.format("Ultimo aggiornamento: %s", format.format(now.getTime())));

        trainFoundTextView.setText(String.format("Treni monitorati: %d", trains.size()));

    }


    public void serviceFailure(Exception exc) {
        // Do Stuff
        dialog.hide();
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, exc.getMessage(), Toast.LENGTH_LONG).show();
    }
}

package com.manzolik.gmanzoli.mytrains;

import android.app.ActionBar;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;

import java.util.List;

public class ManageReminderActivity extends AppCompatActivity {

    private RecyclerView reminderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_reminder);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        reminderList = (RecyclerView) findViewById(R.id.manage_reminder_list);
        final FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.manage_reminder_add);



        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        reminderList.setLayoutManager(llm);
        setReminderListAdapter();
        reminderList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && addButton.isShown())
                    addButton.hide();
                else if (dy < 0 && !addButton.isShown())
                    addButton.show();
            }
        });

        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageReminderActivity.this, AddReminderActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setReminderListAdapter();
    }

    private void setReminderListAdapter(){
        final TrainReminderDAO reminderDAO = new TrainReminderDAO(this);
        final List<TrainReminder> reminders = reminderDAO.getAllReminders();
        System.out.printf("REMINDER PRESENTI: %d%n", reminders.size());
        TrainReminderListAdapter adapter = new TrainReminderListAdapter(reminders, new TrainReminderListAdapter.OnDeleteListener() {
            @Override
            public void onDelete(TrainReminderListAdapter adapter, TrainReminder reminder, int position) {
                reminderDAO.deleteReminder(reminder);
                reminders.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });
        reminderList.setAdapter(adapter);
    }

    /*
    * GESTIONE DEL MENU
    * */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (item.getItemId() == android.R.id.home){
            // Codice per usare il tasto indietro fisico
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

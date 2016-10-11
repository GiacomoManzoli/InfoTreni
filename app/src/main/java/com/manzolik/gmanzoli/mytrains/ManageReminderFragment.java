package com.manzolik.gmanzoli.mytrains;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.manzolik.gmanzoli.mytrains.components.TrainReminderListAdapter;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class ManageReminderFragment extends Fragment
    implements SearchView.OnQueryTextListener {

    private RecyclerView reminderList;
    TrainReminderListAdapter adapter;

    public ManageReminderFragment() {
        // Required empty public constructor
    }


    public static ManageReminderFragment newInstance() {
        ManageReminderFragment fragment = new ManageReminderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage_reminder, container, false);

        reminderList = (RecyclerView) view.findViewById(R.id.manage_reminder_list);
        final FloatingActionButton addButton = (FloatingActionButton) view.findViewById(R.id.manage_reminder_add);



        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        reminderList.setLayoutManager(llm);
        // Crea l'adapter e lo imposta come adapter di reminderList
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageReminderFragment.this.getActivity(), AddReminderActivity.class);
                startActivity(intent);
            }
        });

        return  view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setReminderListAdapter();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        // http://stackoverflow.com/questions/30847096/android-getmenuinflater-in-a-fragment-subclass-cannot-resolve-method
        inflater.inflate(R.menu.menu_mangage_reminder, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_manage_reminder_action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement the filter logic
        System.out.println(query);
        adapter.getFilter().filter(query);
        adapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private void setReminderListAdapter(){
        final TrainReminderDAO reminderDAO = new TrainReminderDAO(this.getActivity());
        final List<TrainReminder> reminders = reminderDAO.getAllReminders();
        System.out.printf("REMINDER PRESENTI: %d%n", reminders.size());
        adapter = new TrainReminderListAdapter(reminders, new TrainReminderListAdapter.OnDeleteListener() {
            @Override
            public void onDelete(final TrainReminderListAdapter adapter, final TrainReminder reminder, final int position) {
                // Handler per la scelta del pop up
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                // L'utente ha confermato di cancellare il reminder
                                reminderDAO.deleteReminder(reminder);
                                adapter.deleteItemAtPosition(position);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());

                AlertDialog.Builder builder = new AlertDialog.Builder(ManageReminderFragment.this.getActivity());
                builder.setMessage(String.format("Sei sicuro di voler cancellare l'avviso per il treno \"%s\" dalle %s alle %s?", reminder.toString(), format.format(reminder.getStartTime().getTime()), format.format(reminder.getEndTime().getTime())))
                        .setPositiveButton("Si", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();

            }
        });
        reminderList.setAdapter(adapter);
    }



}

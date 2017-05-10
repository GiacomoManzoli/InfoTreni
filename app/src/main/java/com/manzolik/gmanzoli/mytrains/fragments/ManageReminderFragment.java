package com.manzolik.gmanzoli.mytrains.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.manzolik.gmanzoli.mytrains.AddReminderActivity;
import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.EditReminderActivity;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.adapters.TrainReminderListAdapter;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class ManageReminderFragment extends Fragment
    implements SearchView.OnQueryTextListener,
        TrainReminderListAdapter.OnContextMenuItemClick,
        View.OnClickListener {

    private static final String TAG = ManageReminderFragment.class.getSimpleName();

    private RecyclerView mReminderList;
    private TrainReminderListAdapter mAdapter;
    private AlertDialog mDialog;

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
        View view =  inflater.inflate(R.layout.fragment_manage_reminder, container, false);

        mReminderList = (RecyclerView) view.findViewById(R.id.manage_reminder_list);
        final FloatingActionButton addButton = (FloatingActionButton) view.findViewById(R.id.manage_reminder_add);
        addButton.setOnClickListener(this);

        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mReminderList.setLayoutManager(llm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mReminderList.getContext(),
                llm.getOrientation());
        mReminderList.addItemDecoration(dividerItemDecoration);
        mReminderList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && addButton.isShown())
                    addButton.hide();
                else if (dy < 0 && !addButton.isShown())
                    addButton.show();
            }
        });


        return  view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setReminderListAdapter();
    }

    /*
    * onPause: se sono configurati e aperti dismette i vari dialog per evitare memory leak
    * */
    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.removeOnContextMenuItemClickListener();
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /*
    * onClick : handler per il click sul pulsante `Add`
    * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manage_reminder_add:
                Intent intent = new Intent(getActivity(), AddReminderActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*
    * INIZIO : Gestione della ricerca nella Toolbar
    * */
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
        if (BuildConfig.DEBUG) Log.d(TAG, "Search query: "+query);
        mAdapter.getFilter().filter(query);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private void setReminderListAdapter(){
        final TrainReminderDAO reminderDAO = new TrainReminderDAO(this.getActivity());
        final List<TrainReminder> reminders = reminderDAO.getAllReminders();

        if (BuildConfig.DEBUG) Log.d(TAG, String.format("Reminder presenti: %d%n", reminders.size()));
        mAdapter = new TrainReminderListAdapter(reminders,getContext());
        mAdapter.setOnContextMenuItemClickListener(this);
        mReminderList.setAdapter(mAdapter);
    }
    /*
    * FINE : Gestione della ricerca nella Toolbar
    * */


    /*
    * INIZIO: Gestione del menù contestuale
    * */

    @Override
    public void onContextMenuItemClick(int itemId, TrainReminder reminder, int reminderPosition) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onContextMenuItemClick - "+reminder.toString()+ String.format(" %d", reminderPosition));
        switch (itemId) {
            case R.id.ctx_delete_reminder:
                handleContextMenuDelete(reminder, reminderPosition);
                break;
            case R.id.ctx_edit_reminder:
                handleContextMenuEdit(reminder, reminderPosition);
                break;
        }
    }

    private void handleContextMenuEdit(final TrainReminder reminder, final int position) {
        Intent i = new Intent(getContext(), EditReminderActivity.class);
        i.putExtra(EditReminderActivity.INTENT_REMINDER, reminder);
        startActivity(i);
    }

    private void handleContextMenuDelete(final TrainReminder reminder, final int position) {
        final TrainReminderDAO reminderDAO = new TrainReminderDAO(this.getActivity());

        // Handler per la scelta del pop up
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // L'utente ha confermato di cancellare il reminder
                        reminderDAO.deleteReminder(reminder);
                        mAdapter.deleteItemAtPosition(position);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());

        AlertDialog.Builder builder = new AlertDialog.Builder(ManageReminderFragment.this.getActivity());
        mDialog = builder.setMessage(String.format("Sei sicuro di voler cancellare l'avviso per il treno \"%s\" dalle %s alle %s?", reminder.toString(), format.format(reminder.getStartTime().getTime()), format.format(reminder.getEndTime().getTime())))
                .setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    /*
    * FINE : Gestione del menu contestuale
    * */

}

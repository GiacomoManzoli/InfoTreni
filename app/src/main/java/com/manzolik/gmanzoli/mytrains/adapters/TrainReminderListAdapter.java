package com.manzolik.gmanzoli.mytrains.adapters;


import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrainReminderListAdapter extends RecyclerView.Adapter<TrainReminderListAdapter.TrainReminderItemHolder>
    implements Filterable {

    private static final String TAG = TrainReminderListAdapter.class.getSimpleName();
    private List<TrainReminder> mReminderList;
    private List<TrainReminder> mOriginalReminderList;

    private OnContextMenuItemClick mListener;
    private Context mContext;
    private PopupMenu mPopupMenu;

    public TrainReminderListAdapter(List<TrainReminder> reminderList, Context context) {
        this.mOriginalReminderList = reminderList;
        this.mReminderList = new ArrayList<>();
        this.mReminderList.addAll(mOriginalReminderList);
        this.mContext = context;
    }

    @Override
    public TrainReminderItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_adapter_row_train_reminder, parent, false);

        return new TrainReminderItemHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mReminderList.size();
    }

    @Override
    public void onBindViewHolder(final TrainReminderItemHolder holder, int position) {
        final TrainReminder reminder = mReminderList.get(position);

        holder.trainDescription.setText(String.format("%s - %s", reminder.getTrain().getCode(), reminder.getTrain().getDepartureStation().getName()));
        holder.trainTarget.setText(reminder.getTargetStation().getName());
        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());
        holder.trainTime.setText(String.format("Dalle %s alle %s", format.format(reminder.getStartTime().getTime()), format.format(reminder.getEndTime().getTime())));

        holder.trainActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick - " + reminder.toString());
                /* Non posso usare la variabile position che ricevo come paramentro
                * perché se ho già eliminato degli elementi la posizione effettiva della view
                * all'interno della RecycleView può essere diversa, portando ad un animazione inconsistente */

                final int pos = holder.getAdapterPosition();

                mPopupMenu = new PopupMenu(mContext, holder.trainActions);
                mPopupMenu.inflate(R.menu.menu_ctx_manage_reminder);
                mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "onMenuItemClick - "+reminder.toString()+ String.format(" %d", pos));
                        if (mListener != null) {
                            mListener.onContextMenuItemClick(item.getItemId(), reminder, pos);
                        }
                        return true;
                    }
                });
                mPopupMenu.show();
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new TrainReminderFilter(this, mOriginalReminderList);
    }

    public void deleteItemAtPosition(int adapterPosition) {
        TrainReminder reminder = mReminderList.get(adapterPosition);
        mOriginalReminderList.remove(reminder);  // Rimuove l'elemento dalla lista completa
        mReminderList.remove(adapterPosition); // Rimuove l'elemento dal datasource della lista visualizzata
        notifyItemRemoved(adapterPosition);
    }

    /* ContextMenuInterface*/

    public interface OnContextMenuItemClick {
        void onContextMenuItemClick(int itemId, TrainReminder reminder, int reminderPosition);
    }

    public void setOnContextMenuItemClickListener(OnContextMenuItemClick listener) {
        this.mListener = listener;
    }

    public void removeOnContextMenuItemClickListener() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        this.mListener = null;
    }


    /* ViewHolder pattern */
    static class TrainReminderItemHolder extends RecyclerView.ViewHolder{
        final TextView trainDescription;
        final TextView trainTarget;
        final TextView trainTime;
        final ImageButton trainActions;


        TrainReminderItemHolder(View v) {
            super(v);
            this.trainDescription = (TextView) v.findViewById(R.id.train_reminder_adapter_train_description);
            this.trainTarget = (TextView) v.findViewById(R.id.train_reminder_adapter_train_target);
            this.trainTime = (TextView) v.findViewById(R.id.train_reminder_adapter_train_time);
            this.trainActions = (ImageButton) v.findViewById(R.id.train_reminder_adapter_train_actions);
        }
    }

    /* Filtro per la lista */
    private static class TrainReminderFilter extends Filter {

        private final TrainReminderListAdapter mAdapter;
        private final List<TrainReminder> mOriginalList;
        private final List<TrainReminder> mFilteredList;

        TrainReminderFilter(TrainReminderListAdapter adapter, List<TrainReminder> originalList) {
            super();
            this.mAdapter = adapter;
            this.mOriginalList = originalList;
            this.mFilteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence query) {
            mFilteredList.clear();
            final FilterResults results = new FilterResults();

            if (query.length() == 0) {
                mFilteredList.addAll(mOriginalList);
            } else {
                final String filterPattern = query.toString().toLowerCase().trim();

                for (final TrainReminder reminder : mOriginalList) {
                    // reminer.toString = codiceTreno - stazioneDiPartenzaDelTreno
                    if (reminder.toString().contains(filterPattern)) {
                        mFilteredList.add(reminder);
                    }
                }
            }
            results.values = mFilteredList;
            results.count = mFilteredList.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mAdapter.mReminderList.clear();
            mAdapter.mReminderList.addAll((ArrayList<TrainReminder>) filterResults.values);
            mAdapter.notifyDataSetChanged();
        }

    }
}

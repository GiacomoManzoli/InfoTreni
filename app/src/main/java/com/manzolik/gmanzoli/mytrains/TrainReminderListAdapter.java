package com.manzolik.gmanzoli.mytrains;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.data.TrainReminder;

import java.text.SimpleDateFormat;
import java.util.List;

public class TrainReminderListAdapter extends RecyclerView.Adapter<TrainReminderListAdapter.TrainReminderItemHolder> {

    private List<TrainReminder> reminderList;
    private OnDeleteListener onDeleteListener;

    public TrainReminderListAdapter(List<TrainReminder> reminderList, OnDeleteListener onDeleteListener) {
        this.reminderList = reminderList;
        this.onDeleteListener = onDeleteListener;
    }

    @Override
    public TrainReminderItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_adapter_row_train_reminder, parent, false);

        return new TrainReminderItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrainReminderItemHolder holder, final int position) {
        final TrainReminder reminder = reminderList.get(position);

        holder.trainDescription.setText(String.format("%d - %s", reminder.getTrain().getCode(), reminder.getTrain().getDepartureStation().getName()));
        holder.trainTarget.setText("Stazione target: " + reminder.getTargetStaion().getName());
        SimpleDateFormat format = new SimpleDateFormat("H:mm");
        holder.trainTime.setText(String.format("Dalle: %s alle: %s", format.format(reminder.getStartTime().getTime()), format.format(reminder.getEndTime().getTime())));

        holder.trainDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteListener.onDelete(TrainReminderListAdapter.this, reminder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }


    public interface OnDeleteListener{
        void onDelete(TrainReminderListAdapter adapter, TrainReminder reminder, int position);
    }

    public static class TrainReminderItemHolder extends RecyclerView.ViewHolder{
        protected final TextView trainDescription;
        protected final TextView trainTarget;
        protected final TextView trainTime;
        protected final ImageButton trainDelete;


        public TrainReminderItemHolder(View v) {
            super(v);
            this.trainDescription = (TextView) v.findViewById(R.id.train_reminder_adapter_train_description);
            this.trainTarget = (TextView) v.findViewById(R.id.train_reminder_adapter_train_target);
            this.trainTime = (TextView) v.findViewById(R.id.train_reminder_adapter_train_time);
            this.trainDelete = (ImageButton) v.findViewById(R.id.train_reminder_adapter_train_delete);
        }
    }
}

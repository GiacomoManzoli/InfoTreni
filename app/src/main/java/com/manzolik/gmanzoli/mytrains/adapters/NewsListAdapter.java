package com.manzolik.gmanzoli.mytrains.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.News;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NewsListAdapter
        extends RecyclerView.Adapter<NewsListAdapter.NewsItemHolder>{

    private static final String TAG = NewsListAdapter.class.getSimpleName();

    private List<News> mList;


    public NewsListAdapter(List<News> list) {
        this.mList = list;
    }

    @Override
    public NewsItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_adapter_row_news, parent, false);
        return new NewsItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewsItemHolder holder, int position) {
        final News news = mList.get(position);

        holder.titleText.setText(news.getTitle());
        holder.messageText.setText(news.getText());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        holder.dateText.setText(dateFormat.format(news.getDate()));

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setItems(List<News> items){
        this.mList = items;
        this.notifyDataSetChanged();
    }

    static class NewsItemHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView dateText;
        final TextView messageText;

        NewsItemHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            dateText = (TextView) itemView.findViewById(R.id.date_text);
            messageText = (TextView) itemView.findViewById(R.id.content_text);
        }
    }
}

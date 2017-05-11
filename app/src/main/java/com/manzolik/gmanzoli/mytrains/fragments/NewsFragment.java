package com.manzolik.gmanzoli.mytrains.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.adapters.NewsListAdapter;
import com.manzolik.gmanzoli.mytrains.data.News;
import com.manzolik.gmanzoli.mytrains.adapters.StationInfoListAdapter;
import com.manzolik.gmanzoli.mytrains.data.http.NewsService;

import java.util.ArrayList;
import java.util.List;


public class NewsFragment
        extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, NewsService.NewsServiceListener {

    private static final String ARG_NEWS = "news";

    private static final String TAG = NewsFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mNoElementText;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ProgressDialog mDialog;

    private ArrayList<News> mList;


    public NewsFragment() {
        // Required empty public constructor
    }


    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        if (savedInstanceState != null ) {
            //noinspection unchecked
            mList = (ArrayList<News>) savedInstanceState.getSerializable(ARG_NEWS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mNoElementText = (TextView) view.findViewById(R.id.recycler_view_fragment_empty_text);
        mNoElementText.setText(R.string.no_news);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.recycler_view_fragment_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_fragment_recycler);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mRecyclerView.setAdapter(new NewsListAdapter(new ArrayList<News>()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                llm.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mNoElementText.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);

        NewsService newsService = new NewsService();
        newsService.getNews(this);

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("Caricamento dei dati in corso");
        mDialog.show();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        NewsListAdapter adapter = (NewsListAdapter) mRecyclerView.getAdapter();
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume - elementi: "+String.valueOf(adapter.getItemCount()));
        if (adapter.getItemCount() > 0) {
            mNoElementText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mNoElementText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(ARG_NEWS, mList);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onRefresh() {
        NewsService service = new NewsService();
        service.getNews(this);
    }

    @Override
    public void onNewsServiceSuccess(List<News> result) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mDialog != null) mDialog.dismiss();
        NewsListAdapter adapter = (NewsListAdapter) mRecyclerView.getAdapter();
        adapter.setItems(result);
        if (BuildConfig.DEBUG) Log.d(TAG, "onStationStatusResult - elementi: "+String.valueOf(adapter.getItemCount()));

        if (adapter.getItemCount() > 0) {
            mNoElementText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Imposto la textView visibile");
            mNoElementText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNewsServiceFailure(Exception exc) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (BuildConfig.DEBUG) Log.e(TAG, exc.getMessage());
    }
}

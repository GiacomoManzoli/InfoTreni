package com.manzolik.gmanzoli.mytrains.data.http;


import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.News;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NewsService implements HttpGetTask.HttpGetTaskListener {
    private static final String TAG = TravelSolutionsService.class.getSimpleName();
    private static final String ENDPOINT_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/news/0/it";

    private NewsServiceListener mListener;
    private int mLastQueryLimit;
    private boolean mQueryInProgress = false;

    public boolean getNews(NewsServiceListener listener) {
        if (mQueryInProgress) { return false; }
        mQueryInProgress = true;
        mListener = listener;
        String endpoint = ENDPOINT_FORMAT;

        new HttpGetTask(ENDPOINT_FORMAT, this).execute();
        return true;
    }


    @Override
    public void onHttpGetTaskCompleted(String response) {
        mQueryInProgress = false;
        try {
            List<News> results = new ArrayList<>();
            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++) {
                News news = new News();
                news.populate(array.getJSONObject(i));
                results.add(news);
            }

            if (mListener != null) {
                mListener.onNewsServiceSuccess(results);
            }


        } catch (JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Errore nel parsing del JSON");
            e.printStackTrace();

            if (mListener != null) {
                String msg = "Errore nell'eleborazione delle soluzioni di viaggio";
                mListener.onNewsServiceFailure(new InvalidNewsSolutions(msg));
            }
        }
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        mQueryInProgress = false;
        if (mListener != null) {
            mListener.onNewsServiceFailure(e);
            mListener = null;
        }
    }


    public interface NewsServiceListener {
        void onNewsServiceSuccess(List<News> result);
        void onNewsServiceFailure(Exception exc);
    }

    public class InvalidNewsSolutions extends Exception {
        InvalidNewsSolutions(String message) {
            super(message);
        }
    }

}

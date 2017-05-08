package com.manzolik.gmanzoli.mytrains.service;

import android.os.AsyncTask;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/*
* AsyncTask<Params, Progress, Result>
* */
class HttpGetTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = HttpGetTask.class.getSimpleName();

    private String mEndpoint;
    private HttpGetTaskListener mListener;
    private Exception mError;


    HttpGetTask(String endpoint, HttpGetTaskListener listener) {
        this.mListener = listener;
        this.mEndpoint = endpoint.replace(" ", "%20");
    }

    @Override
    protected String doInBackground(Void... params) {
        if (BuildConfig.DEBUG) Log.d(TAG, mEndpoint);

        try {
            URL url = new URL(mEndpoint);
            URLConnection connection = url.openConnection();

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null){
                result.append(line);
            }

            return result.toString();
        }catch (IOException e){
            this.mError = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (response == null){
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed! " + mEndpoint);
                Log.e(TAG, mError.getClass().toString());
                Log.e(TAG, mError.getMessage());
            }
            mListener.onHttpGetTaskFailed(mError);
        } else {
            mListener.onHttpGetTaskCompleted(response);
        }
    }

    interface HttpGetTaskListener {
        void onHttpGetTaskCompleted(String response);
        void onHttpGetTaskFailed(Exception e);
    }
}

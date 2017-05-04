package com.manzolik.gmanzoli.mytrains;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class NoConnectivityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connectivity);

        // Aggiorna il titolo
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle(R.string.app_name);
        }
    }

    public void retryStart(View view) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isNetworkConnected = networkInfo != null && networkInfo.isConnected();

        if (isNetworkConnected) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "Connessione non ancora disponibile", Toast.LENGTH_LONG).show();
        }
    }
}

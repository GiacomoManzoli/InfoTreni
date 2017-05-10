package com.manzolik.gmanzoli.mytrains.utils;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.manzolik.gmanzoli.mytrains.services.DatabaseMaintenanceService;

public class MaintenanceUtils {


    public static AlertDialog.Builder buildMaintenanceDialog(final Activity activity, boolean askForNow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        String dialogMessage = "Il database dell'applicazione richiede della manutenzione. " +
                "Alcune funzionalità potrebbero comportarsi in modo anomalo.";

        if (askForNow) {
            dialogMessage += "Eseguire la manutenzione del database adesso?";
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startMaintenance(activity);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Non fa niente
                }
            });
        } else {
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Non fa niente
                }
            });
        }
        builder.setMessage(dialogMessage);
        return builder;
    }

    public static void startMaintenance(Activity activity) {
        Intent i = new Intent(activity, DatabaseMaintenanceService.class);
        activity.startService(i);
    }

    public static void startSilenceMaintenance(Activity activity) {
        Intent i = new Intent(activity, DatabaseMaintenanceService.class);
        i.putExtra(DatabaseMaintenanceService.ARG_SILENT, true);
        activity.startService(i);
    }
}

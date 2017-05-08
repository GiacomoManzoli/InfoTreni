package com.manzolik.gmanzoli.mytrains.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private static final String TAG = LocationUtils.class.getSimpleName();

    @Nullable
    public static Location getLastLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        /* NOTA: uso ACCURACY_FINE perché con COARSE getLastKnownLocation ritorna sempre null */
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String provider = locationManager.getBestProvider(criteria, true);
        boolean localizationPermitted = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (localizationPermitted) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Provider: " + provider);
            Location lastLocation = locationManager.getLastKnownLocation(provider);
                /* lastLocation = null anche se la geolocalizzazione è disabilitata a sistema */
            if (lastLocation != null) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Location: " +lastLocation.toString() );
                return lastLocation;
            } else {
                if (BuildConfig.DEBUG) Log.d(TAG, "Location: null");
            }
        } else {
            if (BuildConfig.DEBUG) Log.e(TAG, "Localizzazione non permessa");
        }
        return null;
    }

    public static String getAddress(Context context, double latitude, double longitude) {

        Geocoder gc = new Geocoder(context, Locale.getDefault());

        if(Geocoder.isPresent()) {
            try {
                List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
                StringBuilder sb = new StringBuilder();
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);

                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                        sb.append(address.getAddressLine(i)).append("\n");

                    sb.append(address.getLocality()).append("\n");
                    sb.append(address.getPostalCode()).append("\n");
                    sb.append(address.getCountryName());
                }
                return sb.toString();
            } catch (IOException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
                return null;
            }
        }else {
            if (BuildConfig.DEBUG) Log.e(TAG, "Geodecoder non presente");
            return null;
        }
    }

}

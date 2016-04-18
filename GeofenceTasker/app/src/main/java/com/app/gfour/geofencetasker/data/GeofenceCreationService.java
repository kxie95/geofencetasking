package com.app.gfour.geofencetasker.data;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.app.gfour.geofencetasker.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.ResultCallback;

import java.util.ArrayList;
import java.util.List;

public class GeofenceCreationService extends IntentService
        implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    protected List<Geofence> mGeofenceList;

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    private TaskHelper mTaskHelper;

    private static final String TAG = "GeofenceCreationService";

    public GeofenceCreationService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create TaskHelper.
        mTaskHelper = new TaskHelper(this);

        // Create client.
        buildGoogleApiClient();

        // Create empty geofence list.
        mGeofenceList = new ArrayList<Geofence>();

        // Start client.
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            // logSecurityException(securityException);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");
        // onConnected() will be called again automatically when the service reconnects
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get ID of task.
        int id = mTaskHelper.getIdByFields(intent.getStringExtra("title"), intent.getStringExtra("address"));

        double taskLatitude = intent.getDoubleExtra("latitude", 0.0);
        double taskLongitude = intent.getDoubleExtra("longitude", 0.0);

        addToGeofenceList(id, taskLatitude, taskLongitude);
    }

    // Adds Geofence to List
    private void addToGeofenceList(int geofenceID, double latitude, double longitude) {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(Integer.toString(geofenceID))
                    .setCircularRegion(
                            latitude,
                            longitude,
                            10
                    )
                    .setExpirationDuration(86400000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Geofence created for new task.");
        } else {
            Log.e(TAG, "Something went wrong. Geofence not created.");
        }
    }
}


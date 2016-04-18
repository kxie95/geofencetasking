package com.app.gfour.geofencetasker.data;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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

        mTaskHelper = new TaskHelper(this);

        // Create client.
        buildGoogleApiClient();

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

        String taskTitle = intent.getStringExtra("title");
        String taskAddress = intent.getStringExtra("title");
        double taskLatitude = intent.getDoubleExtra("latitude", 0.0);
        double taskLongitude = intent.getDoubleExtra("longitude", 0.0);

        //Get ID of task.
        mTaskHelper

        addToGeofenceList(taskTitle, );

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            //logSecurityException(securityException);
        }
    }

    // Adds Geofence to List
    private void addToGeofenceList(String geofenceID, Place selectedPlace) {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(geofenceID)
                    .setCircularRegion(
                            selectedPlace.getLatLng().latitude,
                            selectedPlace.getLatLng().longitude,
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

    }
}


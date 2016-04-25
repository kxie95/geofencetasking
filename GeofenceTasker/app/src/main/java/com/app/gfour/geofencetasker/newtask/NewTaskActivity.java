package com.app.gfour.geofencetasker.newtask;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.gfour.geofencetasker.R;
import com.app.gfour.geofencetasker.data.GeofenceIntentService;
import com.app.gfour.geofencetasker.data.Task;
import com.app.gfour.geofencetasker.data.TaskHelper;
import com.app.gfour.geofencetasker.data.Ag;
import com.app.gfour.geofencetasker.tasks.TasksActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for creating a new task. Also contains logic for
 * creating geofence for each task.
 * Credits to: http://developer.android.com/training/location/geofencing.html
 */
public class NewTaskActivity extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>{

    private EditText mTitle;
    private Button mDoneButton;
    private SupportPlaceAutocompleteFragment mSupportPlaceFragment;
    private TaskHelper mTaskHelper;

    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    protected List<Geofence> mGeofenceList;

    /**
     * Store location here after the user selects it from gmap.
     */
    private String mSelectedAddress;
    private String TAG = "NewTaskActivity";

    private Place mSelectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // Initialise database
        mTaskHelper = new TaskHelper(this);

        // Initialise UI components
        mTitle = (EditText) findViewById(R.id.new_task_title);
        mDoneButton = (Button) findViewById(R.id.new_tast_done_button);

        // Place picker fragment
        mSupportPlaceFragment = (SupportPlaceAutocompleteFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        View.OnClickListener doneBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedAddress == null) {
                    Toast.makeText(getBaseContext(), "Please select a location.", Toast.LENGTH_LONG).show();
                } else if (mTitle.getText() == null || mTitle.getText().toString().equals("")) {
                    Toast.makeText(getBaseContext(), "Please give your task a title.", Toast.LENGTH_LONG).show();
                } else {
                    Task task = new Task(mTitle.getText().toString(), mSelectedAddress);

                    mTaskHelper.addTask(task);

                    // Create geofence for new task.
                    if (mSelectedPlace != null) {
                        int id = mTaskHelper.getIdByFields(task.getTitle(), task.getAddress());
                        addToGeofenceList(id, mSelectedPlace.getLatLng().latitude, mSelectedPlace.getLatLng().longitude);
                        createGeofence();
                    }
                }
            }
        };

        // Add listeners
        mDoneButton.setOnClickListener(doneBtnListener);

        mSupportPlaceFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                mSelectedPlace = place;
                mSelectedAddress = place.getAddress().toString();
                mSupportPlaceFragment.setText(mSelectedAddress);
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onError: Status = " + status.toString());

                Toast.makeText(getBaseContext(), status.getStatusMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });
        // Create client.
        buildGoogleApiClient();

        // Create empty geofence list.
        mGeofenceList = new ArrayList<Geofence>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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

        createGeofence();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Geofence created for new task.");
            //Return back to the main task list activity.
            Intent intent = new Intent(NewTaskActivity.this, TasksActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //Set arguments.
            intent.putExtra("title", mTitle.getText().toString());
            intent.putExtra("address", mSelectedAddress);

            startActivity(intent);
        } else {
            Log.e(TAG, "ERROR: Geofence not created. (Status Code:" + status.getStatusCode() + ")");
        }
    }

    private void addToGeofenceList(int geofenceID, double latitude, double longitude) {
        Log.d(TAG, "ID: " + geofenceID + ", Latitude: " + latitude + ", Longitude: " + longitude);
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(Integer.toString(geofenceID))
                .setCircularRegion(
                        latitude,
                        longitude,
                        200
                )
                .setExpirationDuration(86400000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        if (mGeofenceList == null || mGeofenceList.isEmpty()) {
            return null;
        }
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
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createGeofence() {
        // Check if client is connected.
        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }

        GeofencingRequest geofencingRequest = getGeofencingRequest();
        PendingIntent mGeofencePendingIntent = getGeofencePendingIntent();

        if (geofencingRequest != null && mGeofencePendingIntent != null) {
            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        // The GeofenceRequest object.
                        geofencingRequest,
                        // This pending intent is used to generate an intent when a matched geofence
                        // transition is observed.
                        mGeofencePendingIntent
                ).setResultCallback(this); // Result processed in onResult().
                LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
                Log.i(TAG, "gps enabled?" + lm.isProviderEnabled(LocationManager.GPS_PROVIDER));
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                Log.e(TAG, "Invalid location permission.", securityException);
            }
        }
    }
}


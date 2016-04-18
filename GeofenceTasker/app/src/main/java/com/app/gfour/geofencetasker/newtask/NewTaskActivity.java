package com.app.gfour.geofencetasker.newtask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.gfour.geofencetasker.R;
import com.app.gfour.geofencetasker.data.GeofenceCreationService;
import com.app.gfour.geofencetasker.data.Task;
import com.app.gfour.geofencetasker.data.TaskHelper;
import com.app.gfour.geofencetasker.tasks.TasksActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
/**
 * Shows the UI for creating a new task.
 * Note: I would normally create a fragment to go with this activity for best practices,
 * but we just wanna hack this thing out.
 */
public class NewTaskActivity extends AppCompatActivity {

    private EditText mTitle;
    private Button mDoneButton;
    private SupportPlaceAutocompleteFragment mSupportPlaceFragment;
    private TaskHelper mTaskHelper;
    private GeofenceCreationService mGeofenceCreationService;

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

        // Create GeofenceCreationService
        mGeofenceCreationService = new GeofenceCreationService();

        // Add listeners
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Save title, description and location (latlong probs) in storage.
                if (mSelectedAddress == null) {
                    Toast.makeText(getBaseContext(), "Please select a location.", Toast.LENGTH_LONG).show();
                } else if (mTitle.getText() == null || mTitle.getText().equals("")) {
                    Toast.makeText(getBaseContext(), "Please give your task a title.", Toast.LENGTH_LONG).show();
                } else {
                    Task task = new Task(mTitle.getText().toString(), mSelectedAddress);

                    mTaskHelper.addTask(task);

                    // Start intent service for creating task's geofence.
                    if (mSelectedPlace != null) {
                        Intent serviceIntent = new Intent(NewTaskActivity.this, GeofenceCreationService.class);
                        serviceIntent.putExtra("title", task.getTitle());
                        serviceIntent.putExtra("address", task.getAddress());
                        serviceIntent.putExtra("latitude", mSelectedPlace.getLatLng().latitude);
                        serviceIntent.putExtra("longitude", mSelectedPlace.getLatLng().longitude);
                        startService(serviceIntent);
                    }

                    //Return back to the main task list activity.
                    Intent intent = new Intent(NewTaskActivity.this, TasksActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    //Set arguments.
                    intent.putExtra("title", task.getTitle());
                    intent.putExtra("address", task.getAddress());

                    startActivity(intent);
                }
            }
        });

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
    }
}


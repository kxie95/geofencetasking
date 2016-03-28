package com.app.gfour.geofencetasker.newtask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.gfour.geofencetasker.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Shows the UI for creating a new task.
 * Note: I would normally create a fragment to go with this activity for best practices,
 * but we just wanna hack this thing out.
 */
public class NewTaskActivity extends AppCompatActivity {

    private EditText mTitle;
    private EditText mDescription;
    private EditText mLocation;
    private Button mDoneButton;

    /**
     * Store location here after the user selects it from gmap.
     */
    private LatLng mSelectedLocation;
    private String TAG = "NewTaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        mTitle = (EditText) findViewById(R.id.new_task_title);
        mDescription = (EditText) findViewById(R.id.new_task_description);
        //mLocation = (EditText) findViewById(R.id.new_task_location);
        mDoneButton = (Button) findViewById(R.id.new_tast_done_button);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Save title, description and location (latlong probs) in storage.
                if (mSelectedLocation == null) {
                    Toast.makeText(getBaseContext(), "Please select a location.", Toast.LENGTH_LONG).show();
                } else if (mTitle.getText() == null || mTitle.getText().equals("")) {
                    Toast.makeText(getBaseContext(), "Please give your task a title.", Toast.LENGTH_LONG).show();
                } else {
                    // TODO: Save details in SQLiteDB. LatLng to be stored as (double lat, double lon).
                }

            }
        });

        // Place picker fragment
        final SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                mSelectedLocation = place.getLatLng();
                autocompleteFragment.setText(place.getAddress());
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

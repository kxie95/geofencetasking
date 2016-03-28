package com.app.gfour.geofencetasker.newtask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.app.gfour.geofencetasker.R;

/**
 * Shows the UI for creating a new task.
 * Note: I would normally create a fragment to go with this activity for best practices,
 * but we just wanna hack this thing out.
 */
public class NewTaskActivity extends AppCompatActivity {

    EditText mTitle;
    EditText mDescription;
    EditText mLocation;
    Button mDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        mTitle = (EditText) findViewById(R.id.new_task_title);
        mDescription = (EditText) findViewById(R.id.new_task_description);
        mLocation = (EditText) findViewById(R.id.new_task_location);
        mDoneButton = (Button) findViewById(R.id.new_tast_done_button);
    }
}

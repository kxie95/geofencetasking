package com.app.gfour.geofencetasker.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.app.gfour.geofencetasker.R;
import com.app.gfour.geofencetasker.data.Task;
import com.app.gfour.geofencetasker.data.TaskHelper;
import com.app.gfour.geofencetasker.newtask.NewTaskActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TasksActivity extends AppCompatActivity {
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;

    private TaskHelper taskHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btnAddItem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TasksActivity.this, NewTaskActivity.class));
            }
        });

        taskHelper = new TaskHelper(this);

        lvItems = (ListView) findViewById(R.id.lvItems);
        items = new ArrayList<String>();

        for (Task task : taskHelper.getAllTasks()){
            items.add(task.getTitle() + "\n" + task.getAddress());
        }

        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);

        //setupListViewListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//
//    // Attaches a long click listener to the listview
//    private void setupListViewListener() {
//        lvItems.setOnItemLongClickListener(
//                new AdapterView.OnItemLongClickListener() {
//                    @Override
//                    public boolean onItemLongClick(AdapterView<?> adapter,
//                                                   View item, int pos, long id) {
//                        // Remove the item within array at position
//                        items.remove(pos);
//                        // Refresh the adapter
//                        itemsAdapter.notifyDataSetChanged();
//
//                        writeItems();
//                        // Return true consumes the long click event (marks it handled)
//                        return true;
//                    }
//
//                });
//    }
}

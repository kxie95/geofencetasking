package com.app.gfour.geofencetasker.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.app.gfour.geofencetasker.R;
import com.app.gfour.geofencetasker.data.Task;
import com.app.gfour.geofencetasker.data.TaskHelper;
import com.app.gfour.geofencetasker.newtask.NewTaskActivity;

import java.util.ArrayList;

import static android.view.ContextMenu.ContextMenuInfo;

/**
 * Main Activity which shows list of tasks.
 * Credits to: https://guides.codepath.com/android/Basic-Todo-App-Tutorial
 */
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
        registerForContextMenu(lvItems);
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteItem:
                String taskItem = itemsAdapter.getItem(info.position);

                // Remove the task from the database.
                String[] taskFields = taskItem.split("\n");
                int id = taskHelper.getIdByFields(taskFields[0], taskFields[1]);
                taskHelper.deleteTask(id);

                // Remove the task from the listView.
                itemsAdapter.remove(taskItem);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

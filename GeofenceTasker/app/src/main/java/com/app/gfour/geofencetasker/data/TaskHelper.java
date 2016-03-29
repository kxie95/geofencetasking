package com.app.gfour.geofencetasker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.app.gfour.geofencetasker.data.TaskContract.*;

/**
 * Helper for storing task data.
 * Reference: http://hmkcode.com/android-simple-sqlite-database-tutorial/.
 */
public class TaskHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Tasks.db";
    public static final String TABLE_NAME = TaskEntry.TABLE_NAME;

    private SQLiteDatabase db;

    public TaskHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

    @Override
	public void onCreate(SQLiteDatabase db) {
        Log.i("onCreate", "creating db");
        // SQL statement to create task table
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TaskEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TaskEntry.COLUMN_NAME_TITLE + " TEXT, " +
                        TaskEntry.COLUMN_NAME_ADDRESS + " TEXT )";

        db.execSQL(CREATE_TABLE);
	}

    @Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("onUpgrade", "droppping table then creating");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

    @Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("onDowngrade", "calling onUpgrade");
		onUpgrade(db, oldVersion, newVersion);
	}

	public void addTask(Task task) {
        Log.i("addTask", task.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        // Create values to add into the table.
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TaskEntry.COLUMN_NAME_ADDRESS, task.getAddress());

        // Insert values
        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, //table name
                TaskEntry.COLUMN_NAME_ID + " = ?",  // selections
                new String[]{String.valueOf(id)}); // selections args

        db.close();
    }

    public List<Task> getAllTasks() {
        Log.i("getAllTasks", "getAllTasks");
        List<Task> tasks = new ArrayList<Task>();

        // Query to select all tasks from the task table
        String query = "SELECT * FROM " + TABLE_NAME;

        // Get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // Go over each row, build task and add it to list
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setAddress(cursor.getString(2));

                tasks.add(task);
            } while (cursor.moveToNext());
        }

        Log.d("getAllTasks()", tasks.toString());

        return tasks;
    }
}

package com.app.gfour.geofencetasker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.app.gfour.geofencetasker.data.TaskContract.TaskEntry;

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
        // SQL statement to create task table
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TaskEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TaskEntry.COLUMN_NAME_TITLE + " TEXT, " +
                        TaskEntry.COLUMN_NAME_ADDRESS + " TEXT )";

        db.execSQL(CREATE_TABLE);
	}

    @Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

    @Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public void addTask(Task task) {
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

    public Task getTaskById(int id) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_NAME, // a. table
                        TaskEntry.COLUMNS, // b. column names
                        TaskEntry.COLUMN_NAME_ID + " = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        Task task = new Task();

        if (cursor.moveToFirst()) {
            task.setId(cursor.getInt(0));
            task.setTitle(cursor.getString(1));
            task.setAddress(cursor.getString(2));
        }

        cursor.close();
        db.close();

        return task;
    }

    public int getIdByFields(String title, String address) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_NAME, // a. table
                        new String[] {TaskEntry.COLUMN_NAME_ID}, // b. column names
                        TaskEntry.COLUMN_NAME_TITLE + " = ? AND "
                                + TaskEntry.COLUMN_NAME_ADDRESS + " = ?", // c. selections
                        new String[] {title, address}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        int id = -1;

        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }

        cursor.close();
        db.close();


        return id;
    }

    public List<Task> getAllTasks() {
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


        cursor.close();
        db.close();

        return tasks;
    }
}

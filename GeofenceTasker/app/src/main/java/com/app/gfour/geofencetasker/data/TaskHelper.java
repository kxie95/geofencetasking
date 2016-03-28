package com.app.gfour.geofencetasker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.app.gfour.geofencetasker.data.TaskContract.*;

/**
 * Helper for storing task data.
 */

public class TaskHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Tasks.db";

	private static final String TEXT_TYPE = " TEXT";
	private static final String REAL_TYPE = " REAL";
	private static final String COMMA_SEP = ",";

    /**
     * Commands to create and delete entries.
     */
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
					TaskEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
					TaskEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
					TaskEntry.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
					TaskEntry.COLUMN_NAME_LONG + REAL_TYPE +
			" )";
	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME;

	public TaskHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}

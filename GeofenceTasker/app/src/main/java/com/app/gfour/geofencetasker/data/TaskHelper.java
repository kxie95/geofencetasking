package com.app.gfour.geofencetasker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper for storing task data.
 */

public class TaskHelper extends SQLiteOpenHelper {

	public TaskHelper(Context context) {
		super(context, TaskContract.DB_NAME, null, TaskContract.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqlDB) {
		String sqlQuery = String.format("CREATE TABLE %s (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT)", TaskContract.TABLE, TaskContract.Columns.TASK);

		Log.d("TaskHelper","Query to form table: " + sqlQuery);
		sqlDB.execSQL(sqlQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
		sqlDB.execSQL("DROP TABLE IF EXISTS " + TaskContract.TABLE);
		onCreate(sqlDB);
	}
}

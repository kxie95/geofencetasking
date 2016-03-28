package com.app.gfour.geofencetasker.data;

import android.provider.BaseColumns;

/**
 * Defines the database schema for tasks.
 */
public final class TaskContract {
    public static final String DB_NAME = "com.example.TodoList.db.tasks";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "tasks";


    public class Columns {
        public static final String TASK = "task";
        public static final String ID = BaseColumns._ID;
    }

}

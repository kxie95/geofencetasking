package com.app.gfour.geofencetasker.data;

import android.provider.BaseColumns;

/**
 * Defines the database schema for tasks.
 */
public final class TaskContract {

    /**
     * To prevent someone from accidentally instantiating the contract class, give it an empty
     * constructor.
     */
    private TaskContract() {}

    /**
     * Inner class that defines the table contents.
     */
    public static abstract class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "TASKS";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_ADDRESS = "address";
    }
}

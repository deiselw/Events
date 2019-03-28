package com.dlwrasse.events.persistence.db.virtual;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.IOException;

public class DatabaseTable {
    private static final String TAG = "SearchDatabase";

    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_TIMESTAMP = "TIMESTAMP";

    private static final String DATABASE_NAME = "VIRTUAL_EVENT";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper mDatabaseOpenHelper;

    public DatabaseTable(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    public Cursor getTextMatches(String query, String[] columns) {
        String selection = COL_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;
        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts4 (" +
                        COL_ID + ", " +
                        COL_NAME + ", " +
                        COL_TIMESTAMP + ")";
        private static final String FTS_TABLE_INSERT =
                "INSERT INTO " + FTS_TABLE_CREATE + "(" +
                        COL_ID + ", " +
                        COL_TIMESTAMP + ", " +
                        COL_NAME + ")" +
                        " SELECT id, timestamp, name FROM event";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDatabase();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        private void loadDatabase() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        SQLiteStatement statement = mDatabase.compileStatement(FTS_TABLE_INSERT);
                        statement.execute();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }
}


package com.dlwrasse.events.persistence.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.db.converter.CalendarConverter;
import com.dlwrasse.events.persistence.db.dao.EventDao;
import com.dlwrasse.events.persistence.db.entity.Event;

/**
 * Created by Deise on 19/02/2018.
 */

@Database(entities = {Event.class}, version = 1)
@TypeConverters({CalendarConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase mInstance;

    public static AppDatabase getInstance(final Context context) {
        if (mInstance == null) {
            synchronized (AppDatabase.class) {
                mInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, context.getResources().getString(R.string.database_name))
                        .setJournalMode(JournalMode.TRUNCATE)
                        .build();
            }
        }
        return mInstance;
    }

    public static void reset() {
        mInstance = null;
    }

    public abstract EventDao eventDao();
}

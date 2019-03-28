package com.dlwrasse.events.persistence.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dlwrasse.events.persistence.db.entity.Event;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Deise on 19/02/2018.
 */

@Dao
public interface EventDao {
    @Query("SELECT * FROM event ORDER BY timestamp DESC")
    LiveData<List<Event>> eventsByTimestamp();

    @Query("SELECT * FROM event WHERE id == :id")
    LiveData<Event> event(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Event... events);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM event WHERE id == :id")
    void delete(int id);

    @Query("UPDATE event SET text = :text WHERE id == :id")
    void updateText(int id, String text);

    @Query("UPDATE event SET timestamp = :timestamp WHERE id == :id")
    void updateTimestamp(int id, Calendar timestamp);
}

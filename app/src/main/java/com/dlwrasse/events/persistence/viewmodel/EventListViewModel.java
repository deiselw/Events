package com.dlwrasse.events.persistence.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dlwrasse.events.persistence.EventRepository;
import com.dlwrasse.events.persistence.db.entity.Event;

import java.util.List;

/**
 * Created by Deise on 19/02/2018.
 */

public class EventListViewModel extends AndroidViewModel {
    private final EventRepository mRepository;

    public EventListViewModel(Application application) {
        super(application);

        mRepository = EventRepository.getInstance(application);
    }

    public LiveData<List<Event>> getEventsByTimestamp() {
        return mRepository.getEventsByTimestamp();
    }

    public void insert(Event event) { mRepository.insert(event); }

    public void delete(List<Integer> eventIds) { mRepository.delete(eventIds); }
}

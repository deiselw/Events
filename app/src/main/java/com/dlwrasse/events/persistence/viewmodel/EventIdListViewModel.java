package com.dlwrasse.events.persistence.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dlwrasse.events.persistence.EventRepository;

import java.util.List;

/**
 * Created by Deise on 19/02/2018.
 */

public class EventIdListViewModel extends AndroidViewModel {
    private final EventRepository mRepository;

    public EventIdListViewModel(Application application) {
        super(application);

        mRepository = EventRepository.getInstance(application);
    }

    public LiveData<List<Integer>> getEventIdsByTimestamp() {
        return mRepository.getEventIdsByTimestamp();
    }

    public void delete(int eventId) {
        mRepository.delete(eventId);
    }
}

package com.dlwrasse.events.persistence.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.dlwrasse.events.persistence.EventRepository;
import com.dlwrasse.events.persistence.db.entity.Event;

public class EventViewModel extends AndroidViewModel {
    private EventRepository mRepository;
    private LiveData<Event> mEvent;

    public EventViewModel(Application application, int eventId) {
        super(application);
        mRepository = EventRepository.getInstance(application);
        mEvent = mRepository.getEvent(eventId);
    }

    public LiveData<Event> getEvent() {
        return mEvent;
    }

    public void update(Event event) { mRepository.update(event); }

    public void updateText(Event event) { mRepository.updateText(event); }

    public void updateTimestamp(Event event) { mRepository.updateTimestamp(event); }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;
        private final int mEventId;

        public Factory(@NonNull Application application, int eventId) {
            mApplication = application;
            mEventId = eventId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new EventViewModel(mApplication, mEventId);
        }
    }
}

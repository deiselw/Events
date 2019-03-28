package com.dlwrasse.events.persistence.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dlwrasse.events.persistence.EventRepository;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;

import java.util.List;

/**
 * Created by Deise on 19/02/2018.
 */

public class TagListViewModel extends AndroidViewModel {
    private EventRepository mRepository;

    public TagListViewModel(Application application) {
        super(application);

        mRepository = EventRepository.getInstance(application);
    }

    public LiveData<List<Tag>> getTags() { return mRepository.getTags(); }
}

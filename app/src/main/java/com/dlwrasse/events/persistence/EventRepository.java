package com.dlwrasse.events.persistence;

import android.app.Application;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import android.os.AsyncTask;

import com.dlwrasse.events.persistence.db.AppDatabase;
import com.dlwrasse.events.persistence.db.dao.EventDao;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Deise on 22/02/2018.
 */

public class EventRepository {
    private static EventRepository mInstance;
    private EventDao mEventDao;
    private LiveData<List<Event>> mEventsByTimestamp;
    private LiveData<List<Tag>> mTags;
    private LiveData<List<Integer>> mEventIdsByTimestamp;
    private Application mApplication;

    public static EventRepository getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new EventRepository(application);
        }
        return mInstance;
    }

    public void close() {
        AppDatabase.getInstance(mApplication).close();
        AppDatabase.getInstance(mApplication).reset();
    }

    public void open() {
        initDatabase(mApplication);
    }

    public boolean isOpen() {
        return AppDatabase.getInstance(mApplication).isOpen();
    }

    public LiveData<List<Event>> getEventsByTimestamp() { return mEventsByTimestamp; }

    public LiveData<List<Integer>> getEventIdsByTimestamp() { return mEventIdsByTimestamp; }

    public LiveData<List<Tag>> getTags() { return mTags; }

    public LiveData<Event> getEvent(int eventId) { return mEventDao.event(eventId); }

    public void insert(Event event) {
        new InsertAsyncTask(mEventDao).execute(event);
    }

    public void delete(Event event) {
        new DeleteAsyncTask(mEventDao).execute(event);
    }

    public void delete(int eventId) {
        new DeleteByIdAsyncTask(mEventDao).execute(eventId);
    }

    public void delete(List<Integer> eventIdList) {
        new DeleteAllAsyncTask(mEventDao).execute(eventIdList);
    }

    public void update(Event event) {
        new UpdateAsyncTask(mEventDao).execute(event);
    }

    public void updateText(Event event) {
        new UpdateTextAsyncTask(mEventDao).execute(event);
    }

    public void updateTimestamp(Event event) {
        new UpdateTimestampAsyncTask(mEventDao).execute(event);
    }

    private static class InsertAsyncTask extends AsyncTask<Event, Void, Void> {
        private EventDao mAsyncTaskDao;

        InsertAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Event... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Event, Void, Void> {
        private EventDao mAsyncTaskDao;

        DeleteAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Event... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class DeleteByIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private EventDao mAsyncTaskDao;

        DeleteByIdAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<List<Integer>, Void, Void> {
        private EventDao mAsyncTaskDao;

        DeleteAllAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<Integer>... params) {
            List<Integer> eventIdList = params[0];
            for (int i = 0; i < eventIdList.size(); i++) {
                mAsyncTaskDao.delete(eventIdList.get(i));
            }
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Event, Void, Void> {
        private EventDao mAsyncTaskDao;

        UpdateAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Event... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateTextAsyncTask extends AsyncTask<Event, Void, Void> {
        private EventDao mAsyncTaskDao;

        UpdateTextAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Event... params) {
            Event event = params[0];
            mAsyncTaskDao.updateText(event.getId(), event.getText());
            return null;
        }
    }

    private static class UpdateTimestampAsyncTask extends AsyncTask<Event, Void, Void> {
        private EventDao mAsyncTaskDao;

        UpdateTimestampAsyncTask(EventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Event... params) {
            Event event = params[0];
            mAsyncTaskDao.updateTimestamp(event.getId(), event.getTimestamp());
            return null;
        }
    }

    private EventRepository(Application application) {
        initDatabase(application);
    }

    private void initDatabase(Application application) {
        mApplication = application;

        AppDatabase db = AppDatabase.getInstance(mApplication);
        mEventDao = db.eventDao();

        mEventsByTimestamp = mEventDao.eventsByTimestamp();

        mEventIdsByTimestamp = Transformations.map(mEventsByTimestamp, new Function<List<Event>, List<Integer>>() {
            @Override
            public List<Integer> apply(List<Event> eventList) {
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < eventList.size(); i++) {
                    ids.add(eventList.get(i).getId());
                }
                return ids;
            }
        });

        mTags = Transformations.map(mEventsByTimestamp, new Function<List<Event>, List<Tag>>() {
            @Override
            public List<Tag> apply(List<Event> eventList) {
                HashMap<String, Tag> tagMap = new HashMap<>();

                for (int i = 0; i < eventList.size(); i++) {
                    Event event = eventList.get(i);
                    for (String eventTag : event.getTags()) {
                        Tag tag = tagMap.get(eventTag);
                        if (tag == null) {
                            tag = new Tag(eventTag);
                            tag.addEventId(event.getId());
                            tagMap.put(eventTag, tag);
                        }else {
                            tag.addEventId(event.getId());
                            tag.incTagCount();
                        }
                    }
                }

                List<Tag> tagList = new ArrayList<Tag>(tagMap.values());
                Collections.sort(tagList, Tag.COMPARATOR);
                return tagList;
            }
        });
    }
}
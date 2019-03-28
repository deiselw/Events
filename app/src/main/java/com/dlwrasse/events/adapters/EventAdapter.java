package com.dlwrasse.events.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.helpers.EventMultiSelectHelper;
import com.dlwrasse.events.interfaces.OnViewHolderInteractionListener;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;
import com.dlwrasse.events.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Deise on 19/02/2018.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>
        implements Filterable, EventFilter.OnFilterResultsListener {
    private static final String TAG = "EventAdapter";

    private OnViewHolderInteractionListener mListener;
    private List<Event> mFullList;
    private List<Event> mFilteredList;
    private List<Tag> mTagList;
    private EventFilter mFilter;

    private EventMultiSelectHelper mMultiSelectHelper;

    public EventAdapter(OnViewHolderInteractionListener viewHolderListener,
                        EventMultiSelectHelper.OnMultiSelectActiveListener multiSelectActiveListener) {
        mListener = viewHolderListener;
        mFilteredList = new ArrayList<>();
        mMultiSelectHelper = new EventMultiSelectHelper(multiSelectActiveListener);

        mFilter = new EventFilter(this);

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return mFullList.get(position).getId();
    }

    public void setEventList(List<Event> eventList) {
        mFullList = eventList;
        mFilter.setContentList(mFullList);
        mFilteredList.clear();
        mFilteredList.addAll(eventList);
        notifyDataSetChanged();
    }

    public void setTagList(List<Tag> tagList) {
        mTagList = tagList;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EventViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_item_event, parent, false));
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        Event event = mFilteredList.get(position);

        final int id = event.getId();
        final int index = mFullList.indexOf(event);

        if (holder.itemView.isSelected()) {
            holder.itemView.setSelected(false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMultiSelectHelper.isActive()) {
                    v.setSelected(!v.isSelected());
                    mMultiSelectHelper.select(id);
                    return;
                }
                mListener.onViewHolderItemClick(index);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setSelected(!v.isSelected());
                mMultiSelectHelper.select(id);
                return true;
            }
        });

        holder.nameTextView.setText(event.getTaggedText());

        Calendar timestamp = event.getTimestamp();
        String timeText = DateUtils.formatDateTime(holder.itemView.getContext(),
                timestamp.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
        holder.timeTextView.setText(timeText);
    }

    @Override
    public int getItemViewType(int position) {
        Calendar calendar = mFilteredList.get(position).getTimestamp();
        int itemCount = getItemCount();

        boolean isFooter = false;
        int nextPosition = position + 1;
        if (nextPosition == itemCount) { // last item
            isFooter = true;
        }else if (nextPosition < itemCount) {
            isFooter = !CalendarUtils.sameDate(calendar,
                    mFilteredList.get(nextPosition).getTimestamp());
        }

        boolean isHeader;
        if (position == 0) {
            isHeader = true;
        }else {
            int prevPos = position - 1;
            isHeader = !CalendarUtils.sameDate(calendar, mFilteredList.get(prevPos).getTimestamp());
        }

        int viewType = EventViewHolderType.VIEW_TYPE_DEFAULT;
        if (isHeader) viewType |= EventViewHolderType.VIEW_TYPE_HEADER;
        if (isFooter) viewType |= EventViewHolderType.VIEW_TYPE_FOOTER;
        return viewType;
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    public int getFullListCount() {
        return mFullList.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void endMultiSelect() {
        mMultiSelectHelper.end();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedIds() {
        return mMultiSelectHelper.getSelectedList();
    }

    public int getSelectedCount() {
        return mMultiSelectHelper.getCount();
    }

    public Event getEvent(int position) {
        return mFilteredList.get(position);
    }

    public void filterByTag(int tagIndex) {
        mFilter.reset(mFullList);
        mFilteredList.clear();

        Tag tag = mTagList.get(tagIndex);
        ArrayList<Integer> eventIds = tag.getEventIds();
        for (int i = 0; i < mFullList.size(); i++) {
            Event event = mFullList.get(i);
            if (eventIds.contains(event.getId())) {
                mFilteredList.add(event);
            }
        }
        mFilter.setContentList(mFilteredList);
        notifyDataSetChanged();
    }

    public void clearFilter() {
        mFilter.reset(mFullList);

        mFilteredList.clear();
        mFilteredList.addAll(mFullList);

        notifyDataSetChanged();
    }

    @Override
    public void onFilterResults(List<Event> results) {
        mFilteredList = results;
        notifyDataSetChanged();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView nameTextView;
        LinearLayout itemLinearLayout;

        public EventViewHolder(View view) {
            super(view);
            timeTextView = view.findViewById(R.id.text_item_info);
            nameTextView = view.findViewById(R.id.text_item_content);
            itemLinearLayout = view.findViewById(R.id.layout_item);
        }
    }
}
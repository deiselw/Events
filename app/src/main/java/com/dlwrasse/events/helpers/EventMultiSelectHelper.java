package com.dlwrasse.events.helpers;

import java.util.ArrayList;
import java.util.List;

public class EventMultiSelectHelper {
    private List<Integer> mSelected = null;
    private boolean mActive;
    private OnMultiSelectActiveListener mListener;

    public EventMultiSelectHelper(OnMultiSelectActiveListener listener) {
        mActive = false;
        mListener = listener;
    }

    public boolean isActive() {
        return mActive;
    }

    public void select(Integer selectedId) {
        if (mSelected == null) {
            mSelected = new ArrayList<>();
            mSelected.add(selectedId);
            mActive = true;
            mListener.onMultiSelectChanged(mActive);
        }else {
            if (mSelected.contains(selectedId)) {
                mSelected.remove(selectedId);
                if (mSelected.isEmpty()) {
                    mSelected = null;
                    mActive = false;
                    mListener.onMultiSelectChanged(mActive);
                }
            }else {
                mSelected.add(selectedId);
                if (!mActive) {
                    mActive = true;
                    mListener.onMultiSelectChanged(mActive);
                }
            }
        }

        int count = getCount();
        if (count != 0) {
            mListener.onMultiSelectCountChanged(count);
        }
    }

    public List<Integer> getSelectedList() {
        return mSelected;
    }

    public void end() {
        mActive = false;
        mSelected = null;
    }

    public int getCount() {
        if (mSelected == null) {
            return 0;
        }
        return mSelected.size();
    }

    public interface OnMultiSelectActiveListener {
        void onMultiSelectChanged(boolean active);
        void onMultiSelectCountChanged(int count);
    }
}

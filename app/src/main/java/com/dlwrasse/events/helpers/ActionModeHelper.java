package com.dlwrasse.events.helpers;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dlwrasse.events.R;

import androidx.appcompat.app.AlertDialog;

public class ActionModeHelper implements ActionMode.Callback, EventMultiSelectHelper.OnMultiSelectActiveListener {

    private Activity mActivity;
    private ActionMode mActionMode;
    private OnActionModeListener mListener;
    private int mSelectedCount = 0;

    public ActionModeHelper(Activity activity) {
        mActivity = activity;
    }

    public void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public void startActionMode() {
        mActionMode = mActivity.startActionMode(this);
    }

    // region ActionMode.Callback
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.activity_main_delete_event, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.item_delete) {
            getConfirmDeleteDialog().show();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mListener.onActionModeDestroyed();
    }
    // endregion

    private AlertDialog getConfirmDeleteDialog() {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(mActivity);
        confirmDialogBuilder.setMessage(mActivity.getResources()
                .getQuantityString(R.plurals.text_delete_event, mSelectedCount))
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mListener != null) {
                            mListener.onActionModeDeleteClicked();
                        }
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
        return confirmDialogBuilder.create();
    }

    // region Interfaces
    public void setOnActionModeListener(OnActionModeListener listener) {
        mListener = listener;
    }

    public interface OnActionModeListener {
        void onActionModeDeleteClicked();
        void onActionModeDestroyed();
    }
    // endregion

    // region OnMultiSelectActiveListener
    @Override
    public void onMultiSelectChanged(boolean active) {
        if (active) {
            startActionMode();
        }else {
            finishActionMode();
        }
    }

    @Override
    public void onMultiSelectCountChanged(int count) {
        mSelectedCount = count;
        mActionMode.setTitle(Integer.valueOf(count).toString());
    }
    // endregion
}

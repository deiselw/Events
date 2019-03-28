package com.dlwrasse.events.helpers;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.util.Log;

import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.EventRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class GoogleDriveAppFolderHelper {
    public static final String TAG = "GoogleDriveAppFolderH";

    private String mFilePath;
    private String mFileName;
    private Activity mActivity;
    private OnDataUploadListener mListener;
    private int mDeleteTaskCount = 0;

    public GoogleDriveAppFolderHelper(Activity activity, String fileAbsolutePath, String fileName, OnDataUploadListener listener) {
        mFilePath = fileAbsolutePath;
        mFileName = fileName;
        mActivity = activity;
        mListener = listener;
    }

    public void loadFromDrive(GoogleSignInAccount account) {
        if (!verifyAccount(account)) {
            return;
        }
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(mActivity, account);
        doSearchAndLoadFromDrive(driveResourceClient);
    }

    public void saveToDrive(GoogleSignInAccount account) {
        if (!verifyAccount(account)) {
            return;
        }
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(mActivity, account);
        doSaveToDrive(driveResourceClient);
    }

    // region load
    private void doSearchAndLoadFromDrive(final DriveResourceClient driveResourceClient) {
        driveResourceClient.getAppFolder().continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                DriveFolder parent = task.getResult();
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, mFileName))
                        .build();
                return driveResourceClient.queryChildren(parent, query);
            }
        }).addOnCompleteListener(new OnCompleteListener<MetadataBuffer>() {
            @Override
            public void onComplete(Task<MetadataBuffer> task) {
                if (task == null || !task.isSuccessful()) {
                    return;
                }
                MetadataBuffer metadataBuffer = task.getResult();
                if (metadataBuffer != null && metadataBuffer.getCount() > 0) {
                    Date modifiedDate = metadataBuffer.get(0).getModifiedDate();
                    String message = mActivity.getResources().
                            getString(R.string.dialog_message_confirmBackup, modifiedDate.toString());
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
                    dialogBuilder.setTitle(R.string.dialog_title_confirmBackup)
                            .setMessage(message)
                            .setPositiveButton(R.string.dialog_positive_confirmBackup,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            doLoadFromDrive(driveResourceClient);
                                        }
                                    })
                            .setNegativeButton(R.string.dialog_negative_confirmBackup,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {}
                                    }).create().show();
                }
            }
        });
    }

    private void doLoadFromDrive(final DriveResourceClient driveResourceClient) {
        driveResourceClient.getAppFolder().continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                DriveFolder parent = task.getResult();
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, mFileName))
                        .build();
                return driveResourceClient.queryChildren(parent, query);
            }
        }).continueWithTask(new Continuation<MetadataBuffer, Task<DriveContents>>() {
            @Override
            public Task<DriveContents> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                if (task == null || !task.isSuccessful()) {
                    return null;
                }

                MetadataBuffer metadataBuffer = task.getResult();
                if (metadataBuffer == null || metadataBuffer.getCount() <= 0) {
                    return null;
                }
                return driveResourceClient.openFile(metadataBuffer.get(0).getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);
            }
        }).continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents contents = task.getResult();
                EventRepository.getInstance(mActivity.getApplication()).close();
                try {
                    final FileOutputStream outLocal = new FileOutputStream(new File(mFilePath));

                    InputStream inDrive = contents.getInputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inDrive.read(buffer, 0, buffer.length)) > 0) {
                        outLocal.write(buffer, 0, bytesRead);
                    }
                    outLocal.flush();
                    outLocal.close();
                }catch (Exception e) {
                    throw e;
                }

                return driveResourceClient.discardContents(contents);
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mListener.onDataLoaded(task.isSuccessful());
            }
        });
    }
    // endregion

    // region save
    private void doSaveToDrive(final DriveResourceClient driveResourceClient) {
        driveResourceClient.getAppFolder().continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                DriveFolder parent = task.getResult();
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, mFileName))
                        .build();
                return driveResourceClient.queryChildren(parent, query);
            }
        }).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
            @Override
            public void onSuccess(MetadataBuffer metadataBuffer) {
                final int count = metadataBuffer.getCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        driveResourceClient.delete(metadataBuffer.get(i).getDriveId().asDriveFile())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mDeleteTaskCount++;
                                if (mDeleteTaskCount == count) {
                                    mDeleteTaskCount = 0;
                                    createFile(driveResourceClient);
                                }
                            }
                        });
                    }
                }else {
                    createFile(driveResourceClient);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "doSaveToDrive error: " + e.getMessage());
            }
        });
    }

    private void createFile(final DriveResourceClient driveResourceClient) {
        final Task<DriveFolder> appFolderTask = driveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = driveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                DriveFolder parent = appFolderTask.getResult();
                DriveContents contents = createContentsTask.getResult();
                OutputStream outDrive = contents.getOutputStream();

                FileInputStream inLocal = new FileInputStream(new File(mFilePath));

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inLocal.read(buffer, 0, buffer.length)) > 0){
                    outDrive.write(buffer, 0, bytesRead);
                }
                outDrive.flush();
                outDrive.close();
                inLocal.close();

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(mFileName)
                        .setMimeType("application/x-sqlite3")
                        .setStarred(false)
                        .build();

                return driveResourceClient.createFile(parent, changeSet, contents);
            }
        }).addOnCompleteListener(new OnCompleteListener<DriveFile>() {
            @Override
            public void onComplete(Task<DriveFile> task) {
                mListener.onDataSaved(task.isSuccessful());
            }
        });
    }
    // endregion

    public void cleanAppFolder(GoogleSignInAccount account) {
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(mActivity, account);
        doCleanAppFolder(driveResourceClient);
    }

    private void doCleanAppFolder(final DriveResourceClient driveResourceClient) {
        driveResourceClient.getAppFolder().continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                DriveFolder parent = task.getResult();
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, mFileName))
                        .build();
                return driveResourceClient.queryChildren(parent, query);
            }
        }).addOnCompleteListener(new OnCompleteListener<MetadataBuffer>() {
            @Override
            public void onComplete(@NonNull Task<MetadataBuffer> task) {
                if (task == null || !task.isSuccessful()) {
                    return;
                }

                MetadataBuffer metadataBuffer = task.getResult();
                if (metadataBuffer == null) {
                    return;
                }
                for (int i = 0; i < metadataBuffer.getCount(); i++) {
                    driveResourceClient.delete(metadataBuffer.get(i).getDriveId().asDriveResource());
                }
            }
        });
    }

    private boolean verifyAccount(GoogleSignInAccount account) {
        return account != null && GoogleSignInHelper.areScopesGranted(account);
    }

    public interface OnDataUploadListener {
        void onDataLoaded(boolean success);
        void onDataSaved(boolean success);
    }
}

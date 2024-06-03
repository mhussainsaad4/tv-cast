package com.sensustech.mytvcast.utils.Dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Async task to list items in a folder
 */
public class RevokeTokenTask extends AsyncTask<Void, Void, Void> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded();
        void onError(Exception e);
    }

    public RevokeTokenTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            mDbxClient.auth().tokenRevoke();
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}
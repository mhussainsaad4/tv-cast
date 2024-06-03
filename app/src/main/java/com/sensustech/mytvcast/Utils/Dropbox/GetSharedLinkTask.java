package com.sensustech.mytvcast.utils.Dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

/**
 * Async task to list items in a folder
 */
public class GetSharedLinkTask extends AsyncTask<String, Void, SharedLinkMetadata> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(SharedLinkMetadata result);
        void onError(Exception e);
    }

    public GetSharedLinkTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(SharedLinkMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected SharedLinkMetadata doInBackground(String... params) {
        try {
            return mDbxClient.sharing().createSharedLinkWithSettings(params[0]);
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}
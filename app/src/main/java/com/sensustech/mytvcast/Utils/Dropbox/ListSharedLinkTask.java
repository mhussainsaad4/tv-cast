package com.sensustech.mytvcast.utils.Dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;

/**
 * Async task to list items in a folder
 */
public class ListSharedLinkTask extends AsyncTask<String, Void, ListSharedLinksResult> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(ListSharedLinksResult result);
        void onError(Exception e);
    }

    public ListSharedLinkTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(ListSharedLinksResult result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected ListSharedLinksResult doInBackground(String... params) {
        try {
            return mDbxClient.sharing().listSharedLinksBuilder().withPath(params[0]).start();
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}
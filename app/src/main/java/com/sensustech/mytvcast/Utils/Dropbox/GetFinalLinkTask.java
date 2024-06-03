package com.sensustech.mytvcast.utils.Dropbox;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Async task to list items in a folder
 */
public class GetFinalLinkTask extends AsyncTask<String, Void, String> {

    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(String result);
        void onError(Exception e);
    }

    public GetFinalLinkTask(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return getFinalURL(params[0]);
        } catch (Exception e) {
            mException = e;
        }
        return null;
    }

    public static String getFinalURL(String url) throws IOException {
        String resultUrl = url;
        HttpURLConnection connection = null;
        try {
            URL urlConnect = new URL(url);
            connection = (HttpURLConnection) urlConnect.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String locationUrl = connection.getHeaderField("Location");
                if(!locationUrl.startsWith("http"))
                    locationUrl = urlConnect.getProtocol() + "://" + urlConnect.getHost() + locationUrl;
                if (locationUrl != null && locationUrl.trim().length() > 0) {
                    connection.disconnect();
                    resultUrl = getFinalURL(locationUrl);
                }
            }
        } catch (Exception e) {
        } finally {
            connection.disconnect();
        }
        return resultUrl;
    }

}
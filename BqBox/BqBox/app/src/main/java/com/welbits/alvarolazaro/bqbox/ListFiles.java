package com.welbits.alvarolazaro.bqbox;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ListFiles extends AsyncTask<Void, Void, List<DropboxAPI.Entry>> {

    private final OnLoadCompleted onLoadCompleted;
    private DropboxAPI dropboxApi;
    private String path;

    public ListFiles(DropboxAPI dropboxApi, String path, OnLoadCompleted onLoadCompleted) {
        this.dropboxApi = dropboxApi;
        this.path = path;
        this.onLoadCompleted = onLoadCompleted;
    }

    @Override
    protected List<DropboxAPI.Entry> doInBackground(Void... params) {
        try {
            return dropboxApi.search("/", ".epub", 1000, false);
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<DropboxAPI.Entry> result) {
        if (result == null) return;
        onLoadCompleted.onLoadCompleted(result);
    }
}
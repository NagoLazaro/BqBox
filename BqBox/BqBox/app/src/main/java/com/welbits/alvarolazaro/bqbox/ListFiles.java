package com.welbits.alvarolazaro.bqbox;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ListFiles extends AsyncTask<Void, Void, ArrayList<String>> {

    private DropboxAPI dropboxApi;
    private String path;
    private Handler handler;

    public ListFiles(DropboxAPI dropboxApi, String path, Handler handler) {
        this.dropboxApi = dropboxApi;
        this.path = path;
        this.handler = handler;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        ArrayList<String> files = new ArrayList<>();
        try {
            List<DropboxAPI.Entry> directory = dropboxApi.search("/", ".epub", 1000, false);
            System.out.println();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("data", result);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
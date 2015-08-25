package com.welbits.alvarolazaro.bqbox;

import com.dropbox.client2.DropboxAPI;

import java.util.ArrayList;
import java.util.List;

public interface OnLoadCompleted {
    void onLoadCompleted(List<DropboxAPI.Entry> items);
}

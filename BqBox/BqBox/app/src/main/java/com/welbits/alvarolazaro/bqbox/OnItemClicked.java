package com.welbits.alvarolazaro.bqbox;

import com.dropbox.client2.DropboxAPI;

public interface OnItemClicked {
    void onItemClicked(DropboxAPI.Entry entry, int position);
}

package com.welbits.alvarolazaro.bqbox;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.ButterKnife;

public class ListAdapter extends RecyclerView.Adapter {
    public DropboxAPI.Entry currentItem;
    List<DropboxAPI.Entry> items = new ArrayList<DropboxAPI.Entry>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(android.R.layout.simple_list_item_2, viewGroup, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), currentItem.fileName(), Toast.LENGTH_LONG).show();
            }
        });
        return new RecyclerView.ViewHolder(view){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        this.currentItem = items.get(position);

        TextView text1 = ButterKnife.findById(viewHolder.itemView, android.R.id.text1);
        if (!TextUtils.isEmpty(currentItem.fileName())) text1.setText(currentItem.fileName());

        TextView text2 = ButterKnife.findById(viewHolder.itemView, android.R.id.text2);
        if (!TextUtils.isEmpty(currentItem.size)) text2.setText(currentItem.size);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(Collection<? extends DropboxAPI.Entry> items) {
        int previous = getItemCount();
        this.items.addAll(items);
        notifyItemRangeInserted(previous, getItemCount());
    }
}

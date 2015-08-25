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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final OnItemClicked listener;
    private List<DropboxAPI.Entry> items = new ArrayList<DropboxAPI.Entry>();

    public ListAdapter(OnItemClicked listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(android.R.layout.simple_list_item_2, viewGroup, false), listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        DropboxAPI.Entry currentItem = items.get(position);

        viewHolder.setCurrentItem(currentItem);

        if (!TextUtils.isEmpty(currentItem.fileName()))
            viewHolder.text1.setText(currentItem.fileName());

        if (!TextUtils.isEmpty(currentItem.modified))
            viewHolder.text2.setText(currentItem.clientMtime);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(Collection<? extends DropboxAPI.Entry> items) {
        int previous = getItemCount();
        this.items.addAll(items);
        notifyItemRangeInserted(previous, items.size());
    }

    public void sort(Comparator<DropboxAPI.Entry> comparator) {
        Collections.sort(items, comparator);
        notifyDataSetChanged();
    }

    public void clear() {
        int previous = getItemCount();
        this.items.clear();
        notifyItemRangeRemoved(0, previous);
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(android.R.id.text1) public TextView text1;
    @Bind(android.R.id.text2) public TextView text2;

    private DropboxAPI.Entry currentItem;

    public ViewHolder(View itemView, final OnItemClicked listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(currentItem, getAdapterPosition());
            }
        });
    }

    public void setCurrentItem(DropboxAPI.Entry entry) {
        this.currentItem = entry;
    }
}
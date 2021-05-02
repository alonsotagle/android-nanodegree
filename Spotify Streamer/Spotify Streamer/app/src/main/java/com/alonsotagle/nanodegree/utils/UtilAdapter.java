package com.alonsotagle.nanodegree.utils;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlonsoTagle on 18/06/15.
 */
public abstract class UtilAdapter<T> extends BaseAdapter {

    private List<T> items;

    public UtilAdapter(List<T> items) {
        this.items = items;
    }

    public UtilAdapter() {
        this.items = new ArrayList<>();
    }

    public List<T> getItems() {
        return this.items;
    }

    public void clearData() {
        if (this.items != null) {
            this.items.clear();
            notifyDataSetChanged();
        }
    }

    public void setItems(List<T> adapterItems) {
        if (adapterItems != null) {
            clearData();
            this.items.addAll(adapterItems);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.indexOf(items.get(i));
    }
}

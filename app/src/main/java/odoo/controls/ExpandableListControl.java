/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 3/2/15 2:08 PM
 */
package odoo.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.odoo.core.orm.ODataRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ExpandableListControl extends LinearLayout
        implements ExpandableListOperationListener {
    public static final String TAG = ExpandableListControl.class.getSimpleName();
    private ExpandableListAdapter mAdapter;
    private Context context;

    public ExpandableListControl(Context context) {
        super(context);
        this.context = context;
    }

    public ExpandableListControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ExpandableListControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void onAdapterDataChange(List<Object> items) {
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            View view = mAdapter.getView(i, null, this);
            addView(view);
        }
    }

    public ExpandableListAdapter getAdapter(int resource, List<Object> objects,
                                            final ExpandableListAdapterGetViewListener listener) {
        mAdapter = new ExpandableListAdapter(context, resource, objects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(getResource(), parent, false);
                }
                if (listener != null) {
                    return listener.getView(position, convertView, parent);
                }
                return convertView;
            }
        };
        mAdapter.setOperationListener(this);
        return mAdapter;
    }


    public abstract static class ExpandableListAdapter {
        private List<Object> objects = new ArrayList<>();
        private Context context;
        private int resource = android.R.layout.simple_list_item_1;
        private ExpandableListOperationListener listener;

        public ExpandableListAdapter(Context context, int resource, List<Object> objects) {
            this.context = context;
            this.objects = objects;
            this.resource = resource;
        }

        public abstract View getView(int position, View convertView, ViewGroup parent);

        public void notifyDataSetChangedWithSort(List<Object> items) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                for (int j = 0; j < items.size() - 2; j++) {
                    for (int i = 0; i < items.size() - j - 1; i++) {
                        ODataRow row = (ODataRow) items.get(i);
                        Date arg0Date = format.parse(row.getString("seq_date"));
                        ODataRow row_1 = (ODataRow) items.get(i + 1);
                        Date arg1Date = format.parse(row_1.getString("seq_date"));
                        if (arg0Date.compareTo(arg1Date) == 1) {
                            items.remove(i);
                            items.add(i, row_1);
                            items.remove(i + 1);
                            items.add(i + 1, row);
                        }
                    }
                }
                objects = items;
                listener.onAdapterDataChange(items);
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }

        public void notifyDataSetChanged(List<Object> items) {
            objects = items;
            listener.onAdapterDataChange(items);
        }

        public Object getItem(int position) {
            return objects.get(position);
        }

        public void setOperationListener(ExpandableListOperationListener listener) {
            this.listener = listener;
        }

        public int getResource() {
            return resource;
        }
    }

    public static interface ExpandableListAdapterGetViewListener {
        public View getView(int position, View view, ViewGroup parent);
    }


}

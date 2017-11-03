package com.odoo.addons.picking;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.odoo.R;
import com.odoo.addons.stock.Models.Picking;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baaska on 6/29/17.
 */

public class ReceiptPickings extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ISyncStatusObserverListener, SwipeRefreshLayout.OnRefreshListener, OCursorListAdapter.OnViewBindListener, IOnSearchViewChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String KEY = ReceiptPickings.class.getSimpleName();
    private String mCurFilter = "";
    private String pickingType = "";
    private View mView;
    private OCursorListAdapter mAdapter = null;
    private boolean syncRequested = false;
    private Context mContext;
    private Toolbar toolbar;
    public FragmentManager manager;

    public ReceiptPickings() {
    }

    public ReceiptPickings(String pickingType, FragmentManager manager) {
        this.pickingType = pickingType;
        this.manager = manager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setHasSyncStatusObserver(KEY, this, db());
        mView = inflater.inflate(R.layout.picking_listview, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasSwipeRefreshView(view, R.id.swipe_container_picking, this);
        mView = view;
        mContext = this.getContext();
        ListView mListViewPicking = (ListView) view.findViewById(R.id.lw_picking);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.picking_row_item);
        mAdapter.setOnViewBindListener(this);
        mListViewPicking.setAdapter(mAdapter);
        mListViewPicking.setFastScrollAlwaysVisible(true);
        mListViewPicking.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStatusChange(Boolean changed) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        String state = "";
        switch (row.getString("state")) {
            case "draft":
                state = "Ноорог";
                break;
            case "cancel":
                state = "Цуцлагдсан";
                break;
            case "waiting":
                state = "Өөр үйлдлийг хүлээж буй";
                break;
            case "confirmed":
                state = "Бэлэн болохыг хүлээж буй";
                break;
            case "partially_available":
                state = "Зарим хэсэг нь бэлэн";
                break;
            case "assigned":
                state = "Шилжүүлэхэд бэлэн";
                break;
            case "done":
                state = "Шилжсэн";
                break;
        }
        OControls.setText(view, R.id.tvPickingOrigin, row.getString("name"));
        OControls.setText(view, R.id.tvPickingLocation, row.getString("partner_name"));
        OControls.setText(view, R.id.tvPickingPartner, row.getString("partner_name"));
        OControls.setText(view, R.id.tvPickingPO, row.getString("origin"));
        OControls.setText(view, R.id.tvPickingState, state);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = "";
        String order_by = "";
        String[] whereArgs = null;
        List<String> args = new ArrayList<>();
        if (pickingType.length() > 0) {
            where = " picking_type_id = ? and ";
            args.add(pickingType);
        }
        where += " origin like ? ";
        args.add("%" + mCurFilter + "%");
        order_by = " origin ASC";
        whereArgs = (args.size() > 0) ? args.toArray(new String[args.size()]) : null;
        return new CursorLoader(getActivity(), db().uri(), null, where, whereArgs, order_by);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container_picking);
                    OControls.setGone(mView, R.id.data_list_no_picking_item);
                    setHasSwipeRefreshView(mView, R.id.swipe_container_picking, ReceiptPickings.this);
                }
            }, 500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container_picking);
                    OControls.setVisible(mView, R.id.data_list_no_picking_item);
                    setHasSwipeRefreshView(mView, R.id.data_list_no_picking_item, ReceiptPickings.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_suppliers);
                    OControls.setText(mView, R.id.title, _s(R.string.label_no_receipt_picking_found));
                    OControls.setText(mView, R.id.subTitle, "");
                }
            }, 500);
            if (db().isEmptyTable() && !syncRequested) {
                syncRequested = true;
                onRefresh();
            }
        }
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(Picking.AUTHORITY);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        items.add(new ODrawerItem(KEY).setTitle("Хүргэлтийн захиалгууд")
                .setIcon(R.drawable.ic_action_suppliers)
                .setInstance(new ReceiptPickings("", null)));
        return items;
    }

    @Override
    public Class<Picking> database() {
        return Picking.class;
    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
        if (newFilter == null) {
            newFilter = "";
        }
        mCurFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onSearchViewClose() {

    }

    private void loadActivity(ODataRow row) {
        Bundle data = new Bundle();
        if (row != null) {
            data = row.getPrimaryBundleData();
        }
        IntentUtils.startActivity(getActivity(), ReceiptPickingDetails.class, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            StockPickings pickings = new StockPickings();
            FragmentManager fragmentManager = manager;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, pickings);
            fragmentTransaction.commit();
        } catch (Exception e) {
            Log.e(KEY, e.toString());
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_back, menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        loadActivity(row);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
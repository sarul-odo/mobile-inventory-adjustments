package com.odoo.addons.picking;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.picking.models.PartScrapPhotos;
import com.odoo.addons.picking.models.Picking;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.OResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baaska on 6/29/17.
 */

public class ReceiptPickings extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ISyncStatusObserverListener, SwipeRefreshLayout.OnRefreshListener, OCursorListAdapter.OnViewBindListener, IOnSearchViewChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String KEY = ReceiptPickings.class.getSimpleName();
    private String mCurFilter = null;
    private View mView;
    private OCursorListAdapter mAdapter = null;
    private boolean syncRequested = false;
    private Context mContext;

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
        mAdapter.setHasSectionIndexers(true, "name");
        mListViewPicking.setAdapter(mAdapter);
        mListViewPicking.setFastScrollAlwaysVisible(true);
        mListViewPicking.setOnItemClickListener(this);
        setHasFloatingButton(view, R.id.fabButton, mListViewPicking, this);
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

        OControls.setText(view, R.id.tvPickingOrigin, row.getString("origin").equals("-") ? "Илгээгдээгүй" : row.getString("origin"));
        OControls.setText(view, R.id.tvPickingLocation, row.getString("technic_name"));
        OControls.setText(view, R.id.tvPickingPartner, row.getString("date"));
        OControls.setText(view, R.id.tvPickingPO, row.getString("date"));
        OControls.setText(view, R.id.tvPickingState, state;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = "";
        String order_by = "";
        String[] whereArgs = null;
        List<String> args = new ArrayList<>();

        if (mCurFilter != null) {
            where += " origin like ? ";
            args.add("%" + mCurFilter + "%");
            order_by = "origin ASC";
        }

        where = (args.size() > 0) ? where : null;
        order_by = (args.size() > 0) ? order_by : null;
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
                    OControls.setGone(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.swipe_container_picking, ReceiptPickings.this);
                }
            }, 500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container_picking);
                    OControls.setVisible(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.data_list_no_item, ReceiptPickings.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
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
//            parent().sync().requestSync(Picking.AUTHORITY);
            OnPartScrapChangeUpdate onTireScrapChangeUpdate = new OnPartScrapChangeUpdate();
            ODomain d = new ODomain();
            /*swipe хийхэд бүх үзлэгийг update хйих*/
            onTireScrapChangeUpdate.execute(d);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG).show();
        }
    }

    private class OnPartScrapChangeUpdate extends AsyncTask<ODomain, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle(R.string.title_please_wait_mn);
            progressDialog.setMessage("Мэдээлэл шинэчилж байна.");
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progressDialog.setProgress(1);
            progressDialog.setMax(mAdapter.getCount());
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(ODomain... params) {
            if (inNetwork()) {
//                ODomain domain = params[0];
//                List<ODataRow> rows = scrapParts.select(null, "id = ?", new String[]{"0"});
//                List<ODataRow> photoRows = partScrapPhotos.select(null, "id = ?", new String[]{"0"});
//                for (ODataRow row : rows) {
//                    scrapParts.quickCreateRecord(row);
//                }
//                for (ODataRow row : photoRows) {
//                    partScrapPhotos.quickCreateRecord(row);
//                }
//                /*Бусад бичлэгүүдийг update хийж байна*/
//                scrapParts.quickSyncRecords(domain);
//                partScrapPhotos.quickSyncRecords(domain);
            } else {
                Toast.makeText(mContext, OResource.string(mContext, R.string.toast_network_required), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideRefreshingProgress();
            progressDialog.dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        items.add(new ODrawerItem(KEY).setTitle("Сэлбэг актлах хүсэлт")
                .setIcon(R.drawable.ic_action_suppliers)
                .setInstance(new ReceiptPickings()));
        return items;
    }

    @Override
    public Class<Picking> database() {
        return Picking.class;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_partners, menu);
        setHasSearchView(this, menu, R.id.menu_partner_search);
    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        loadActivity(row);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabButton:
                loadActivity(null);
                break;
        }
    }
}
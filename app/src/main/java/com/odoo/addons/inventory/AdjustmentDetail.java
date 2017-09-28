package com.odoo.addons.inventory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.inventory.models.StockInventory;
import com.odoo.addons.inventory.models.StockInventoryLine;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.OControls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.ExpandableListControl;
import odoo.controls.OForm;

import static com.odoo.core.utils.OAlert.showError;

/**
 * Created by ko on 9/7/17.
 */

public class AdjustmentDetail extends OdooCompatActivity implements View.OnClickListener {

    private OForm mForm;
    private Bundle extras;
    private Toolbar toolbar;
    private App app;
    private Boolean mEditMode = false;
    private ODataRow recordStockInventory = null;
    private List<ODataRow> recordStockInventoryLine = null;
    private StockInventory stockInventory;
    private StockInventoryLine stockInventoryLine;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<Object> objects = new ArrayList<Object>();
    private OdooMobile odooMobile;
    private List<Integer> stockInventoryLineIds = new ArrayList<Integer>();
//    private List<Integer> prdIds = new ArrayList<Integer>();
//    private AtomicIntegerArray prdId;
    private GetStockInventoryLineFromServer getStockInventoryLineFromServer;
    private static OdooResult response;
    private static List<ODataRow> result;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_adjustment_detail);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mForm = (OForm) findViewById(R.id.inventoryForm);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        if (toolbar != null)
//            toolbar.setTitle("");
        app = (App) getApplicationContext();
        stockInventory = new StockInventory(this, null);
        odooMobile = new OdooMobile(this, null);
        getStockInventoryLineFromServer = new GetStockInventoryLineFromServer();

        extras = getIntent().getExtras();
//        if (hasRecordInExtra())
//            extras.getString("");
        setupToolbar();
    }

    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey(OColumn.ROW_ID);
    }

    private void setupToolbar() {

        if (!hasRecordInExtra()) {
//            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            recordStockInventory = stockInventory.browse(rowId);
            if (recordStockInventory.size() > 0) {
                System.out.println(" \n___ static ___ " + result);
                getStockInventoryLineFromServer.execute();
                System.out.println(" \n___ statu ___ " + result);
//                recordStockInventoryLine = result;
            }
//            recordLine = record.getO2MRecord("line_ids").browseEach();
            checkControls();
//            setMode(mEditMode);
            mForm.setEditable(true);
            System.out.println(" ___ record ___ " + recordStockInventory);

//            initAdapter();
            mForm.initForm(recordStockInventory);
//            OControls.setText(view, R.id.company, row.getString("company"));
            collapsingToolbarLayout.setTitle(recordStockInventory.getString("name"));
        }
    }

    private void checkControls() {
//        findViewById(R.id.name).setOnClickListener(this);
//        findViewById(R.id.filter).setOnClickListener(this);
//        findViewById(R.id.exhausted).setOnClickListener(this);
//        findViewById(R.id.date).setOnClickListener(this);
//        findViewById(R.id.company).setOnClickListener(this);
//        findViewById(R.id.stockInventoryLine).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "------------------------ CLICK -------------------------");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        Log.e(TAG, "EACH __________ : " + result);
        try {
            mList = (ExpandableListControl) findViewById(R.id.line_ids);
            if (extras != null && recordStockInventory != null) {
                objects.addAll(result);
            } else {
                mList.setVisibility(View.VISIBLE);
            }
            mAdapter = mList.getAdapter(R.layout.stock_inventory_line_list, objects,
                    new ExpandableListControl.ExpandableListAdapterGetViewListener() {

                        @Override
                        public View getView(int position, View mView, ViewGroup parent) {
//                            Object row = mAdapter.getItem(position);
//                            LinkedTreeMap<String, Object> mapResult = result.getMapResult();
                            LinkedTreeMap<String, Object> row = (LinkedTreeMap<String, Object>) mAdapter.getItem(position);
                            Log.d(TAG, "row : " + row);
                            OControls.setText(mView, R.id.edtName, "["+ row.get("product_code") +"] "+ row.get("product_name"));
                            OControls.setText(mView, R.id.productId, row.getString("product_id").charAt(1));
                            OControls.setText(mView, R.id.thoereticalQty, String.format("%.2f", row.get("theoretical_qty")));
                            OControls.setText(mView, R.id.productQty, String.format("%.2f", row.get("product_qty")));
//                            OControls.setText(mView, R.id.ean13, String.format("%.2f", row.getFloat("ean13")));
                            return mView;
                        }
                    });
            mAdapter.notifyDataSetChanged(objects);
        } catch (Exception ex) {
            showError(this, "Error" + ex.toString());
            Log.e(TAG, "ERROR", ex);
        }
    }

    private class GetStockInventoryLineFromServer extends AsyncTask<ODataRow, Void, Void> {
        private List<ODataRow> aa = new ArrayList<>();
        @Override
        protected Void doInBackground(ODataRow... params) {
            System.out.println(" _______ doInBackground ______________ ");
            Odoo odoo;
            stockInventoryLineIds.add(recordStockInventory.getInt("id"));
            try {
                odoo = odooMobile.getServerDataHelper().getOdoo();
                Log.d(TAG, " ODOO : " + stockInventoryLineIds + odoo + recordStockInventory.getInt("id"));
                if (odoo != null) {
                    OArguments args = new OArguments();
                    args.add(new JSONObject());
                    args.add(new JSONArray(stockInventoryLineIds));
                    response = odoo.callMethod(odooMobile.getModelName(), "getStockInventoryLine", args, null);
                    aa = response.getArray("result");
                    Log.d(TAG, "\n RESPONSE : " + result);
                } else {
                    Context context = getApplicationContext();
                    int text = R.string.toast_check_internet_connection;
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            result = aa;
            initAdapter();
//            ActionInProgress(false);
//            getLoaderManager().restartLoader(0, null, SaleReturnDetail.this);
        }
    }

}

//ServerDataHelper helper = getServerDataHelper();
//OArguments oArguments = new OArguments();
//oArguments.add(new JSONArray().put(2));
//Object billno = helper.callMethod("get_order_no", oArguments);

//    @Override
//    protected Void doInBackground(ODataRow... params) {
//        Odoo odoo;
//        OdooResult result = null;
//        try {
//            odoo = odooMobile.getServerDataHelper().getOdoo();
//            Log.d(TAG, " ODOO :" + odoo + prdIds);
//            if (odoo != null) {
//                OArguments args = new OArguments();
//                args.add(new JSONObject());
//                args.add(new JSONArray(prdIds));
//                result = odoo.callMethod(odooMobile.getModelName(), "getProduct", args, null);
//                Log.d(TAG, " RESULT :" + result);
//            } else {
//                Context context = getApplicationContext();
//                int text = R.string.toast_check_internet_connection;
//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (result == null) {
//            Toast.makeText(getApplicationContext(), "Баримт хэвлэхэд алдаа гарлаа", Toast.LENGTH_LONG);
//        }
//        return null;
//    }
//    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
//    }
//}
package com.odoo.addons.inventory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.addons.inventory.models.ProductTemplate;
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
import java.util.concurrent.atomic.AtomicIntegerArray;

import odoo.controls.ExpandableListControl;
import odoo.controls.OForm;

/**
 * Created by ko on 9/7/17.
 */

public class AdjustmentDetail extends OdooCompatActivity implements View.OnClickListener {

    private OForm mForm;
    private Bundle extras;
    private Toolbar toolbar;
    private App app;
    private Boolean mEditMode = false;
    private ODataRow record = null;
    private ODataRow productPrd = null;
    private ODataRow productTmpl = null;
    private List<ODataRow> recordLine = null;
    private StockInventory stockInventory;
    private StockInventoryLine stockInventoryLine;
    private ProductProduct productProduct;
    private ProductTemplate productTemplate;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<Object> objects = new ArrayList<Object>();
    private OdooMobile odooMobile;
    private List<Integer> prdIds = new ArrayList<Integer>();
    private GetProductFromServer getProductFromServer;


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
        stockInventoryLine = new StockInventoryLine(this, null);
        odooMobile = new OdooMobile(this, null);
//        productProduct = new ProductProduct(this, null);
//        productTemplate = new ProductTemplate(this, null);

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
            record = stockInventory.browse(rowId);
            recordLine = record.getO2MRecord("line_ids").browseEach();
            checkControls();
//            setMode(mEditMode);
            mForm.setEditable(true);
            System.out.println(" ___ record ___ " + record);
            System.out.println(" ___ recordLINE ___ " + recordLine);

            initAdapter();
            mForm.initForm(record);
//            OControls.setText(view, R.id.company, row.getString("company"));
            collapsingToolbarLayout.setTitle(record.getString("name"));
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
//        getDatasFromServer();
//        LiveSearchProduct liveSearchProduct = new LiveSearchProduct();
//        liveSearchProduct.execute();
        try {
            mList = (ExpandableListControl) findViewById(R.id.line_ids);
//            mList.setVisibility(View.VISIBLE);
            if (extras != null && record != null) {
//                int[] prdIds = new int[3];
//                int[] myIntArray = {};
//                int[] myIntArray = new int[]{1,2,3};

                AtomicIntegerArray prdId;
//                List<ODataRow> lines = stockInventoryLine.select(null, "inventory = ?", new String[]{String.valueOf(record.getInt("id"))});
                if (recordLine.size() > 0) {
                    for (ODataRow line : recordLine) {
                        ODataRow m2o = line.getM2ORecord("product_id").browse();
                        prdIds.add(line.getInt("product_id"));
//                        odooMobile = new OdooMobile(this, null);
//                        getDatasFromServer();
//                        if (prdId != 0) {
                        System.out.println(" ___ TEST 000 ___" + "\n --- /// " + line);
                        System.out.println(" ___ TEST 001 ___" + "\n --- /// " + m2o);
                        line.put("productName", m2o.getString("name"));
//                            lineIds.put(product + "", line.getInt("id"));
                        System.out.println(" ___ TEST 002 ___" + "\n --- /// " + line);

//                        }
                    }
                    if (prdIds.size() > 0) {
                        GetProductFromServer getProductFromServer = new GetProductFromServer();
                        getProductFromServer.execute();
                    }
                    System.out.println(" ___ INT IDS ___" + "\n --- /// " + prdIds + prdIds.size());
                    objects.addAll(recordLine);
                }
            }
            mAdapter = mList.getAdapter(R.layout.stock_inventory_line_list, objects,
                    new ExpandableListControl.ExpandableListAdapterGetViewListener() {

                        @Override
                        public View getView(int position, View mView, ViewGroup parent) {
                            ODataRow row = (ODataRow) mAdapter.getItem(position);

                            Log.d(TAG, "row : " + row);
                            OControls.setText(mView, R.id.edtName, row.getString("productName"));
                            OControls.setText(mView, R.id.productId, row.getString("product_id"));
                            OControls.setText(mView, R.id.thoereticalQty, String.format("%.2f", row.getFloat("theoretical_qty")));
                            OControls.setText(mView, R.id.productQty, String.format("%.2f", row.getFloat("product_qty")));
//                            OControls.setText(mView, R.id.ean13, String.format("%.2f", row.getFloat("ean13")));
                            return mView;
                        }
                    });
            mAdapter.notifyDataSetChanged(objects);
//            System.out.println(" ______ SYS ______" + objects);
        } catch (Exception ex) {
//            showError(this, R.string.str_error + ex.toString());
            Log.e(TAG, "ERROR", ex);
        }
    }


    private class GetProductFromServer extends AsyncTask<ODataRow, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
//            ActionInProgress(true);
        }

        @Override
        protected Void doInBackground(ODataRow... params) {
            Odoo odoo;
            try {
                odoo = odooMobile.getServerDataHelper().getOdoo();
                Log.d(TAG, " ODOO :" + odoo + prdIds);
                if (odoo != null) {
                    OArguments args = new OArguments();
                    args.add(new JSONObject());
                    args.add(new JSONArray(prdIds));
                    OdooResult result = odoo.callMethod(odooMobile.getModelName(), "getProduct", args, null);
                    Log.d(TAG, " RESULT :" + result);
                } else {
//                    Toast.makeText(this, "HOlbogdoj chadahgui bnAP KOI", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}

//ServerDataHelper helper = getServerDataHelper();
//OArguments oArguments = new OArguments();
//oArguments.add(new JSONArray().put(2));
//Object billno = helper.callMethod("get_order_no", oArguments);
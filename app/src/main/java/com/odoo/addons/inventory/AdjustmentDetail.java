package com.odoo.addons.inventory;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.OControls;

import java.util.ArrayList;
import java.util.List;

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
        productProduct = new ProductProduct(this, null);
        productTemplate = new ProductTemplate(this, null);

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

    }

    private void initAdapter() {
        try {
            mList = (ExpandableListControl) findViewById(R.id.line_ids);
//            mList.setVisibility(View.VISIBLE);
            if (extras != null && record != null) {
//                List<ODataRow> lines = stockInventoryLine.select(null, "inventory = ?", new String[]{String.valueOf(record.getInt("id"))});
                if (recordLine.size() > 0) {
                    for (ODataRow line : recordLine) {
                        System.out.println(" ___ TEST 001 ___" + line +"///"+ line.getInt("product_id"));

                        productPrd = productProduct.browse(line.getInt("product_id"));
                        System.out.println(" ___ TEST 002 ___" + productPrd +"///"+ productPrd.getInt("product_tmpl_id"));
                        productTmpl = productTemplate.browse(productPrd.getInt("product_tmpl_id"));
                        System.out.println(" ___ TEST 003 ___" + productTmpl);
//                        if (productTmpl.size() > 0) {
//                            line.put("productName", line.getString("name"));
//                            lineIds.put(product + "", line.getInt("id"));
//                        }
                    }
                    objects.addAll(recordLine);
                }
            }
            mAdapter = mList.getAdapter(R.layout.stock_inventory_line_list, objects,
                    new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                        @Override
                        public View getView(int position, View mView, ViewGroup parent) {
                            ODataRow row = (ODataRow) mAdapter.getItem(position);

                            System.out.println(" ___ TEST 222 ___" + row);

                            Log.d(TAG, "row : " + row);
                            OControls.setText(mView, R.id.edtName, row.getString("name"));
                            OControls.setText(mView, R.id.productId, row.getString("product_id"));
                            OControls.setText(mView, R.id.thoereticalQty, String.format("%.2f", row.getFloat("theoretical_qty")));
                            OControls.setText(mView, R.id.productQty, String.format("%.2f", row.getFloat("product_qty")));
//                            OControls.setText(mView, R.id.ean13, String.format("%.2f", row.getFloat("ean13")));
                            return mView;
                        }
                    });
            mAdapter.notifyDataSetChanged(objects);
            System.out.println(" ______ SYS ______" + objects);
        } catch (Exception ex) {
//            showError(this, R.string.str_error + ex.toString());
            Log.e(TAG, "ERROR", ex);
        }
    }
}
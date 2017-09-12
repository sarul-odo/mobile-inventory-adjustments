package com.odoo.addons.inventory;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.inventory.models.StockInventory;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;

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
    private StockInventory stockInventory;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_adjustment_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (App) getApplicationContext();
        stockInventory = new StockInventory(this, null);
        extras = getIntent().getExtras();
//        if (hasRecordInExtra())
//            extras.getString("");
        setupToolbar();
    }

    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey(OColumn.ROW_ID);
    }

    private void setupToolbar() {
        System.out.println(" 111 my 1" + extras);
        mForm = (OForm) findViewById(R.id.inventoryForm);

        if (!hasRecordInExtra()) {
//            setMode(mEditMode);
//            userImage.setColorFilter(Color.parseColor("#ffffff"));
//            userImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = stockInventory.browse(rowId);

            System.out.println(" 111 rowId " + rowId + "\n+___rw " );
            System.out.println(" 111 record " + record + "+___rw " );
            System.out.println(" 111 nbnme " + record.getString("name"));

            checkControls();

//            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
//            collapsingToolbarLayout.setTitle(record.getString("name"));

        }
    }

    private void checkControls() {
        System.out.println(" 111 checkControls ");

        findViewById(R.id.name).setOnClickListener(this);
        findViewById(R.id.filter).setOnClickListener(this);
        findViewById(R.id.date).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
package com.odoo.addons.inventory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.customers.utils.ShareUtil;
import com.odoo.addons.inventory.models.StockInventory;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.OStringColorUtil;

import java.util.List;

import odoo.controls.OField;
import odoo.controls.OForm;

/**
 * Created by ko on 10/5/17.
 */

public class AdjustmentDetails extends OdooCompatActivity
        implements View.OnClickListener,
        OField.IOnFieldValueChangeListener {

    public static final String TAG = Adjustments.class.getSimpleName();

    private Bundle extras;
    private StockInventory stockInventory;
    private ODataRow record = null;
    private ImageView userImage = null;
    private OForm mForm;
    private App app;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private OFileManager fileManager;
    private String newImage = null;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private Context mContext;
    private OnStockInventoryChangeUpdate onStockInventoryChangeUpdate;
//    private Adjustments.Type partnerType = Adjustments.Type.Customer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_inventory_detail);

        mContext = getApplicationContext();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.stock_inventory_collapsing_toolbar);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fileManager = new OFileManager(this);
        if (toolbar != null)
            collapsingToolbarLayout.setTitle("New");
        if (savedInstanceState != null) {
            mEditMode = true;
//            newImage = savedInstanceState.getString(KEY_NEW_IMAGE);
        }
        app = (App) getApplicationContext();
        stockInventory = new StockInventory(this, null);
        extras = getIntent().getExtras();

        if (!hasRecordInExtra())
            mEditMode = true;
        setupToolbar();

    }

    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey(OColumn.ROW_ID);
    }

    private void setMode(Boolean edit) {
//        findViewById(R.id.captureImage).setVisibility(edit ? View.VISIBLE : View.GONE);
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_stock_inventory_detail_more).setVisible(!edit);
            mMenu.findItem(R.id.menu_stock_inventory_edit).setVisible(!edit);
            mMenu.findItem(R.id.menu_stock_inventory_save).setVisible(edit);
            mMenu.findItem(R.id.menu_stock_inventory_cancel).setVisible(edit);
        }
        int color = Color.DKGRAY;
        if (record != null) {
            color = OStringColorUtil.getStringColor(this, record.getString("name"));
        }
        if (edit) {
            if (!hasRecordInExtra()) {
                collapsingToolbarLayout.setTitle("New");
            }
            mForm = (OForm) findViewById(R.id.stockInventoryFormEdit);
            findViewById(R.id.stock_inventory_view_layout).setVisibility(View.GONE);
            findViewById(R.id.stock_inventory_edit_layout).setVisibility(View.VISIBLE);
//            OField is_company = (OField) findViewById(R.id.is_company_edit);
//            is_company.setOnValueChangeListener(this);
        } else {
            mForm = (OForm) findViewById(R.id.stockInventoryForm);
            findViewById(R.id.stock_inventory_edit_layout).setVisibility(View.GONE);
            findViewById(R.id.stock_inventory_view_layout).setVisibility(View.VISIBLE);
        }
        setColor(color);
    }

    private void setupToolbar() {
        if (!hasRecordInExtra()) {
            setMode(mEditMode);
//            userImage.setColorFilter(Color.parseColor("#ffffff"));
//            userImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = stockInventory.browse(rowId);
//            record.put("full_address", resPartner.getAddress(record));
            checkControls();
            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
            collapsingToolbarLayout.setTitle(record.getString("name"));
//            setCustomerImage();
//            if (record.getInt("id") != 0 && record.getString("large_image").equals("false")) {
//                CustomerDetails.BigImageLoader bigImageLoader = new AdjustmentDetails.BigImageLoader();
//                bigImageLoader.execute(record.getInt("id"));
//            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.full_address:
                IntentUtils.redirectToMap(this, record.getString("full_address"));
                break;
            case R.id.website:
                IntentUtils.openURLInBrowser(this, record.getString("website"));
                break;
            case R.id.email:
                IntentUtils.requestMessage(this, record.getString("email"));
                break;
            case R.id.phone_number:
                IntentUtils.requestCall(this, record.getString("phone"));
                break;
            case R.id.mobile_number:
                IntentUtils.requestCall(this, record.getString("mobile"));
                break;
            case R.id.captureImage:
                fileManager.requestForFile(OFileManager.RequestType.IMAGE_OR_CAPTURE_IMAGE);
                break;
        }
    }

    private void checkControls() {
        findViewById(R.id.name).setOnClickListener(this);
//        findViewById(R.id.website).setOnClickListener(this);
//        findViewById(R.id.email).setOnClickListener(this);
//        findViewById(R.id.phone_number).setOnClickListener(this);
//        findViewById(R.id.mobile_number).setOnClickListener(this);
    }

//    private void setCustomerImage() {
//
//        if (record != null && !record.getString("image_small").equals("false")) {
//            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            String base64 = newImage;
//            if (newImage == null) {
//                if (!record.getString("large_image").equals("false")) {
//                    base64 = record.getString("large_image");
//                } else {
//                    base64 = record.getString("image_small");
//                }
//            }
//            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, base64));
//        } else {
//            userImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            userImage.setColorFilter(Color.WHITE);
//            int color = OStringColorUtil.getStringColor(this, record.getString("name"));
//            userImage.setBackgroundColor(color);
//        }
//    }

    private void setColor(int color) {
        mForm.setIconTintColor(Color.RED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onStockInventoryChangeUpdate = new OnStockInventoryChangeUpdate();
        ODomain domain = new ODomain();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_stock_inventory_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    if (record != null) {
                        stockInventory.update(record.getInt(OColumn.ROW_ID), values);
                        onStockInventoryChangeUpdate.execute(domain);
                        Toast.makeText(this, R.string.toast_information_saved, Toast.LENGTH_LONG).show();
                        mEditMode = !mEditMode;
                        setupToolbar();
                    } else {
                        final int row_id = stockInventory.insert(values);
                        if (row_id != OModel.INVALID_ROW_ID) {
                            onStockInventoryChangeUpdate.execute(domain);
                            Toast.makeText(this, R.string.stock_inventory_created, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
            case R.id.menu_stock_inventory_cancel:
            case R.id.menu_stock_inventory_edit:
                if (hasRecordInExtra()) {
                    mEditMode = !mEditMode;
                    setMode(mEditMode);
                    mForm.setEditable(mEditMode);
                    mForm.initForm(record);
//                    setCustomerImage();
                } else {
                    finish();
                }
                break;
            case R.id.menu_stock_inventory_share:
                ShareUtil.shareContact(this, record, true);
                break;
            case R.id.menu_stock_inventory_import:
                ShareUtil.shareContact(this, record, false);
                break;
            case R.id.menu_stock_inventory_delete:
                OAlert.showConfirm(this, OResource.string(this,
                        R.string.confirm_are_you_sure_want_to_delete),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    // Deleting record and finishing activity if success.
                                    if (stockInventory.delete(record.getInt(OColumn.ROW_ID))) {
                                        Toast.makeText(AdjustmentDetails.this, R.string.toast_record_deleted,
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }
                        });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stock_inventory_detail, menu);
        mMenu = menu;
        setMode(mEditMode);
        return true;
    }

//    @Override
//    public void onFieldValueChange(OField field, Object value) {
//        if (field.getFieldName().equals("is_company")) {
//            Boolean checked = Boolean.parseBoolean(value.toString());
//            int view = (checked) ? View.GONE : View.VISIBLE;
//            findViewById(R.id.parent_id).setVisibility(view);
//        }
//    }

//    private class BigImageLoader extends AsyncTask<Integer, Void, String> {

//        @Override
//        protected String doInBackground(Integer... params) {
//            String image = null;
//            try {
//                Thread.sleep(300);
//                OdooFields fields = new OdooFields();
//                fields.addAll(new String[]{"image_medium"});
//                OdooResult record = resPartner.getServerDataHelper().read(null, params[0]);
//                if (record != null && !record.getString("image_medium").equals("false")) {
//                    image = record.getString("image_medium");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return image;
//        }

//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            if (result != null) {
//                if (!result.equals("false")) {
//                    OValues values = new OValues();
//                    values.put("large_image", result);
//                    resPartner.update(record.getInt(OColumn.ROW_ID), values);
//                    record.put("large_image", result);
////                    setCustomerImage();
//                }
//            }
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(String.valueOf(false), mEditMode);
//        outState.putString(KEY_NEW_IMAGE, newImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OValues values = fileManager.handleResult(requestCode, resultCode, data);
        if (values != null && !values.contains("size_limit_exceed")) {
//            newImage = values.getString("datas");
//            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            userImage.setColorFilter(null);
//            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, newImage));
        } else if (values != null) {
            Toast.makeText(this, R.string.toast_image_size_too_large, Toast.LENGTH_LONG).show();
        }
    }

    private class OnStockInventoryChangeUpdate extends AsyncTask<ODomain, Void, Void> {

        @Override
        protected Void doInBackground(ODomain... params) {
            if (app.inNetwork()) {
                ODomain domain = params[0];
                List<ODataRow> rows = stockInventory.select(null, "id = ?", new String[]{"0"});
                for (ODataRow row : rows) {
                    stockInventory.quickCreateRecord(row);
                }
            /*Бусад бичлэгүүдийг update хийж байна*/
                stockInventory.quickSyncRecords(domain);
                stockInventory.quickSyncRecords(domain);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!app.inNetwork())
                Toast.makeText(mContext, OResource.string(mContext, R.string.toast_network_required), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFieldValueChange(OField field, Object value) {
//        if (record == null && field.getFieldName().equals("technic_id")) {
//            ODataRow techVal = (ODataRow) value;
//            technicSync(techVal.getString("id"));

        }
}
package com.odoo.addons.picking;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.addons.stock.Models.PackOperation;
import com.odoo.addons.stock.Models.Picking;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.ServerDataHelper;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OResource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import odoo.controls.ExpandableListControl;
import odoo.controls.OField;
import odoo.controls.OForm;

/**
 * Created by baaska on 5/30/17.
 */

public class ReceiptPickingDetails extends OdooCompatActivity implements OField.IOnFieldValueChangeListener, View.OnClickListener {

    public static final String TAG = ReceiptPickingDetails.class.getSimpleName();
    private Bundle extra;
    private OForm mForm;
    private EditText scanCode;
    private ODataRow record = null;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private Picking picking;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<ODataRow> scrapPartLines = new ArrayList<>();
    private List<ODataRow> partRow = new ArrayList<>();
    private Toolbar toolbar;
    private LinearLayout layoutAddItem = null;
    private Context mContext;
    App app;
    /*Зүйлс оруулж ирэх*/
    private HashMap<String, Boolean> toWizardTechParts = new HashMap<>();
    private List<Object> objects = new ArrayList<>();
    public static final int REQUEST_ADD_ITEMS = 323;
    private List<ODataRow> lines = new ArrayList<>();

    private ProductProduct products = null;
    private Button btnTransfer;
    private OField state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picking_detail);

        extra = getIntent().getExtras();
        app = (App) getApplicationContext();
        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.toolbarPicking);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditMode = (!hasRecordInExtra() ? true : false);
        picking = new Picking(this, null);
        products = new ProductProduct(this, null);
        mForm = (OForm) findViewById(R.id.OFormPicking);

        layoutAddItem = (LinearLayout) findViewById(R.id.layoutAddItem);
        scanCode = (EditText) findViewById(R.id.scanCode);
        btnTransfer = (Button) findViewById(R.id.btnDoNewTransfer);
        state = (OField) findViewById(R.id.StatePicking);
        layoutAddItem.setOnClickListener(this);
        setupToolbar();
        initAdapter();
    }

    private void ToolbarMenuSetVisibl(Boolean Visibility) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_picking_edit).setVisible(!Visibility);
            mMenu.findItem(R.id.menu_packeg_sync).setVisible(!Visibility);
            mMenu.findItem(R.id.menu_picking_save).setVisible(Visibility);
            mMenu.findItem(R.id.menu_picking_cancel).setVisible(Visibility);
        }
    }

    private void setMode(Boolean edit) {
        ToolbarMenuSetVisibl(edit);
        scanCode.setVisibility(View.GONE);
        scanCode.setText(null);
        btnTransfer.setVisibility(View.GONE);
        if (edit) {
            if (state.getValue().equals("assigned")) {
                btnTransfer.setVisibility(View.VISIBLE);
                btnTransfer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasRecordInExtra()) {
                            DoNewTransfer doNewTransfer = new DoNewTransfer();
                            if (app.inNetwork()) {
                                doNewTransfer.execute();
                                mEditMode = !mEditMode;
                                setupToolbar();
                                initAdapter();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }

            scanCode.setVisibility(View.VISIBLE);
            scanCode.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        String ean13 = scanCode.getText().toString();
                        if (productQtyChange(ean13)) {
                            scanCode.setText(null);
                            mAdapter.notifyDataSetChanged(objects);
                            return true;
                        } else if (scanCode.getText().length() > 0) {
                            Toast.makeText(getApplicationContext(), "(" + ean13 + ")" + "\nУг бараа мөрөнд байхгүй байна!!!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private boolean productQtyChange(String ean13) {
        for (int i = 0; i < objects.size(); i++) {
            ODataRow packLine = (ODataRow) objects.get(i);
            String code = products.browse(packLine.getInt("product_id")).getString("barcode");
            float qty = packLine.getFloat("qty_done");
            boolean swich = packLine.getBoolean("swicher");
            if (code.equals(ean13)) {
                packLine.put("qty_done", swich ? qty + 1 : qty - 1);
                objects.remove(i);
                objects.add(i, packLine);
                return true;
            }
        }
        return false;
    }

    private void setupToolbar() {
        if (!hasRecordInExtra()) {
            setTitle("Үүсгэх");
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int pickingId = extra.getInt(OColumn.ROW_ID);
            record = picking.browse(pickingId);
            setTitle(record.getString("name"));
            mForm.initForm(record);
            mForm.setEditable(mEditMode);
        }
        setMode(mEditMode);
    }

    private void initAdapter() {
        mList = (ExpandableListControl) findViewById(R.id.ExpandListProductLine);
        mList.setVisibility(View.VISIBLE);
        if (extra != null && record != null) {
            lines.clear();
            for (ODataRow line : record.getO2MRecord("pack_operation_product_ids").browseEach()) {
                line.put("swicher", true);
                lines.add(line);
            }
            objects.clear();
            objects.addAll(lines);
        }
        mAdapter = mList.getAdapter(R.layout.pickng_pack_operation_line_item, objects,
                new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                    @Override
                    public View getView(final int position, View mView, ViewGroup parent) {
                        final ODataRow row = (ODataRow) mAdapter.getItem(position);
                        final EditText edtProductQtyDone = (EditText) mView.findViewById(R.id.edtProductQtyDone);
                        edtProductQtyDone.setFocusable(mEditMode);
                        final SwitchCompat switchCompat = (SwitchCompat) mView.findViewById(R.id.switchButton);
                        switchCompat.setChecked(row.getBoolean("swicher"));
                        if (mEditMode) {
                            switchCompat.setVisibility(View.VISIBLE);
                        }
                        OControls.setText(mView, R.id.edtName, row.getString("product_name"));
                        OControls.setText(mView, R.id.edtName, row.getString("product_name"));
                        OControls.setText(mView, R.id.edtProductUom, row.getString("product_uom_name"));
                        OControls.setText(mView, R.id.edtProductOrderQty, row.getString("ordered_qty"));
                        OControls.setText(mView, R.id.edtProductQty, row.getString("product_qty"));
                        edtProductQtyDone.setText(row.getString("qty_done"));

                        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                row.put("swicher", isChecked);
                                lines.remove(position);
                                lines.add(position, row);
                            }
                        });

                        edtProductQtyDone.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                row.put("qty_done", edtProductQtyDone.getText());
                                lines.remove(position);
                                lines.add(position, row);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        return mView;
                    }
                });
        mAdapter.notifyDataSetChanged(objects);
    }

    private class ActionAssign extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private String warning = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ReceiptPickingDetails.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.title_please_wait);
            progressDialog.setMessage(OResource.string(ReceiptPickingDetails.this, R.string.pack_operation_sync));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<ODataRow> items = new ArrayList<>();
            try {
                ServerDataHelper helper = picking.getServerDataHelper();
                OArguments arguments = new OArguments();
                arguments.add(record.getInt("id"));
                helper.callMethod("action_assign", arguments);
                picking.quickCreateRecord(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setupToolbar();
            initAdapter();
            progressDialog.dismiss();
        }
    }

    private class DoNewTransfer extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ReceiptPickingDetails.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.title_please_wait);
            progressDialog.setMessage(OResource.string(ReceiptPickingDetails.this, R.string.picking_transfer));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<ODataRow> items = new ArrayList<>();
            try {
                ServerDataHelper helper = picking.getServerDataHelper();
                OArguments arguments = new OArguments();
                arguments.add(record.getInt("id"));
                Object response = helper.callMethod("do_new_transfer", arguments);
                if (response.equals(true)) {
                    picking.delete(record.getInt("_id"));
                } else {
                    OModel wizard = new OModel(getApplicationContext(), "stock.backorder.confirmation", picking.getUser());
                    ServerDataHelper immediateTransfer = new ServerDataHelper(getApplicationContext(), wizard, picking.getUser());
                    OArguments argumentss = new OArguments();
                    argumentss.add(new JSONArray().put(record.getInt("id")));
                    argumentss.add(new JSONObject());

                    HashMap<String, Object> args = new HashMap<>();
                    args.put("pick_id", record.getInt("id"));
                    Object aa = immediateTransfer.callMethod("create", argumentss, args);
                    Object wizResponse = immediateTransfer.callMethod("process", argumentss);
                    if (response.equals(true)) {
                        picking.delete(record.getInt("_id"));
                    }
                }
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setupToolbar();
            initAdapter();
            progressDialog.dismiss();
        }
    }

    private boolean hasRecordInExtra() {
        return extra != null && extra.containsKey(OColumn.ROW_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picking_detail, menu);
        mMenu = menu;
        ToolbarMenuSetVisibl(mEditMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_picking_save:
                if (app.inNetwork()) {
                    OnPackOperationUpdate onPackOperationUpdate = new OnPackOperationUpdate();
                    mEditMode = !mEditMode;
                    mAdapter.notifyDataSetChanged(objects);
                    onPackOperationUpdate.execute();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_picking_cancel:
                OAlert.showConfirm(this, OResource.string(this, R.string.close_activity),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    mEditMode = !mEditMode;
                                    setupToolbar();
                                    initAdapter();
                                } else {
                                    mForm.setEditable(true);
                                    setMode(mEditMode);
                                }
                            }
                        });
                break;
            case R.id.menu_picking_edit:
                if (hasRecordInExtra()) {
                    mEditMode = !mEditMode;
//                    mForm.setEditable(mEditMode);
                    setMode(mEditMode);
                    initAdapter();
                    scanCode.requestFocus();
                }
                break;
            case R.id.menu_packeg_sync:
                if (hasRecordInExtra()) {
                    ActionAssign actionAssign = new ActionAssign();
                    if (app.inNetwork()) {
                        actionAssign.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onFieldValueChange(OField field, Object value) {

    }

    @Override
    public void finish() {
        if (mEditMode) {
            OAlert.showConfirm(this, OResource.string(this, R.string.close_activity),
                    new OAlert.OnAlertConfirmListener() {
                        @Override
                        public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                            if (type == OAlert.ConfirmType.POSITIVE) {
                                mEditMode = !mEditMode;
                                finish();
                            }
                        }
                    });
        } else {
            super.finish();
        }
    }

    private class OnPackOperationUpdate extends AsyncTask<OValues, Void, Boolean> {

        private ProgressDialog mDialog;
        PackOperation packOperation = new PackOperation(getApplicationContext(), null);
        String productName = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(ReceiptPickingDetails.this);
            mDialog.setTitle(R.string.title_working);
            mDialog.setMessage("Мөрүүдийг шинэчилж байна...");
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(OValues... params) {
            try {
                Thread.sleep(500);
                for (Object line : objects) {
                    ODataRow row = (ODataRow) line;
                    float qtyDone = row.getFloat("qty_done");
                    if (qtyDone <= 0) {
                        productName = row.getString("product_name");
                        break;
                    }
                }
                if (productName.length() > 0) {
                    return false;
                }
                for (Object line : objects) {
                    ODataRow dRow = (ODataRow) line;
                    ORecordValues oRow = new ORecordValues();
                    try {
                        for (String key : dRow.keys()) {
                            if (key.equals("qty_done")) {
                                oRow.put(key, dRow.getFloat(key));
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    packOperation.getServerDataHelper().updateOnServer(oRow, dRow.getInt("id"));
                }
                picking.quickCreateRecord(record);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            mDialog.dismiss();
            if (success) {
                Toast.makeText(ReceiptPickingDetails.this, "Мэдээлэл амжилттай хадгалагдлаа", Toast.LENGTH_LONG).show();
                setupToolbar();
                initAdapter();
            } else if (productName.length() > 0) {
                Toast.makeText(ReceiptPickingDetails.this, productName + "\nБарааны ТОО ХЭМЖЭЭГ оруулана уу!!!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
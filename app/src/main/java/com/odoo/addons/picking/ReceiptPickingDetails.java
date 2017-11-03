package com.odoo.addons.picking;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private PackOperation packOperation;
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
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picking_detail);

        extra = getIntent().getExtras();
        app = (App) getApplicationContext();
        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.toolbarPicking);
        ScrollView sv = (ScrollView) findViewById(R.id.scroll);
        sv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return true;
                }
                return false;
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mEditMode = (!hasRecordInExtra() ? true : false);
        picking = new Picking(this, null);
        packOperation = new PackOperation(this, null);
        products = new ProductProduct(this, null);
        mForm = (OForm) findViewById(R.id.OFormPicking);

        layoutAddItem = (LinearLayout) findViewById(R.id.layoutAddItem);
        scanCode = (EditText) findViewById(R.id.scanCode);
        btnTransfer = (Button) findViewById(R.id.btnDoNewTransfer);
        state = (OField) findViewById(R.id.StatePicking);
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
                btnTransfer.setOnClickListener(this);
            }

            scanCode.setVisibility(View.VISIBLE);
            scanCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String txt = scanCode.getText().toString();
                    mAdapter.notifyDataSetChangedWithSort(search(txt));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            scanCode.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        String ean13 = scanCode.getText().toString();
                        if (true) {
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

    private List<Object> search(String code) {

        if (code.isEmpty()) {
            return objects;
        } else {
            List<Object> searchObject = new ArrayList<>();
            try {
                code = code.toLowerCase();

                for (int i = 0; i < objects.size(); i++) {
                    ODataRow packLine = (ODataRow) objects.get(i);
//                float qty = packLine.getFloat("qty_done");
//                boolean swich = packLine.getBoolean("swicher");
                    String ean13 = packLine.getString("barcode").toLowerCase();
                    String name = packLine.getString("product_name").toLowerCase();
                    if (ean13.contains(code) || name.contains(code)) {
                        searchObject.add(packLine);
//                packLine.put("qty_done", swich ? qty + 1 : qty - 1);
//                objects.remove(i);
//                objects.add(i, packLine);
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
            return searchObject;
        }
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
                Calendar c = Calendar.getInstance();
                String formattedDate = df.format(c.getTime());
                Log.i("formattedDate==f===", formattedDate);
                line.put("seq_date", formattedDate);
                lines.add(line);
            }
            objects.clear();
            objects.addAll(lines);
        }
        mAdapter = mList.getAdapter(R.layout.pickng_pack_operation_line_item, objects,
                new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                    UpdateServerQty updateServerQty = new UpdateServerQty();
                    List<Integer> positions = new ArrayList<>();

                    private void colorChange(View mView, int color) {
                        OControls.setTextColor(mView, R.id.edtName, OResource.color(mContext, color));
                        OControls.setTextColor(mView, R.id.txtBarCode, OResource.color(mContext, color));
                        OControls.setTextColor(mView, R.id.edtProductUom, OResource.color(mContext, color));
                        OControls.setTextColor(mView, R.id.edtProductOrderQty, OResource.color(mContext, color));
                        OControls.setTextColor(mView, R.id.edtProductQty, OResource.color(mContext, color));
                        OControls.setTextColor(mView, R.id.edtProductQtyDone, OResource.color(mContext, color));
                    }

                    @Override
                    public View getView(final int position, final View mView, ViewGroup parent) {
                        final ODataRow row = (ODataRow) mAdapter.getItem(position);
                        final EditText edtProductQtyDone = (EditText) mView.findViewById(R.id.edtProductQtyDone);
                        edtProductQtyDone.setFocusable(mEditMode);
                        final SwitchCompat switchCompat = (SwitchCompat) mView.findViewById(R.id.switchButton);
                        switchCompat.setChecked(row.getBoolean("swicher"));
                        if (mEditMode) {
                            switchCompat.setVisibility(View.VISIBLE);
                        }
                        final CardView cardView = (CardView) mView.findViewById(R.id.picking_card_view);

                        for (int pos : positions) {
                            if (pos == position)
                                cardView.setCardBackgroundColor(OResource.color(mContext, R.color.drawer_separator_background));
                        }

                        OControls.setText(mView, R.id.edtName, row.getString("product_name"));
                        OControls.setText(mView, R.id.txtBarCode, row.getString("barcode").equals("false") ? "" : row.getString("barcode"));
                        OControls.setText(mView, R.id.edtProductUom, row.getString("product_uom_name"));
                        OControls.setText(mView, R.id.edtProductOrderQty, row.getString("ordered_qty"));
                        OControls.setText(mView, R.id.edtProductQty, row.getString("product_qty"));
                        edtProductQtyDone.setText(row.getString("qty_done"));

                        int color = R.color.body_text_2;
                        if (row.getFloat("product_qty").equals(row.getFloat("qty_done"))) {
                            color = R.color.line_green;
                        } else if (row.getFloat("product_qty") < row.getFloat("qty_done")) {
                            color = R.color.line_red;
                        }
                        colorChange(mView, color);

                        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                row.put("swicher", isChecked);
                                lines.remove(position);
                                lines.add(position, row);
                            }
                        });

                        edtProductQtyDone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    try {
                                        if (edtProductQtyDone.length() == 0) {
                                            edtProductQtyDone.setText("0.0");
                                        }
                                        row.put("qty_done", edtProductQtyDone.getText().toString());
                                        Log.i("seq_date=====", row.getString("seq_date"));
                                        for (int i = 0; i < objects.size(); i++) {
                                            ODataRow r = (ODataRow) objects.get(i);
                                            Log.i("seq_date==rr===", r.getString("seq_date"));
                                            if (r.getString("id").equals(row.getString("id"))) {
                                                int color = R.color.body_text_2;
                                                if (row.getFloat("product_qty").equals(row.getFloat("qty_done"))) {
                                                    color = R.color.line_green;
                                                } else if (row.getFloat("product_qty") < row.getFloat("qty_done")) {
                                                    color = R.color.line_red;
                                                }
                                                if (cardView.getCardBackgroundColor().getDefaultColor() != OResource.color(mContext, R.color.drawer_separator_background)) {
                                                    if (positions.size() == 0) {
                                                        positions.add(objects.size() - 1);
                                                    } else {
                                                        int m = positions.get(positions.size() - 1);
                                                        positions.add(m - 1);
                                                    }
                                                }
                                                cardView.setCardBackgroundColor(OResource.color(mContext, R.color.drawer_separator_background));
                                                colorChange(mView, color);
                                                Calendar c = Calendar.getInstance();
                                                String date_date = df.format(c.getTime());
                                                Log.i("formattedDate=====", date_date);
                                                row.put("seq_date", date_date);
                                                objects.remove(i);
                                                objects.add(i, row);
                                                mAdapter.notifyDataSetChangedWithSort(objects);
                                                break;
                                            }
                                        }
                                    } catch (NumberFormatException | NullPointerException e) {
                                        edtProductQtyDone.setText("0");
                                        colorChange(mView, R.color.body_text_2);
                                    } catch (Exception e) {
                                        Log.i(TAG, e.toString());
                                    }
                                }
                            }
                        });
                        return mView;
                    }
                });
        mAdapter.notifyDataSetChanged(objects);
    }

    private class UpdateServerQty extends AsyncTask<List, Void, Void> {
        private ProgressDialog progressDialog;

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog = new ProgressDialog(ReceiptPickingDetails.this);
//            progressDialog.setCancelable(false);
//            progressDialog.setTitle(R.string.title_please_wait);
//            progressDialog.setMessage(OResource.string(ReceiptPickingDetails.this, R.string.pack_operation_sync));
////            progressDialog.show();
//                Calendar c = Calendar.getInstance();
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String formattedDate = df.format(c.getTime());
//        }

        @Override
        protected Void doInBackground(List... params) {
            List param = new ArrayList();
            param = params[0];
            ORecordValues value = new ORecordValues();
            float qty = Float.parseFloat(param.get(0).toString());
            value.put("qty_done", qty);
            int serverId = Integer.parseInt(param.get(1).toString());
            try {
                packOperation.getServerDataHelper().updateOnServer(value, serverId);
//                ServerDataHelper helper = picking.getServerDataHelper();
//                OArguments arguments = new OArguments();
//                arguments.add(record.getInt("id"));
//                helper.callMethod("action_assign", arguments);
//                picking.quickCreateRecord(record);
                ODataRow lineRecord = new ODataRow();
                lineRecord.put("id", serverId);
                packOperation.quickCreateRecord(lineRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initAdapter();
//            progressDialog.dismiss();
        }
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
        switch (v.getId()) {
            case R.id.btnDoNewTransfer:
                Log.i("btnClick===", "work");
//                if (hasRecordInExtra()) {
//                            DoNewTransfer doNewTransfer = new DoNewTransfer();
//                            if (app.inNetwork()) {
//                                doNewTransfer.execute();
//                                mEditMode = !mEditMode;
//                                setupToolbar();
//                                initAdapter();
//                            } else {
//                                Toast.makeText(getApplicationContext(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
//                            }
//                        }
                break;
        }

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
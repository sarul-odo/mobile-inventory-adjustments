package com.odoo.addons.picking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.addons.picking.wizards.ProductSerialWizard;
import com.odoo.addons.stock.Models.PackOperation;
import com.odoo.addons.stock.Models.PackOperationLot;
import com.odoo.addons.stock.Models.Picking;
import com.odoo.addons.stock.Models.PickingType;
import com.odoo.addons.stock.Models.ProductTemplate;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.ServerDataHelper;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.ODomain;
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

public class ReceiptPickingDetails extends OdooCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = ReceiptPickingDetails.class.getSimpleName();
    private Bundle extra;
    private OForm mForm;
    private EditText scanCode;
    private ODataRow record = null;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private Picking picking;
    private PackOperation packOperation;
    private PackOperationLot packOperationLot;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<ODataRow> scrapPartLines = new ArrayList<>();
    private List<ODataRow> partRow = new ArrayList<>();
    private Toolbar toolbar;
    private LinearLayout layoutAddItem = null;
    private Context mContext;
    App app;
    private HashMap<String, Boolean> toWizardTechParts = new HashMap<>();
    private List<Object> objects = new ArrayList<>();

    public static final int REQUEST_ADD_ITEMS = 323;
    private List<ODataRow> lines = new ArrayList<>();

    private ProductProduct products = null;
    private ProductTemplate tmpl = null;
    private OField state;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private PackOperationLotSync packOperationLotSync;
    private PickingType pickingType;
    private String pickTypeCode;
    private Boolean lineSync = false;
    SwipeRefreshLayout swipeLayout;
    Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picking_detail);
        extra = getIntent().getExtras();
        app = (App) getApplicationContext();
        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.toolbarPicking);
        ScrollView sv = (ScrollView) findViewById(R.id.scroll);
        mActivity = this;
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
        packOperationLot = new PackOperationLot(this, null);
        products = new ProductProduct(this, null);
        tmpl = new ProductTemplate(this, null);
        pickingType = new PickingType(this, null);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        mForm = (OForm) findViewById(R.id.OFormPicking);
        layoutAddItem = (LinearLayout) findViewById(R.id.layoutAddItem);
        scanCode = (EditText) findViewById(R.id.scanCode);
        state = (OField) findViewById(R.id.StatePicking);
        setupToolbar();
        initAdapter();
    }

    private void ToolbarMenuSetVisibl(Boolean Visibility) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_picking_edit).setVisible(!Visibility);
            mMenu.findItem(R.id.menu_picking_confirm).setVisible(!Visibility);
            if (!Visibility)
                mMenu.findItem(R.id.menu_packeg_sync).setVisible(lineSync);
            mMenu.findItem(R.id.menu_picking_save).setVisible(Visibility);
            mMenu.findItem(R.id.menu_picking_cancel).setVisible(Visibility);
        }
    }

    private void setMode(Boolean edit) {
        ToolbarMenuSetVisibl(edit);
        layoutAddItem.setVisibility(View.GONE);
        scanCode.setText(null);
        if (edit) {
            layoutAddItem.setVisibility(View.VISIBLE);
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
                    String ean13 = packLine.getString("barcode").toLowerCase();
                    String name = packLine.getString("product_name").toLowerCase();
                    if (ean13.contains(code) || name.contains(code)) {
                        searchObject.add(packLine);
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
            pickTypeCode = pickingType.browse(record.getInt("picking_type_id")).getString("code");
            ODomain domain = new ODomain();
            setTitle(record.getString("name"));
            mForm.initForm(record);
            mForm.setEditable(mEditMode);
        }
        setMode(mEditMode);
    }

    private void initAdapter() {
        mList = (ExpandableListControl) findViewById(R.id.ExpandListProductLine);
        if (extra != null && record != null) {
            lines.clear();
            for (ODataRow line : record.getO2MRecord("pack_operation_product_ids").browseEach()) {
                if (!line.getFloat("ordered_qty").equals(line.getFloat("product_qty")) && !lineSync) {
                    lineSync = true;
                }
                Calendar c = Calendar.getInstance();
                String formattedDate = df.format(c.getTime());
                line.put("seq_date", formattedDate);
                lines.add(line);
            }
            ToolbarMenuSetVisibl(mEditMode);
            objects.clear();
            objects.addAll(lines);
        }
        mAdapter = mList.getAdapter(R.layout.pickng_pack_operation_line_item, objects,
                new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                    //                    UpdateServerQty updateServerQty = new UpdateServerQty();
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
                        Log.i("row=======", row.toString());
                        ODataRow rowProduct = products.select(new String[]{"product_tmpl_id"}, "_id = ?", new String[]{row.getString("product_id")}).get(0);
                        String tracking = tmpl.select(null, "_id = ?", new String[]{rowProduct.getString("product_tmpl_id")}).get(0).getString("tracking");
                        final Button btnTracking = (Button) mView.findViewById(R.id.btnTracking);
                        final EditText edtProductQtyDone = (EditText) mView.findViewById(R.id.edtProductQtyDone);
                        edtProductQtyDone.setEnabled(mEditMode);
                        edtProductQtyDone.setFocusable(mEditMode);
                        if (!tracking.equals("none")) {
                            btnTracking.setVisibility(mEditMode ? View.VISIBLE : View.GONE);
                            edtProductQtyDone.setEnabled(false);
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
                        btnTracking.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mActivity, ProductSerialWizard.class);
                                Bundle data = new Bundle();
                                if (row != null) {
                                    if (cardView.getCardBackgroundColor().getDefaultColor() != OResource.color(mContext, R.color.drawer_separator_background)) {
                                        if (positions.size() == 0) {
                                            positions.add(lines.size() - 1);
                                        } else {
                                            int m = positions.get(positions.size() - 1);
                                            positions.add(m - 1);
                                        }
                                    }
                                    data = row.getPrimaryBundleData();
                                    data.putString("pickingType", pickTypeCode);
                                    i.putExtras(data);
                                    startActivityForResult(i, REQUEST_ADD_ITEMS);
                                }
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
                                        for (int i = 0; i < lines.size(); i++) {
                                            ODataRow r = (ODataRow) lines.get(i);
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
                                                        positions.add(lines.size() - 1);
                                                    } else {
                                                        int m = positions.get(positions.size() - 1);
                                                        positions.add(m - 1);
                                                    }
                                                }
                                                cardView.setCardBackgroundColor(OResource.color(mContext, R.color.drawer_separator_background));
                                                colorChange(mView, color);
                                                Calendar c = Calendar.getInstance();
                                                String date_date = df.format(c.getTime());
                                                row.put("seq_date", date_date);
                                                lines.remove(i);
                                                lines.add(i, row);
                                                AdapterChangeBackground background = new AdapterChangeBackground();
                                                background.execute();
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
        mAdapter.notifyDataSetChangedWithSort(objects);
    }

    @Override
    public void onRefresh() {
        PickingRefresh pickingRefresh = new PickingRefresh();
        pickingRefresh.execute();
    }

    private class AdapterChangeBackground extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChangedWithSort(objects);
        }
    }

    private class UpdateServerQty extends AsyncTask<List, Void, Void> {
        private ProgressDialog progressDialog;

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
        }
    }

    private class PickingRefresh extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            List<ODataRow> items = new ArrayList<>();
            try {
                ODomain d = new ODomain();
                d.add("id", "=", record.getString("id"));
                picking.quickSyncRecords(d);
                List<Integer> lotIds = new ArrayList<>();
                for (ODataRow line : record.getO2MRecord("pack_operation_product_ids").browseEach()) {
                    lotIds.add(line.getInt("_id"));
                }
                d = new ODomain();
                d.add("operation_id", "in", lotIds.toArray());
                packOperationLot.quickSyncRecords(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeLayout.setRefreshing(false);
            setupToolbar();
            initAdapter();
        }
    }

    private class PackOperationLotSync extends AsyncTask<ODomain, Void, Void> {
        @Override
        protected Void doInBackground(ODomain... params) {
            try {
                packOperationLot.quickSyncRecords(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ActionAssign extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.title_please_wait);
            progressDialog.setMessage(OResource.string(mActivity, R.string.pack_operation_sync));
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

                ODomain d = new ODomain();
                List<Integer> lotIds = new ArrayList<>();
                for (ODataRow line : record.getO2MRecord("pack_operation_product_ids").browseEach()) {
                    lotIds.add(line.getInt("_id"));
                }
                d.add("operation_id", "in", lotIds.toArray());
                packOperationLot.quickSyncRecords(d);

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

    private class DoNewTransfer extends AsyncTask<Boolean, Void, Boolean> {
        private ProgressDialog progressDialog;
        private boolean backorderCreate;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.title_please_wait);
            progressDialog.setMessage(OResource.string(mActivity, R.string.picking_transfer));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            backorderCreate = params[0];
            try {
                for (ODataRow line : record.getO2MRecord("pack_operation_product_ids").browseEach()) {
                    if (line.getFloat("qty_done") == 0.0) {
                        String product_tmpl_id = products.select(new String[]{"product_tmpl_id"}, "_id = ?", new String[]{line.getString("product_id")}).get(0).getString("product_tmpl_id");
                        String tracking = tmpl.select(null, "_id = ?", new String[]{(product_tmpl_id)}).get(0).getString("tracking");
                        if (!tracking.equals("none")) {
                            return false;
                        }
                    }
                }
                if (!backorderCreate) {
                    ServerDataHelper helper = picking.getServerDataHelper();
                    OArguments arguments = new OArguments();
                    arguments.add(record.getInt("id"));
                    Object response = helper.callMethod("do_new_transfer", arguments);
                    if (response.equals(false)) {
                        picking.delete(record.getInt("_id"));
                    } else {
                        backorderCreate = true;
                    }
                } else {
                    OModel wizard = new OModel(getApplicationContext(), "stock.backorder.confirmation", picking.getUser());
                    ServerDataHelper immediateTransfer = new ServerDataHelper(getApplicationContext(), wizard, picking.getUser());
                    OArguments argumentss = new OArguments();
                    argumentss.add(record.getInt("id"));
                    argumentss.add(record.getInt("id"));
                    //                    argumentss.add(new JSONArray().put(record.getInt("id")));
                    //                    argumentss.add(new JSONObject());
                    //                    HashMap<String, Object> args = new HashMap<>();
                    //                    args.put("pick_id", record.getInt("id"));
                    immediateTransfer.callMethod("process_mobile", argumentss);
                    picking.delete(record.getInt("_id"));
                    backorderCreate = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressDialog.dismiss();
            if (success && !backorderCreate) {
                finish();
            } else if (!success) {
                OAlert.showAlert(mActivity, "Зарим бараа нь цувралын дугаар шаардаж байгаа тул эхлээд тэдгээрийг нь зааж өгнө үү!");
            }
            if (backorderCreate) {
                OAlert.showConfirm(mActivity, OResource.string(mActivity, R.string.backorder_confirm),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    DoNewTransfer doNewTransfer = new DoNewTransfer();
                                    doNewTransfer.execute(true);
                                }
                            }
                        });
            }
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
            case R.id.menu_picking_confirm:
                DoNewTransfer doNewTransfer = new DoNewTransfer();
                if (app.inNetwork()) {
                    doNewTransfer.execute(false);
                    setupToolbar();
                    initAdapter();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btnDoNewTransfer:
//                Log.i("btnClick===", "work");
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
//                break;
        }
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
            mDialog = new ProgressDialog(mActivity);
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
                Toast.makeText(mActivity, "Мэдээлэл амжилттай хадгалагдлаа", Toast.LENGTH_LONG).show();
                setupToolbar();
                initAdapter();
            } else if (productName.length() > 0) {
                Toast.makeText(mActivity, productName + "\nБарааны ТОО ХЭМЖЭЭГ оруулана уу!!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("resultCode======", resultCode + "");
        if (resultCode == Activity.RESULT_OK) {
            ODataRow row = new ODataRow();
            for (String key : data.getExtras().keySet()) {
                if (key.equals("_id")) {
                    row = packOperation.select(new String[]{"qty_done"}, "_id=?", new String[]{data.getExtras().getString(key)}).get(0);
                    break;
                }
            }
            int i = 0;
            Calendar c = Calendar.getInstance();
            String date_date = df.format(c.getTime());
            for (ODataRow line : lines) {
                if (line.getInt("_id") == row.getInt("_id")) {
                    line.put("qty_done", row.getString("qty_done"));
                    line.put("seq_date", date_date);
                    lines.remove(i);
                    lines.add(i, line);
                    break;
                }
                i++;
            }
            mAdapter.notifyDataSetChangedWithSort(objects);
        }
    }
}
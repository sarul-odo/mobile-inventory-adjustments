package com.odoo.addons.picking.wizards;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.addons.stock.Models.PackOperation;
import com.odoo.addons.stock.Models.PackOperationLot;
import com.odoo.addons.stock.Models.StockProductionLot;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OResource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import odoo.controls.ExpandableListControl;
import odoo.controls.OField;
import odoo.controls.OForm;

public class ProductSerialWizard extends OdooCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private OField oReason;
    private Menu mMenu;
    private Boolean mEditMode = false;
    private Bundle extra;
    private ODataRow record = null;
    private OForm mForm;
    private GridView gridView;
    private Context mContext;
    private Button takePic;
    private OFileManager fileManager;
    private ArrayList<String> imageItemsString = new ArrayList<>();
    private String scrap_id;
    private String scrap_name = "";
    private String rowId;
    private PackOperation packOperation;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<Object> objects = new ArrayList<>();
    private List<ODataRow> serialLine = new ArrayList<>();
    private TextView lineAdd;
    private StockProductionLot stockProductionLot = null;
    private PackOperationLot packOperationLot = null;
    private OnProductLotChangeUpdate onProductLotChangeUpdate;
    private ProductProduct productProduct;
    private ODomain domain = new ODomain();
    private String pickingType;
    private List<Integer> deleteIds = new ArrayList<>();
    App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extra = getIntent().getExtras();
//        scrap_id = extra.getString("scrap_id");
//        scrap_name = extra.getString("scrap_name");
//        setTitle(scrap_name);
//        rowId = String.valueOf(extra.getInt(OColumn.ROW_ID));
        setContentView(R.layout.product_serial_wizard);
        setResult(RESULT_CANCELED);
        mContext = getApplicationContext();

        mList = (ExpandableListControl) findViewById(R.id.ExpandListProdcutLine);
        mList.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbarWizard);
        mForm = (OForm) findViewById(R.id.ProductSerialWizard);
        app = (App) getApplicationContext();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        packOperation = new PackOperation(this, null);
        stockProductionLot = new StockProductionLot(this, null);
        packOperationLot = new PackOperationLot(this, null);
        productProduct = new ProductProduct(this, null);
        onProductLotChangeUpdate = new OnProductLotChangeUpdate();
        record = packOperation.browse(extra.getInt(OColumn.ROW_ID));
        pickingType = extra.getString("pickingType");
        int localId = record.getInt("product_id");
        domain.add("product_id", "=", localId);
        onProductLotChangeUpdate.execute(domain);

        mForm.initForm(record);
        mForm.setEditable(mEditMode);
        Log.i("record======", record.toString());
        serialLine = record.getO2MRecord("pack_lot_ids").browseEach();

        Log.i("serialLine======", serialLine.toString());
        CheckLotSync checkLotSync = new CheckLotSync();
        checkLotSync.execute(serialLine);

        lineAdd = (TextView) findViewById(R.id.lineAdd);
        lineAdd.setOnClickListener(this);

    }

    private void ToolbarMenuSetVisibl(Boolean Visibility) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_product_save).setVisible(Visibility);
            mMenu.findItem(R.id.menu_product_cancel).setVisible(Visibility);
            mMenu.findItem(R.id.menu_product_edit).setVisible(!Visibility);
        }
    }

    private class CheckLotSync extends AsyncTask<List<ODataRow>, Void, Void> {
        @Override
        protected Void doInBackground(List<ODataRow>... params) {
            for (ODataRow row : params[0]) {
                try {
                    ODataRow lot = stockProductionLot.select(null, "_id = ?", new String[]{row.getString("lot_id")}).get(0);
                    if (lot.getString("write_date").equals("false")) {
                        ODomain d = new ODomain();
                        d.add("id", "=", lot.getString("id"));
                        stockProductionLot.quickSyncRecords(d);
                    }
                } catch (IndexOutOfBoundsException ex) {
                    Log.i(TAG, ex.toString());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            drawSerial(serialLine);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_serial_menu, menu);
        mMenu = menu;
        ToolbarMenuSetVisibl(mEditMode);
        return true;
    }

    private List<String> adapterUpdate(List<String> items, int pos) {
        List<String> newAdap = new ArrayList<>();
//        for (Object obj : used) {
//            ODataRow row = (ODataRow) obj;
////            int a = row.getInt("lot_id");
////            int index = spinnerMap.get(a);
////            Log.i("iindex=======", index + "");
//            String use = base.get(pos);
//            for (String val : base) {
//                if (!val.equals(use)) {
//                    newAdap.add(val);
//                }
//            }
//        }
        String use = items.get(pos);
        for (String val : items) {
            if (!val.equals(use)) {
                newAdap.add(val);
            }
        }
        return newAdap;
    }

    private void drawSerial(List<ODataRow> serialLine) {
        objects.clear();
        objects.addAll(serialLine);
        final List<String> items = new ArrayList<>();
        final HashMap<Integer, Integer> spinnerMap = new HashMap<Integer, Integer>();
        Log.i("product====", record.getString("product_id"));
        if (pickingType.equals("outgoing")) {
            List<ODataRow> rows = stockProductionLot.select(null, "product_id = ? ", new String[]{record.getString("product_id")});
            Log.i("rows===sss-===", rows.toString());
            items.add("Цуврал сонгоно уу...");
            spinnerMap.put(-1, 0);
            int i = 1;
            for (ODataRow row : rows) {
                Log.i("row=========sss-===", row.toString());
                items.add(row.getString("name"));
                spinnerMap.put(row.getInt("_id"), i);
                i++;
            }
        }

        mAdapter = mList.getAdapter(R.layout.product_serial_item, objects,
                new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                    @Override
                    public View getView(final int position, View mView, ViewGroup parent) {
                        final ODataRow row = (ODataRow) mAdapter.getItem(position);
                        Log.i("row===", row.toString());
                        Button btnDelete = (Button) mView.findViewById(R.id.btnDelete);
                        final EditText editQty = (EditText) mView.findViewById(R.id.txtQty);
                        final EditText acLot = (EditText) mView.findViewById(R.id.editTextLot);
                        final Spinner spinnerLot = (Spinner) mView.findViewById(R.id.spinnerLot);
                        if (pickingType.equals("outgoing")) {
                            acLot.setVisibility(View.GONE);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProductSerialWizard.this, android.R.layout.simple_spinner_dropdown_item, items);
                            Log.i("id==2222====", row.getInt("lot_id") + "");
                            int pos = (Integer) spinnerMap.get(row.getInt("lot_id"));
                            spinnerLot.setAdapter(adapter);
                            spinnerLot.setVisibility(View.VISIBLE);
                            spinnerLot.setSelection(pos);
                            spinnerLot.setEnabled(mEditMode);
                        } else if (pickingType.equals("incoming")) {
                            spinnerLot.setVisibility(View.GONE);
                            acLot.setVisibility(View.VISIBLE);
                            if (!row.getString("lot_name").equals("false"))
                                acLot.setText(row.getString("lot_name"));
                            acLot.setEnabled(mEditMode);
                        }
                        editQty.setEnabled(mEditMode);
                        spinnerLot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                                List<ODataRow> rows = stockProductionLot.select(null, "name = ? and product_id = ?", new String[]{spinnerLot.getSelectedItem().toString(), record.getString("product_id")});
                                for (ODataRow r : rows) {
                                    objects.remove(position);
                                    row.put("lot_id", r.getString("_id"));
                                    objects.add(position, row);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
//                        spinnerLot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                            @Override
//                            public void onFocusChange(View v, boolean hasFocus) {
//
//                            }
//                        });
                        acLot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
//                                        OAlert.showConfirm(ProductSerialWizard.this, OResource.string(ProductSerialWizard.this, R.string.close_activity),
//                                                new OAlert.OnAlertConfirmListener() {
//                                                    @Override
//                                                    public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
//                                                        if (type == OAlert.ConfirmType.POSITIVE) {
//                                                        }
//                                                    }
//                                                });
//                                        int result = 0;
//                                        List<Object> currentObject = new ArrayList<>();
//                                        currentObject = objects;
//                                        ODataRow currentRows = new ODataRow();
////                                        currentRows = (ODataRow) currentObject.remove(currentObject.size() - 1);
//                                        if (currentRows.getString("lot_name").equals("false")) {
//                                            currentObject.remove(currentObject.size() - 1);
//                                            currentRows.put("lot_name", acLot.getText().toString());
//                                            currentObject.add(currentRows);
//                                        }
//                                        for (Object line : currentObject) {
//                                            currentRows = (ODataRow) line;
//                                            Log.i("currentRows====", currentRows.getString("lot_name"));
//                                            if (currentRows.getString("lot_name").equals(acLot.getText().toString()) && !currentRows.getString("lot_name").equals("false")) {
//                                                result++;
//                                                Log.i("true_rowssss====", currentRows.getString("lot_name"));
//                                            }
//                                            if (result == 2) {
//                                                Log.i("lot_name====", currentRows.getString("lot_name"));
//                                                acLot.setBackgroundColor(Color.RED);
//                                                Toast.makeText(ProductSerialWizard.this, "Энэ цувралын нэрийг өөр хөдөлгөөнд аль хэдийнээ хэрэглэсэн байна", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
                                    if (true) {
                                        objects.remove(position);
                                        row.put("lot_name", acLot.getText());
                                        objects.add(position, row);
                                    }

                                }
                            }
                        });
                        editQty.setText(row.getString("qty"));
                        editQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    objects.remove(position);
                                    row.put("qty", editQty.getText());
                                    objects.add(position, row);
                                }
                            }
                        });
                        btnDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mEditMode) {
                                    if (row.getInt("_id") > 0)
                                        deleteIds.add(row.getInt("_id"));
                                    acLot.clearFocus();
                                    objects.remove(position);
                                    mAdapter.notifyDataSetChanged(objects);
                                }
                            }
                        });

                        return mView;
                    }
                });
        mAdapter.notifyDataSetChanged(objects);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ODomain domain = new ODomain();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_product_save:
                if (app.inNetwork()) {
                    mEditMode = !mEditMode;
                    View current = getCurrentFocus();
                    if (current != null)
                        current.clearFocus();
                    OnPackOperationLotUpdate onPackOperationLotUpdate = new OnPackOperationLotUpdate();
                    onPackOperationLotUpdate.execute();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_product_cancel:
                OAlert.showConfirm(this, OResource.string(this, R.string.close_activity),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    mEditMode = !mEditMode;
                                    ToolbarMenuSetVisibl(mEditMode);
                                    drawSerial(serialLine);
                                } else {
                                    mForm.setEditable(true);
                                    ToolbarMenuSetVisibl(mEditMode);
                                }
                            }
                        });
                break;
            case R.id.menu_product_edit:
                mEditMode = !mEditMode;
                drawSerial(serialLine);
                ToolbarMenuSetVisibl(mEditMode);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnPackOperationLotUpdate extends AsyncTask<Void, Boolean, Boolean> {
        private ProgressDialog mDialog;
        boolean lot = true;
        String name = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(ProductSerialWizard.this);
            mDialog.setTitle(R.string.title_working);
            mDialog.setMessage("Мөрүүдийг шинэчилж байна...");
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                for (int id : deleteIds) {
                    packOperationLot.delete(id);
                }
                float sumQty = 0;
                for (Object row : objects) {
                    ODataRow dRow = (ODataRow) row;
                }
                for (int j = 0; j < objects.size(); j++) {
                    for (int i = j + 1; i < objects.size(); i++) {
                        ODataRow row = (ODataRow) objects.get(j);
                        ODataRow row_1 = (ODataRow) objects.get(i);
                        if (pickingType.equals("outgoing")) {
                            if (row.getInt("lot_id") == -1)
                                continue;
                            if (row.getInt("lot_id") == row_1.getInt("lot_id")) {
                                name = stockProductionLot.select(new String[]{"name"}, "_id = ?", new String[]{row.getString("lot_id")}).get(0).getString("name");
                                return lot = false;
                            }
                        } else if (pickingType.equals("incoming")) {
                            if (row.getString("lot_name").equals("false"))
                                continue;
                            if (row.getString("lot_name").equals(row_1.getString("lot_name"))) {
                                name = row.getString("lot_name");
                                return lot = false;
                            }
                        }
                    }
                }
                for (Object row : objects) {
                    ODataRow dRow = (ODataRow) row;
                    ORecordValues rRow = new ORecordValues();
                    rRow.put("qty", dRow.getFloat("qty"));
                    rRow.put("operation_id", record.getInt("id"));
                    if (pickingType.equals("outgoing")) {
                        if (dRow.getInt("lot_id") == -1)
                            continue;
                        rRow.put("lot_id", stockProductionLot.selectServerId(dRow.getInt("lot_id")));
                    }

                    if (pickingType.equals("incoming")) {
                        Log.i("gggg=====", dRow.getString("lot_name"));
                        if (dRow.getString("lot_name").equals("false"))
                            continue;
                        rRow.put("lot_name", dRow.getString("lot_name"));
                    }
                    sumQty += dRow.getFloat("qty");
                    if (dRow.getInt("_id") > 0) {
                        Log.i("oRow===", rRow.toString());
                        packOperationLot.getServerDataHelper().updateOnServer(rRow, dRow.getInt("id"));
                    } else {
                        packOperationLot.getServerDataHelper().createOnServer(rRow);
                    }
                }

                ORecordValues packOp = new ORecordValues();
                packOp.put("qty_done", sumQty);
                packOperation.getServerDataHelper().updateOnServer(packOp, record.getInt("id"));
                ODomain d = new ODomain();
                d = new ODomain();
                d.add("id", "=", record.getInt("id"));
                packOperation.quickSyncRecords(d);
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
                Toast.makeText(ProductSerialWizard.this, "Мэдээлэл амжилттай хадгалагдлаа", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                Bundle data = new Bundle();
                data.putString("_id", record.getString("_id"));
                intent.putExtras(data);
                setResult(RESULT_OK, intent);
                finish();
//                setupToolbar();
//                initAdapter();
            } else if (!lot) {
                Toast.makeText(ProductSerialWizard.this, name + ": Энэ цувралыг өөр хөдөлгөөнд аль хэдийнээ хэрэглэсэн байна", Toast.LENGTH_LONG).show();
            }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lineAdd: {
                if (mEditMode) {
                    ODataRow newRow = new ODataRow();
                    newRow.put("qty", 1.0);
                    newRow.put("lot_id", "-1");
                    newRow.put("_id", "-1");
                    objects.add(newRow);
                    mAdapter.notifyDataSetChanged(objects);
                }
            }
        }
    }

    private class OnProductLotChangeUpdate extends AsyncTask<ODomain, Void, Void> {

        @Override
        protected Void doInBackground(ODomain... params) {
            if (app.inNetwork()) {
                ODomain domain = params[0];
                stockProductionLot.quickSyncRecords(domain);
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

}

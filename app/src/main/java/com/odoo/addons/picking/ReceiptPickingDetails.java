package com.odoo.addons.picking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.picking.models.PartScrapPhotos;
import com.odoo.addons.picking.models.Picking;
import com.odoo.addons.picking.models.TechnicParts;
import com.odoo.addons.technic.models.TechnicsModel;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.RelValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mn.odoo.addons.otherClass.AddItemLineWizard;
import mn.odoo.addons.scrapParts.wizards.PartsDetailsWizard;
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
    private OField oState, oOrigin, date, technicId, isPaybale;
    private ODataRow record = null;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private TechnicsModel technic;
    private TechnicParts technicParts;
    private PartScrapPhotos partScrapPhotos;
    private Picking scrapParts;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<ODataRow> scrapPartLines = new ArrayList<>();
    private List<ODataRow> technicPartLines = new ArrayList<>();
    private List<ODataRow> partRow = new ArrayList<>();
    private Toolbar toolbar;
    private LinearLayout layoutAddItem = null;
    private Context mContext;
    App app;
    /*Зүйлс оруулж ирэх*/
    private HashMap<String, Boolean> toWizardTechParts = new HashMap<>();
    private List<Object> objects = new ArrayList<>();
    public static final int REQUEST_ADD_ITEMS = 323;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrap_parts_detail);

        extra = getIntent().getExtras();
        app = (App) getApplicationContext();
        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.toolbarScrapPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditMode = (!hasRecordInExtra() ? true : false);
        technic = new TechnicsModel(this, null);
        technicParts = new TechnicParts(this, null);
        partScrapPhotos = new PartScrapPhotos(this, null);
        scrapParts = new Picking(this, null);

        mList = (ExpandableListControl) findViewById(R.id.ExpandListPartLine);
        mList.setOnClickListener(this);
        mForm = (OForm) findViewById(R.id.OFormPartScrap);

        oState = (OField) mForm.findViewById(R.id.StatePartScrap);
        oOrigin = (OField) mForm.findViewById(R.id.OriginPartScrap);
        date = (OField) mForm.findViewById(R.id.DatePartScrap);
        technicId = (OField) mForm.findViewById(R.id.TechnicPartScrap);
        isPaybale = (OField) mForm.findViewById(R.id.IsPayablePartScrap);
        layoutAddItem = (LinearLayout) findViewById(R.id.layoutAddItem);
        layoutAddItem.setOnClickListener(this);

        setupToolbar();
    }

    private void ToolbarMenuSetVisibl(Boolean Visibility) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_more).setVisible(!Visibility);
            mMenu.findItem(R.id.menu_edit).setVisible(!Visibility);
            mMenu.findItem(R.id.menu_save).setVisible(Visibility);
            mMenu.findItem(R.id.menu_cancel).setVisible(Visibility);
        }
    }

    private void setMode(Boolean edit) {
        ToolbarMenuSetVisibl(edit);
        oOrigin.setEditable(false);
        oState.setEditable(false);
        if (edit && record == null) {
            technicId.setOnValueChangeListener(this);
        }
        if (record != null) {
            date.setEditable(false);
            technicId.setEditable(false);
            isPaybale.setEditable(false);
        }
    }

    private void setupToolbar() {
        if (!hasRecordInExtra()) {
            setTitle("Үүсгэх");
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
            ((OField) mForm.findViewById(R.id.DatePartScrap)).setValue(ODateUtils.getDate());
        } else {
            setTitle("Сэлбэгийн акт дэлгэрэнгүй");
            int ScrapId = extra.getInt(OColumn.ROW_ID);
            record = scrapParts.browse(ScrapId);
            mForm.initForm(record);
            mForm.setEditable(mEditMode);
            scrapPartLines = record.getM2MRecord("parts").browseEach();
            drawPart(scrapPartLines);
        }
        setMode(mEditMode);
    }


    private void drawPart(List<ODataRow> rows) {
        objects.clear();
        objects.addAll(rows);
        mAdapter = mList.getAdapter(R.layout.scrap_accumulator_accum_item, objects,
                new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                    @Override
                    public View getView(final int position, View mView, ViewGroup parent) {
                        ODataRow row = (ODataRow) mAdapter.getItem(position);
                        OControls.setText(mView, R.id.name, (position + 1) + ". " + row.getString("name"));
                        OControls.setText(mView, R.id.date, row.getString("date"));
                        if (row.getString("date").equals("false"))
                            OControls.setText(mView, R.id.date, "");
                        OControls.setText(mView, R.id.product, row.getString("product_name"));
                        OControls.setText(mView, R.id.capacity, row.getString("reason_name"));
                        OControls.setText(mView, R.id.usage_percent, row.getString("part_cost"));

                        if (row.getString("state").equals("draft"))
                            OControls.setText(mView, R.id.state, "Ноорог");
                        else if (row.getString("state").equals("in_use"))
                            OControls.setText(mView, R.id.state, "Ашиглаж буй");
                        else if (row.getString("state").equals("in_reserve"))
                            OControls.setText(mView, R.id.state, "Нөөцөнд");
                        else if (row.getString("state").equals("in_scrap"))
                            OControls.setText(mView, R.id.state, "Акталсан");

                        mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ODataRow row = (ODataRow) mAdapter.getItem(position);
                                loadActivity(row);
                            }
                        });
                        return mView;
                    }
                });
        mAdapter.notifyDataSetChanged(objects);
    }

    private boolean hasRecordInExtra() {
        return extra != null && extra.containsKey(OColumn.ROW_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        mMenu = menu;
        ToolbarMenuSetVisibl(mEditMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final OnPartScrapChangeUpdate onPartScrapChangeUpdate = new OnPartScrapChangeUpdate();
        final ODomain domain = new ODomain();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    List ids = new ArrayList();
                    for (ODataRow row : scrapPartLines) {
                        OValues oValues = new OValues();
                        ids.add(row.getInt("_id"));
                        oValues.put("in_scrap", true);
                        technicParts.update(row.getInt("_id"), oValues);
                    }
                    if (ids.isEmpty()) {
                        OAlert.showError(this, "Сэлбэг сонгон уу?");
                        break;
                    }

                    if (record != null) {
                        values.put("parts", new RelValues().replace(ids));
                        scrapParts.update(record.getInt(OColumn.ROW_ID), values);
                        onPartScrapChangeUpdate.execute(domain);
                        mEditMode = !mEditMode;
                        mForm.setEditable(mEditMode);
                        setMode(mEditMode);
                        Toast.makeText(this, R.string.tech_toast_information_saved, Toast.LENGTH_LONG).show();
                    } else {
                        values.put("parts", new RelValues().append(ids));
                        values.put("technic_name", technic.browse(values.getInt("technic")).getString("name"));
                        int row_id = scrapParts.insert(values);
                        if (row_id != scrapParts.INVALID_ROW_ID) {
                            onPartScrapChangeUpdate.execute(domain);
                            Toast.makeText(this, R.string.tech_toast_information_created, Toast.LENGTH_LONG).show();
                            mEditMode = !mEditMode;
                            finish();
                        }
                    }
                }
                break;
            case R.id.menu_cancel:
                OAlert.showConfirm(this, OResource.string(this, R.string.close_activity),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    mEditMode = !mEditMode;
                                    setupToolbar();
                                } else {
                                    mForm.setEditable(true);
                                    setMode(mEditMode);
                                }
                            }
                        });
                break;
            case R.id.menu_edit:
                if (hasRecordInExtra()) {
                    mEditMode = !mEditMode;
                    mForm.setEditable(mEditMode);
                    setMode(mEditMode);
                }
                break;
            case R.id.menu_delete:
                OAlert.showConfirm(this, OResource.string(this,
                        R.string.to_delete),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    if (scrapParts.delete(record.getInt(OColumn.ROW_ID))) {
                                        onPartScrapChangeUpdate.execute(domain);
                                        Toast.makeText(ReceiptPickingDetails.this, R.string.tech_toast_information_deleted,
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutAddItem:
                int techId = (Integer) technicId.getValue();
                if (techId > 0) {
                    getTechnicParts(techId);
                    Intent intent = new Intent(this, AddItemLineWizard.class);
                    Bundle extra = new Bundle();
                    for (String key : toWizardTechParts.keySet()) {
                        extra.putBoolean(key, toWizardTechParts.get(key));
                    }
                    AddItemLineWizard.mModel = technicParts;
                    intent.putExtras(extra);
                    startActivityForResult(intent, REQUEST_ADD_ITEMS);
                }
                break;
        }
    }

    private void getTechnicParts(int techId) {
        technicPartLines = technicParts.select(null, "technic = ?", new String[]{techId + ""});
        toWizardTechParts.clear();
        for (ODataRow line : technicPartLines) {
            toWizardTechParts.put(line.getString("_id"), false);
        }
        for (ODataRow line : scrapPartLines) {
            if (toWizardTechParts.containsKey(line.getString("_id"))) {
                toWizardTechParts.put(line.getString("_id"), true);
            }
        }
    }

    private void loadActivity(ODataRow row) {
        if (record != null) {
            Intent intent = new Intent(this, PartsDetailsWizard.class);
            Bundle extra = new Bundle();
            if (row != null) {
                extra = row.getPrimaryBundleData();
                extra.putString("scrap_id", record.getString("_id"));
                extra.putString("scrap_name", record.getString("origin"));
            }
            intent.putExtras(extra);
            startActivityForResult(intent, REQUEST_ADD_ITEMS);
        } else {
            OAlert.showAlert(this, OResource.string(this, R.string.required_save));
        }
    }

    private class OnPartScrapChangeUpdate extends AsyncTask<ODomain, Void, Void> {

        @Override
        protected Void doInBackground(ODomain... params) {
            if (app.inNetwork()) {
                ODomain domain = params[0];
                List<ODataRow> rows = scrapParts.select(null, "id = ?", new String[]{"0"});
                List<ODataRow> photoRows = partScrapPhotos.select(null, "id = ?", new String[]{"0"});
                for (ODataRow row : rows) {
                    scrapParts.quickCreateRecord(row);
                }
                for (ODataRow row : photoRows) {
                    partScrapPhotos.quickCreateRecord(row);
                }
                /*Бусад бичлэгүүдийг update хийж байна*/
                scrapParts.quickSyncRecords(domain);
                partScrapPhotos.quickSyncRecords(domain);
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
        if (record == null && field.getFieldName().equals("technic")) {
            ODataRow techVal = (ODataRow) value;
            technicSync(techVal.getString("id"));
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

    public void technicSync(String serverTechId) {
        try {
            if (app.inNetwork()) {
                ODomain domain = new ODomain();
                domain.add("technic.id", "=", serverTechId);
                OnTechnicPartSync sync = new OnTechnicPartSync();
                sync.execute(domain);
            } else {
                Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class OnTechnicPartSync extends AsyncTask<ODomain, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.partScrapProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(ODomain... params) {
            try {
                technicParts.quickSyncRecords(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            findViewById(R.id.partScrapProgress).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_ITEMS && resultCode == Activity.RESULT_OK) {
            scrapPartLines.clear();
            for (String key : data.getExtras().keySet()) {
                if (data.getExtras().getBoolean(key)) {
                    scrapPartLines.add(technicParts.select(null, "_id = ?", new String[]{key}).get(0));
                }
            }
            drawPart(scrapPartLines);
        }
    }
}
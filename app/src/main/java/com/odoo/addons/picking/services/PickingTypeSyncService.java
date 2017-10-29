package com.odoo.addons.picking.services;

import android.content.Context;
import android.content.SyncResult;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.odoo.addons.stock.Models.PickingType;
import com.odoo.core.orm.OSQLite;
import com.odoo.core.service.ISyncFinishListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by baaska on 5/30/17.
 */

public class PickingTypeSyncService extends OSyncService implements ISyncFinishListener {
    public static final String TAG = PickingTypeSyncService.class.getSimpleName();

    private OSQLite sqLite = null;

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, PickingType.class, service, true);
    }

    public SQLiteDatabase getReadableDatabase() {
        return sqLite.getReadableDatabase();
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        if (adapter.getModel().getModelName().equals("stock.picking.type")) {
            adapter.syncDataLimit(80);
            adapter.onSyncFinish(this);
        }
    }

    @Override
    public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
//        ProductProduct product = new ProductProduct(getApplicationContext(), user);
//        PartsScrapReason partsScrapReason = new PartsScrapReason(getApplicationContext(), user);
//        partsScrapReason.quickSyncRecords(null);
//        product.quickSyncRecords(null);
        return null;
    }
}

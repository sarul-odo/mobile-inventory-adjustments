package com.odoo.addons.picking.services;

import android.content.Context;
import android.content.SyncResult;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

//import com.odoo.addons.picking.models.PartsScrapReason;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.addons.stock.Models.Picking;
//import com.odoo.addons.picking.models.TechnicParts;
import com.odoo.core.orm.OSQLite;
import com.odoo.core.service.ISyncFinishListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by baaska on 5/30/17.
 */

public class PickingSyncService extends OSyncService implements ISyncFinishListener {
    public static final String TAG = PickingSyncService.class.getSimpleName();

    private OSQLite sqLite = null;

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, Picking.class, service, true);
    }

    public SQLiteDatabase getReadableDatabase() {
        return sqLite.getReadableDatabase();
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {

        ProductProduct product = new ProductProduct(getApplicationContext(), user);
        product.quickSyncRecords(null);
        if (adapter.getModel().getModelName().equals("stock.picking")) {
            adapter.syncDataLimit(80);
            adapter.onSyncFinish(this);
        }
    }

    @Override
    public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
//        PartsScrapReason partsScrapReason = new PartsScrapReason(getApplicationContext(), user);
//        partsScrapReason.quickSyncRecords(null);
        return null;
    }
}

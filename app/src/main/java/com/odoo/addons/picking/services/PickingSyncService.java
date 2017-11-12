package com.odoo.addons.picking.services;

import android.content.Context;
import android.content.SyncResult;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

//import com.odoo.addons.picking.models.PartsScrapReason;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.addons.stock.Models.PackOperation;
import com.odoo.addons.stock.Models.Picking;
//import com.odoo.addons.picking.models.TechnicParts;
import com.odoo.core.orm.OSQLite;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.service.ISyncFinishListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baaska on 5/30/17.
 */

public class PickingSyncService extends OSyncService {
    public static final String TAG = PickingSyncService.class.getSimpleName();

    private OSQLite sqLite = null;
    private Context mContext = null;

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        this.mContext = context;
        return new OSyncAdapter(context, Picking.class, service, true);
    }

    public SQLiteDatabase getReadableDatabase() {
        return sqLite.getReadableDatabase();
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        ProductProduct product = new ProductProduct(mContext, user);
        product.quickSyncRecords(null);
        PackOperation packOperation = new PackOperation(mContext, user);
        packOperation.quickSyncRecords(null);
        if (adapter.getModel().getModelName().equals("stock.picking")) {
            adapter.syncDataLimit(80);
        }
    }
}

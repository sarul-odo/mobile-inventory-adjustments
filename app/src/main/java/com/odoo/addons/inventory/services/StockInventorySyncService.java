package com.odoo.addons.inventory.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.inventory.models.StockInventory;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/8/17.
 */

public class StockInventorySyncService extends OSyncService{

    public static final String TAG = StockInventorySyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(getApplicationContext(), StockInventory.class, this, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}

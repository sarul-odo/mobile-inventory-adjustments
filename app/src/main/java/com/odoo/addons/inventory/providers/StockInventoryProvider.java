package com.odoo.addons.inventory.providers;

import com.odoo.addons.inventory.models.StockInventory;
import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by ko on 9/8/17.
 */

public class StockInventoryProvider extends BaseModelProvider {

    public static final String TAG = StockInventoryProvider.class.getSimpleName();

    @Override
    public String authority() {
        return StockInventory.AUTHORITY;
    }
}
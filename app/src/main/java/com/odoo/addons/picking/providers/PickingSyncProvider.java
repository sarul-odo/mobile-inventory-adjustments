package com.odoo.addons.picking.providers;

import com.odoo.addons.stock.Models.Picking;
import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by baaska on 5/30/17.
 */
public class PickingSyncProvider extends BaseModelProvider {
    public static final String TAG = PickingSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return Picking.AUTHORITY;
    }
}

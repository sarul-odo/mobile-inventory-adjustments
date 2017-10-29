package com.odoo.addons.picking.providers;

import com.odoo.addons.stock.Models.Picking;
import com.odoo.addons.stock.Models.PickingType;
import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by baaska on 5/30/17.
 */
public class PickingTypeSyncProvider extends BaseModelProvider {
    public static final String TAG = PickingTypeSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return PickingType.AUTHORITY;
    }
}

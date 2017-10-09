package com.odoo.addons.picking.providers;

import com.odoo.addons.picking.models.Picking;
import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by baaska on 5/30/17.
 */
public class ScrapPartsSyncProvider extends BaseModelProvider {
    public static final String TAG = ScrapPartsSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return Picking.AUTHORITY;
    }
}

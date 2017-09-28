package com.odoo.addons.inventory;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.support.OUser;

public class OdooMobile extends OModel {
    public OdooMobile(Context context, OUser user) {
        super(context, "inventory.adjustment.mobile", user);
    }
}

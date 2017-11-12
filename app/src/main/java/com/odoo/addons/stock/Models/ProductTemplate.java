package com.odoo.addons.stock.Models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 10/5/17.
 */

public class ProductTemplate extends OModel {

    OColumn tracking = new OColumn("Tracking", OVarchar.class);

    public ProductTemplate(Context context, OUser user) {
        super(context, "product.template", user);
    }
}

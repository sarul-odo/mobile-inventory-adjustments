package com.odoo.addons.inventory.models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/14/17.
 */

public class ProductTemplate extends OModel {

    public static final String TAG = StockInventory.class.getSimpleName();

    OColumn name = new OColumn("Name", OVarchar.class);

    public ProductTemplate(Context context, OUser user){
        super(context, "product.template", user);
    }

}

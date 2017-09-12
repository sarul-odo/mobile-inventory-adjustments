package com.odoo.addons.inventory.models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/8/17.
 */

class StockInventoryLine  extends OModel {

    public static final String TAG = StockInventory.class.getSimpleName();

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);

    public StockInventoryLine(Context context, OUser user){
        super(context, "stock.inventory.line", user);
    }
}

package com.odoo.addons.inventory.models;

import android.content.Context;
import android.net.Uri;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/7/17.
 */

public class StockInventory extends OModel{

    public static final String TAG = StockInventory.class.getSimpleName();
    public static final String AUTHORITY = "com.odoo.addons.inventory.models.stock_inventory";

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
    OColumn filter = new OColumn("Filter", OBoolean.class);
    OColumn date = new OColumn("Date", ODateTime.class);
    OColumn line = new OColumn("Line", StockInventoryLine.class, OColumn.RelationType.OneToMany);

    public StockInventory(Context context, OUser user) {
        super(context, "stock.inventory", user);
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

}

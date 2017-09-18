package com.odoo.addons.inventory.models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/8/17.
 */

public class StockInventoryLine  extends OModel {

//    public static final String TAG = StockInventory.class.getSimpleName();

//    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn inventory_id = new OColumn("Inventory", StockInventory.class, OColumn.RelationType.ManyToOne);
    OColumn product_id = new OColumn("Product", ProductProduct.class, OColumn.RelationType.ManyToOne);
    OColumn theoretical_qty = new OColumn("Thoeretical qty", OFloat.class);
    OColumn product_qty = new OColumn("Product qty", OFloat.class);
//    OColumn ean13 = new OColumn("Ean13", ProductProduct.class, OColumn.RelationType.Related);

    public StockInventoryLine(Context context, OUser user){
        super(context, "stock.inventory.line", user);
    }
}

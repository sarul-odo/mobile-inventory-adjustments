package com.odoo.addons.stock.Models;

import android.content.Context;

import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 10/5/17.
 */

public class StockProductionLot extends OModel {

    OColumn name = new OColumn("Lot/Serial", OVarchar.class);
    OColumn product_id = new OColumn("Product", ProductProduct.class, OColumn.RelationType.ManyToOne);
    OColumn product_qty = new OColumn("Quantity", OFloat.class);


    public StockProductionLot(Context context, OUser user) {
        super(context, "stock.production.lot", user);
    }
}

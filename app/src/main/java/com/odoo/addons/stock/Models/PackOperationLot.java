package com.odoo.addons.stock.Models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 10/5/17.
 */

public class PackOperationLot extends OModel {

    OColumn operation_id = new OColumn("", PackOperation.class, OColumn.RelationType.ManyToOne);
    OColumn qty = new OColumn("Done", OFloat.class).setDefaultValue(1.0);
    OColumn lot_id = new OColumn("Lot", StockProductionLot.class, OColumn.RelationType.ManyToOne);
    OColumn lot_name = new OColumn("Lot", OVarchar.class);
    OColumn qty_todo = new OColumn("To do", OFloat.class).setDefaultValue(0.0);

    public PackOperationLot(Context context, OUser user) {
        super(context, "stock.pack.operation.lot", user);
    }
}

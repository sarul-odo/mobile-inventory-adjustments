package com.odoo.addons.stock.Models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/29/17.
 */

public class StockLocation extends OModel {

    public static final String TAG = StockLocation.class.getSimpleName();

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
    OColumn location_id = new OColumn("Parent Location", StockLocation.class, OColumn.RelationType.ManyToOne);

    public StockLocation(Context context, OUser user) {
        super(context, "stock.location", user);
    }

}

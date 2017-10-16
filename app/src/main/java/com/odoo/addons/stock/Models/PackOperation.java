/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 31/12/14 6:43 PM
 */
package com.odoo.addons.stock.Models;

import android.content.Context;
import android.util.Log;

import com.odoo.BuildConfig;
import com.odoo.addons.inventory.models.ProductProduct;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class PackOperation extends OModel {

    OColumn picking_id = new OColumn("Stock Picking", Picking.class, OColumn.RelationType.ManyToOne);
    OColumn product_id = new OColumn("Product", ProductProduct.class, OColumn.RelationType.ManyToOne);
    OColumn product_uom_id = new OColumn("Unit of Measure", ProductUom.class, OColumn.RelationType.ManyToOne);
    OColumn ordered_qty = new OColumn("Ordered Quantity", OFloat.class);
    OColumn product_qty = new OColumn("To Do", OFloat.class).setDefaultValue(0.0).setRequired();
    OColumn qty_done = new OColumn("Done", OFloat.class).setDefaultValue(0.0);
    @Odoo.Functional(method = "storeProductName", store = true, depends = {"product_id"})
    OColumn product_name = new OColumn("State Title", OVarchar.class);
    @Odoo.Functional(method = "storeProductUomName", store = true, depends = {"product_uom_id"})
    OColumn product_uom_name = new OColumn("State Title", OVarchar.class);

    public PackOperation(Context context, OUser user) {
        super(context, "stock.pack.operation", user);
    }

    public String storeProductName(OValues values) {
        try {
            if (!values.getString("product_id").equals("false")) {
                List<Object> product_id = (ArrayList<Object>) values.get("product_id");
                return product_id.get(1) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String storeProductUomName(OValues values) {
        try {
            if (!values.getString("product_uom_id").equals("false")) {
                List<Object> product_uom = (ArrayList<Object>) values.get("product_uom_id");
                return product_uom.get(1) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

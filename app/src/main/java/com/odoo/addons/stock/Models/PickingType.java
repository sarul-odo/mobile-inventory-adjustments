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

import com.odoo.BuildConfig;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import java.util.ArrayList;
import java.util.List;

public class PickingType extends OModel {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".core.provider.content.sync.picking_type";

    OColumn name = new OColumn("Нэр", OVarchar.class);
    OColumn count_picking_ready = new OColumn("Count", OInteger.class);
    OColumn code = new OColumn("Ажиллагааны төрөл", OSelection.class)
            .addSelection("incoming", "Нийлүүлэгчид")
            .addSelection("outgoing", "Хахилцагчид")
            .addSelection("internal", "Дотоод");
    OColumn warehouse_id = new OColumn("Агуулах", StockWarehouse.class, OColumn.RelationType.ManyToOne);

    @Odoo.Functional(method = "storeWareHouseName", store = true, depends = {"warehouse_id"})
    OColumn warehouse_name = new OColumn("Агуулахын нэр", OVarchar.class).setLocalColumn();


    public PickingType(Context context, OUser user) {
        super(context, "stock.picking.type", user);
    }

    public String storeWareHouseName(OValues values) {
        try {
            if (!values.getString("warehouse_id").equals("false")) {
                List<Object> house = (ArrayList<Object>) values.get("warehouse_id");
                return house.get(1) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

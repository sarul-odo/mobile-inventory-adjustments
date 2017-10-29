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
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class Picking extends OModel {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".core.provider.content.sync.picking";
    private Context mContext;
    private OUser user;
    private OPreferenceManager preferenceManager;

    OColumn name = new OColumn("Нэр", OVarchar.class);
    OColumn origin = new OColumn("Эх баримт", OVarchar.class);
    OColumn partner_id = new OColumn("Харилцагч", ResPartner.class, OColumn.RelationType.ManyToOne);
    OColumn location_id = new OColumn("Эх байрлалын бүс", StockLocation.class, OColumn.RelationType.ManyToOne);
    OColumn location_dest_id = new OColumn("Хүрэх байрлалын бүс", StockLocation.class, OColumn.RelationType.ManyToOne);
    OColumn picking_type_id = new OColumn("Бэлтгэх Төрөл", PickingType.class, OColumn.RelationType.ManyToOne);
    OColumn min_date = new OColumn("Товлогдсон огноо", ODateTime.class);
    OColumn pack_operation_product_ids = new OColumn("Ажилбарууд", PackOperation.class, OColumn.RelationType.OneToMany).setRelatedColumn("picking_id");
    OColumn state = new OColumn("Төлөв", OSelection.class)
            .addSelection("draft", "Ноорог")
            .addSelection("cancel", "Цуцлагдсан")
            .addSelection("waiting", "Өөр үйлдлийг хүлээж буй")
            .addSelection("confirmed", "Бэлэн болохыг хүлээж буй")
            .addSelection("partially_available", "Зарим хэсэг нь бэлэн")
            .addSelection("assigned", "Шилжүүлэхэд бэлэн")
            .addSelection("done", "Шилжсэн")
            .setDefaultValue("draft");

    @Odoo.Functional(store = true, depends = {"partner_id"}, method = "storePartnerName")
    OColumn partner_name = new OColumn("Харилцагчын нэр", OVarchar.class).setLocalColumn();

    public Picking(Context context, OUser user) {
        super(context, "stock.picking", user);
        this.mContext = context;
        this.user = user;
        preferenceManager = new OPreferenceManager(context);
    }

    public String storePartnerName(OValues value) {
        try {
            if (!value.getString("partner_id").equals("false")) {
                List<Object> partner = (ArrayList<Object>) value.get("partner_id");
                return partner.get(1) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ODomain defaultDomain() {
        ODomain domain = new ODomain();
        List<String> lidtDomain = new ArrayList<>();
        lidtDomain.add("assigned");
        lidtDomain.add("partially_available");
        domain.add("state", "in", lidtDomain);
        int data_limit = preferenceManager.getInt("sync_data_limit", 60);
        domain.add("create_date", ">=", ODateUtils.getDateBefore(data_limit));
        return domain;
    }

    @Override
    public boolean allowDeleteRecordOnServer() {
        return false;
    }

}

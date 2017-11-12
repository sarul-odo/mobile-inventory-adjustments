package com.odoo.addons.inventory.models;

import android.content.Context;

import com.odoo.addons.stock.Models.ProductTemplate;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 10/5/17.
 */

public class ProductProduct extends OModel {

//    public static final String TAG = ProductProduct.class.getSimpleName();

    OColumn default_code = new OColumn("Internal Reference", OVarchar.class);
    OColumn barcode = new OColumn("Barcode", OVarchar.class);
    OColumn product_tmpl_id = new OColumn("Product Template", ProductTemplate.class, OColumn.RelationType.ManyToOne);

    public ProductProduct(Context context, OUser user) {
        super(context, "product.product", user);
    }
}

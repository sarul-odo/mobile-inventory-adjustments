package com.odoo.addons.inventory.models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by ko on 9/14/17.
 */

public class ProductProduct  extends OModel {

//    public static final String TAG = StockInventory.class.getSimpleName();
//
//    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn barcode = new OColumn("Barcode", OVarchar.class).setSize(13);
    OColumn product_tmpl_id = new OColumn("Product template", ProductTemplate.class, OColumn.RelationType.ManyToOne);

    public ProductProduct(Context context, OUser user){
        super(context, "product.product", user);
    }

//    public int selectServer(int server_id) {
//        List<ODataRow> rows = select(null, "id = ?", new String[] {server_id + ""});
//        if (rows.size() > 0) {
//            for (ODataRow row : rows) {
//                return row.getInt("id");
//            }
//        }
//        return INVALID_ROW_ID;
//    }

}

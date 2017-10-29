package com.odoo.addons.picking;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.odoo.R;
import com.odoo.addons.stock.Models.Picking;
import com.odoo.addons.stock.Models.PickingType;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.ServerDataHelper;
import com.odoo.core.support.OUser;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class PickingTypes {

    private String name, stockName;
    private int numOfPicking;
    private int image;
    private String bg;
    private Context context;
    private String pickingType;

    public PickingTypes(Context context) {
        this.context = context;
    }

    public static PickingTypes fromCursor(Cursor cursor, Context context) {
        PickingTypes listItem = new PickingTypes(context);
        String wHouse = (cursor.getString(cursor.getColumnIndex("warehouse_name")));
        String tName = (cursor.getString(cursor.getColumnIndex("name")));
        listItem.setStockName(wHouse);
        listItem.setName(tName);
        listItem.setPickingType(cursor.getString(cursor.getColumnIndex("_id")));
        listItem.setNumOfPicking(cursor.getString(cursor.getColumnIndex("count_picking_ready")));
        listItem.setbg(cursor.getString(cursor.getColumnIndex("warehouse_id")));
        listItem.setImage(cursor.getString(cursor.getColumnIndex("code")));
        return listItem;
    }

    public String getName() {
        return name;
    }

    public String getStockName() {
        return stockName;
    }

    private void setStockName(String stockName) {
        this.stockName = stockName;
    }

    private void setName(String name) {
        this.name = name;
    }

    public int getNumOfPickng() {
        return numOfPicking;
    }

    public void setNumOfPicking(String id) {
        this.numOfPicking = Integer.parseInt(id);
//        Picking picking = new Picking(context, null);
//        Log.i("picking.getUser()===", picking.getUser().toString());
//        Object res = picking.getServerDataHelper().callMethod("picking_count", null);
//        ServerDataHelper connection = new ServerDataHelper(context, picking, picking.getUser());
//        Object res = connection.callMethod("picking_count", null);
//        this.numOfPicking = picking.PickingNumber();
//        this.numOfPicking = Integer.valueOf(id);
    }

    public void setPickingType(String id) {
        this.pickingType = id;
    }

    public String getPickingType() {
        return pickingType;
    }

    public int getImage() {
        return image;
    }

    private void setImage(String codeName) {
        switch (codeName) {
            case "incoming":
                image = R.drawable.shopping_card;
                break;
            case "internal":
                image = R.drawable.repeat;
                break;
            case "outgoing":
                image = R.drawable.local_shipping;
                break;
        }
    }

    public String getbg() {
        return bg;
    }

    private void setbg(String typeId) {
//        Picking picking = new Picking(context, null);
//        List<ODataRow> list = picking.select(new String[]{}, "picking_type_id", new String[]{typeId});
//        for (ODataRow row : list) {
//
//        }
        switch (typeId) {
            case "1":
                bg = "#ffeb3b";
                break;
            case "2":
                bg = "#8bc34a";
                break;
            case "3":
                bg = "#cddc39";
                break;
        }
    }
}
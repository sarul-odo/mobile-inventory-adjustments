package com.odoo.addons.picking;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.odoo.OdooActivity;
import com.odoo.R;

/**
 * Created by skyfishjy on 10/31/14.
 */
public class PickingTypeAdapter extends CursorRecyclerViewAdapter<PickingTypeAdapter.ViewHolder> {

    public Context context;
    public FragmentManager manager;

    public PickingTypeAdapter(Context context, Cursor cursor, FragmentManager manager) {
        super(context, cursor);
        this.context = context;
        this.manager = manager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView stockName, name, count;
        public ImageView pickingTypeImg;
        public CardView cardView;

        public ViewHolder(View view) {
            super(view);
            stockName = (TextView) view.findViewById(R.id.stockName);
            name = (TextView) view.findViewById(R.id.pickingTypeName);
            count = (TextView) view.findViewById(R.id.pickingCount);
            pickingTypeImg = (ImageView) view.findViewById(R.id.pickingTypeImg);
            cardView = (CardView) view.findViewById(R.id.card_view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_picking_cardview, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        PickingTypes myListItem = PickingTypes.fromCursor(cursor, context);
        holder.stockName.setText(myListItem.getStockName());
        holder.name.setText(myListItem.getName());
        holder.count.setText(myListItem.getNumOfPickng() + " picking");
        holder.pickingTypeImg.setImageResource(myListItem.getImage());
        holder.cardView.setBackgroundColor(Color.parseColor(myListItem.getbg()));
        final String pickingType = myListItem.getPickingType();
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                context.setTitle("Pickings");
                ReceiptPickings pickings = new ReceiptPickings(pickingType,manager);
                FragmentManager fragmentManager = manager;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, pickings);
                fragmentTransaction.commit();
            }
        });


//        holder.cardView.setBackgroundColor(Color.rgb(r, g, b));
//        holder.cardView.setBackgroundColor(Color.parseColor("#ffffff"));
    }
}
package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import org.w3c.dom.Text;

import static android.R.attr.name;
import static com.example.android.inventoryapp.data.InventoryProvider.LOG_TAG;

/**
 * Created by nicolaslacaze on 18/10/2016.
 */

public class ProductCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context,cursor, 0/* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.inventory_list_name);
        TextView priceView = (TextView) view.findViewById(R.id.inventory_list_price);
        TextView quantityView = (TextView) view.findViewById(R.id.inventory_list_quantity);
        TextView salesView = (TextView) view.findViewById(R.id.sales_record);

        //Get columns id ready for data extraction
        int columnIdIndex = cursor.getColumnIndex(ProductEntry._ID);
        int columnNameIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int columnPriceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int columnQuantityIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int columnSalesIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

        //Get the data from two specific columns of our cursor.
        String name = cursor.getString(columnNameIndex);
        int price = cursor.getInt(columnPriceIndex);
        final int quantity = cursor.getInt(columnQuantityIndex);
        final int id = cursor.getInt(columnIdIndex);
        final int sales = cursor.getInt(columnSalesIndex);

        //Set the retrieved data into our layout_item .
        nameView.setText(name);
        priceView.setText(String.valueOf(price));
        quantityView.setText(String.valueOf(quantity));
        salesView.setText(String.valueOf(sales));

        Button sellProduct = (Button) view.findViewById(R.id.sell_product);
        sellProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Create current product Uri based on its extracted _id
                Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                /*Decrease product quantity by one at each click. When quantity is equal to 0,
                display warning message to user. */
                int updatedProductQuantity;
                int updatedProductSales;
                if (quantity > 0) {
                    updatedProductQuantity = quantity - 1;
                    updatedProductSales = sales + 1;
                } else {
                    updatedProductQuantity = quantity;
                    updatedProductSales = sales;
                    Toast.makeText(context, R.string.run_out_of_stock, Toast.LENGTH_SHORT).show();
                }

                //Save new values in ContentValues using put method
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, updatedProductQuantity);
                values.put(ProductEntry.COLUMN_PRODUCT_SALES, updatedProductSales);

                //Save new values using ContentResolver.update method from your ContentProvider
                context.getContentResolver().update(productUri, values, null, null);

                //Refresh views display after update was done
                notifyDataSetInvalidated();
            }
        });
    }
}

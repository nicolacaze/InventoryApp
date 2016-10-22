package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * This helper class will help us to create new tables
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    //Database version
    public static final int DB_VERSION = 3;

    //Constant database name
    public static final String DB_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {
        super(context,DB_NAME, null, DB_VERSION);
    }

    //Creates new tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + "("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL,"
                + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL,"
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE_PATH + " TEXT,"
                + ProductEntry.COLUMN_PRODUCT_SALES + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL,"
                + ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + " TEXT NOT NULL" + ");";
        db.execSQL(CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);

        //Creates a new table
        onCreate(db);
    }
}

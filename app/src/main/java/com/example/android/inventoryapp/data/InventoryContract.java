package com.example.android.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by nicolaslacaze on 17/10/2016.
 */

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_PRODUCT_NAME = "name";

        public static final String COLUMN_PRODUCT_PRICE = "price";

        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        public static final String COLUMN_PRODUCT_IMAGE_RESOURCE_PATH = "image_resource_path";

        public static final String COLUMN_PRODUCT_SALES = "sales_record";

        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";

        public static final String COLUMN_PRODUCT_SUPPLIER_EMAIL = "supplier_email";
    }


}

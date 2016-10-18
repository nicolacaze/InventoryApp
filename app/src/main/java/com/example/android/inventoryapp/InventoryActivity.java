package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import static com.example.android.inventoryapp.data.InventoryProvider.LOG_TAG;


public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private ProductCursorAdapter mCursorAdapter;

    private InventoryDbHelper mDbHelper;

    //Define Loader ID
    private static final int PET_LOADER = 0;

    // These are the pet rows that we will retrieve
    private static final String[] PROJECTION = new String[]{
            ProductEntry._ID,
            ProductEntry.COLUMN_PRODUCT_NAME,
            ProductEntry.COLUMN_PRODUCT_PRICE,
            ProductEntry.COLUMN_PRODUCT_QUANTITY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        //Find our ListView UI element
        ListView productListView = (ListView) findViewById(R.id.listview_items);

        //TODO:SET EMPTY VIEW

        // Create an empty adapter we will use to display the loaded data.
        mCursorAdapter = new ProductCursorAdapter(this, null);

        //Set the petAdapter to our ListView UI
        productListView.setAdapter(mCursorAdapter);

        mDbHelper = new InventoryDbHelper(this);

        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/inventory_activity_menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.inventory_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                //TODO: do something
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertProduct() {

        //Store pair of values into a ContentValues object.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Box");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 10);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 3);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE_PATH, 7);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "Big Shop");
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, "bigshop@gmail.com");

        //Insert set of values into the database.
        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        //Takes action based on the ID of the Loader that's being created
        switch (id) {
            case PET_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(this,       // Parent activity context
                        ProductEntry.CONTENT_URI,   // Table to query
                        PROJECTION,                 // Projection to return
                        null,                       // No selection clause
                        null,                       // No selection arguments
                        null);                      // Default sort order
            default:
                // An invalid id was passed in
                return null;
        }
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.
        mCursorAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}

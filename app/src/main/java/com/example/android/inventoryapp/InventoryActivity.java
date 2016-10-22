package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import java.io.FileDescriptor;
import java.io.PrintWriter;


public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = InventoryActivity.class.getSimpleName();

    private ProductCursorAdapter mCursorAdapter;

    //Define Loader ID
    private static final int PET_LOADER = 0;

    // These are the pet rows that we will retrieve
    private static final String[] PROJECTION = new String[]{
            ProductEntry._ID,
            ProductEntry.COLUMN_PRODUCT_NAME,
            ProductEntry.COLUMN_PRODUCT_PRICE,
            ProductEntry.COLUMN_PRODUCT_QUANTITY,
            ProductEntry.COLUMN_PRODUCT_SALES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        //Find our ListView UI element
        ListView productListView = (ListView) findViewById(R.id.listview_items);

        //Set empty view when no items are to be displayed
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Create an empty adapter we will use to display the loaded data.
        mCursorAdapter = new ProductCursorAdapter(this, null);

        //Set the petAdapter to our ListView UI
        productListView.setAdapter(mCursorAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create an Intent to open ProductInformation Activity
                Intent seeProductDetails = new Intent(InventoryActivity.this, ProductInformation.class);

                //Create a complete Uri from product's id
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //Add current Uri to data field of our Intent and start the activity.
                seeProductDetails.setData(currentProductUri);
                startActivity(seeProductDetails);
            }
        });
        getLoaderManager().initLoader(PET_LOADER, null, this);

        Button addProduct = (Button) findViewById(R.id.add_product);
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addNewProduct = new Intent(InventoryActivity.this, AddProduct.class);
                startActivity(addNewProduct);
            }
        });
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
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.total_delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProducts() {

        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            // If the number of rows deleted is 0, then there was an error with delete.
            Toast.makeText(this, getString(R.string.reset_inventory_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.reset_inventory_successful),
                    Toast.LENGTH_SHORT).show();
        }
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

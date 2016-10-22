package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.content.Loader;
import android.content.CursorLoader;
import android.app.LoaderManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.io.FileDescriptor;
import java.io.IOException;


public class ProductInformation extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProductInformation.class.getSimpleName();
    //Define Loader ID
    private static final int PRODUCT_LOADER = 1;
    //Find associated TextViews
    private TextView mNameView;
    private TextView mPriceView;
    private TextView mQuantityView;
    private ImageView mImageView;
    private TextView mSupplierNameView;
    private TextView mSalesView;
    private EditText mShipmentReceived;
    private EditText mItemsSold;
    private Uri mCurrentProductUri;

    private String mProductName;

    private int mProductQuantity;

    private String mSupplierEmail;

    private String mSupplierName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);

        //Get Uri from current product
        mCurrentProductUri = getIntent().getData();

        mNameView = (TextView) findViewById(R.id.product_information_name);
        mPriceView = (TextView) findViewById(R.id.product_information_price);
        mQuantityView = (TextView) findViewById(R.id.product_information_quantity);
        mImageView = (ImageView) findViewById(R.id.product_image);
        mSupplierNameView = (TextView) findViewById(R.id.product_information_supplier_name);
        mSalesView = (TextView) findViewById(R.id.product_information_sales);
        mShipmentReceived = (EditText) findViewById(R.id.product_information_shipment_received);
        mItemsSold= (EditText) findViewById(R.id.product_information_items_sold);

        //Initiate our Loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        Button orderButton = (Button) findViewById(R.id.product_information_order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeAnOrder();
            }
        });

        Button updateButton = (Button) findViewById(R.id.product_information_update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout updatesView = (LinearLayout) findViewById(R.id.product_information_updates_view);
                updatesView.setVisibility(View.VISIBLE);
            }
        });
    }

    /*Helper method to send an email to our supplier*/
    private void makeAnOrder() {

        String subject = getString(R.string.new_order) + mSupplierName;
        String message = String.format(getString(R.string.email_content), mProductName);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mSupplierEmail});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i(LOG_TAG, "Email was sent successfully");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.no_client_email), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_information_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete product" menu option
            case R.id.action_delete_product:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_save_quantity:
                saveQuantityChanges();
                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                NavUtils.navigateUpFromSameTask(ProductInformation.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteProduct() {

        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
        if (rowsDeleted == 0) {
            // If the number of rows deleted is 0, then there was an error with delete.
            Toast.makeText(this, getString(R.string.delete_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
        //Return to previous activity when done
        finish();
    }

    private void saveQuantityChanges() {

        //Get input text and store it as a String.
        String stockInString = mShipmentReceived.getText().toString().trim();
        String stockOutString = mItemsSold.getText().toString().trim();

        int stockIn;
        int stockOut;

        //Check if data were input by user, if not assign value 0.
        if (stockInString.isEmpty()) {
            stockIn = 0;
        } else {
            stockIn = Integer.parseInt(stockInString);
        }
        //Check if data were input by user, if not assign value 0
        if (stockOutString.isEmpty()) {
            stockOut = 0;
        } else {
            stockOut = Integer.parseInt(stockOutString);
        }

        //Calculate new quantity taking into account new user's input
        int newQuantity = mProductQuantity + stockIn - stockOut;

        //We check that stock cannot be negative. Otherwise return
        if (newQuantity < 0) {
            Toast.makeText(this, getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
            return;
            //Assign the new quantity value
        } else {
            mProductQuantity = newQuantity;
        }

        //Prepare values ready to update into the database
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mProductQuantity);
        int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);
        // Show a toast message depending on whether or not the update was successful
        if (rowsUpdated == 0) {
            // If the new content URI is null, then there was an error with update.
            Toast.makeText(this, getString(R.string.update_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.update_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
        //Return to inventory list
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE_PATH,
                ProductEntry.COLUMN_PRODUCT_SALES,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Cursor starts at position 0.
        if (data.moveToFirst()) {
            //Get column id for each fields.
            int columnNameIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int columnPriceIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int columnQuantityIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int columnImageResourcePathIndex = data.getColumnIndex(
                    ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE_PATH);
            int columnSalesIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);
            int columnSupplierNameIndex = data.getColumnIndex(
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int columnSupplierEmailIndex = data.getColumnIndex(
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);

            //Extract data from cursor
            mProductName = data.getString(columnNameIndex);
            int productPrice = data.getInt(columnPriceIndex);
            mProductQuantity = data.getInt(columnQuantityIndex);
            String imageResourcePath = data.getString(columnImageResourcePathIndex);
            int productSales = data.getInt(columnSalesIndex);
            mSupplierName = data.getString(columnSupplierNameIndex);
            mSupplierEmail = data.getString(columnSupplierEmailIndex);

            //Set accurate values to our Views
            mNameView.setText(mProductName);
            mPriceView.setText(String.valueOf(productPrice));
            mQuantityView.setText(String.valueOf(mProductQuantity));
            mSupplierNameView.setText(mSupplierName);
            mSalesView.setText(String.valueOf(productSales));

            //Make an Uri from our retrieved image resource path
            Uri imageUri = Uri.parse(imageResourcePath);

            //Transform the obtained Uri into a Bitmap
            Bitmap selectedImage = getBitmapFromUri(imageUri);
            //Check if it is null before authorizing display
            if (selectedImage != null) {
                mImageView.setImageBitmap(selectedImage);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Clear all fields
        mNameView.setText("");
        mPriceView.setText("");
        mQuantityView.setText("");
        mSalesView.setText("");
        mSupplierNameView.setText("");
    }
}

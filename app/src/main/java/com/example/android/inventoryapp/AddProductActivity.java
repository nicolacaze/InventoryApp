package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.example.android.inventoryapp.data.InventoryProvider.LOG_TAG;

public class AddProductActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    //Declare all input fields as global variables
    private EditText productNameView;
    private EditText productPriceView;
    private EditText productQuantityView;
    private EditText supplierNameView;
    private EditText supplierEmailView;
    private Uri mImageUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        productNameView = (EditText) findViewById(R.id.add_product_name);
        productPriceView = (EditText) findViewById(R.id.add_product_price);
        productQuantityView = (EditText) findViewById(R.id.add_product_quantity);
        supplierNameView = (EditText) findViewById(R.id.add_supplier_name);
        supplierEmailView = (EditText) findViewById(R.id.add_supplier_email);

        //Save button calls for the insertProduct() method
        Button saveInput = (Button) findViewById(R.id.action_save_input);
        saveInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
                //Return to Inventory Activity
                finish();
            }
        });

        imageView = (ImageView) findViewById(R.id.imported_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent importImage = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(importImage, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            mImageUri = data.getData();

            imageView.setImageBitmap(getBitmapFromUri(mImageUri));
        }
    }

    //Helper method to transform a Uri into a Bitmap object
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

    //Helper method to get user input and store them properly into our database
    private void saveProduct() {
        //Extract each data from fields
        String productName = productNameView.getText().toString().trim();
        String productPriceString = productPriceView.getText().toString().trim();
        String productQuantityString = productQuantityView.getText().toString().trim();
        String supplierName = supplierNameView.getText().toString().trim();
        String supplierEmail = supplierEmailView.getText().toString().trim();

        //Make a check whether any field is empty or email is correctly formatted.
        //If true, leave the method, do not save product.
        if (productName.isEmpty() || supplierName.isEmpty() || supplierEmail.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_information), Toast.LENGTH_SHORT).show();
            //return;
        }
        if (!supplierEmail.contains("@")) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            //return;
        }

        int productPrice;
        int productQuantity;
        //If price field is left empty, return.
        if (productPriceString.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_information), Toast.LENGTH_SHORT).show();
            return;
        } else {
            productPrice = Integer.parseInt(productPriceString);
        }

        //If quantity field is left empty, return.
        if (productQuantityString.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_information), Toast.LENGTH_SHORT).show();
            return;
        } else {
            productQuantity = Integer.parseInt(productQuantityString);
        }

        //Check that an image was imported before calling insert() method
        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.no_image), Toast.LENGTH_SHORT).show();
            //return;
        }

        //Create a ContentValues object and associate each relevant data extracted with its key column
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE_RESOURCE_PATH, mImageUri.toString());
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmail);

        //Call ContentProvider to insert our new values
        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (uri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.insert_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.insert_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
}

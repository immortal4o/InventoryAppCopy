package com.example.android.inventoryapp;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri myCurrentUri;

    private TextView myBookNameText;

    private TextView myBookQuantityText;

    private TextView myBookPriceText;

    private TextView mySupplierNameText;

    private TextView mySupplierPhoneText;

    private ImageView myBuyButton;

    private ImageView mySellButton;

    private int myQuantity;

    private Context myContext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle(R.string.book_details);

        Intent intent = getIntent();
        myCurrentUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        myBookNameText = findViewById(R.id.detail_book_name);
        myBookQuantityText = findViewById(R.id.detail_book_quantity);
        myBookPriceText = findViewById(R.id.detail_book_price);
        mySupplierNameText = findViewById(R.id.detail_supplier_name);
        mySupplierPhoneText = findViewById(R.id.detail_supplier_phone);
        myBuyButton = findViewById(R.id.detail_button_plus);
        mySellButton = findViewById(R.id.detail_button_minus);

        myContext = getApplicationContext();

        myBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyBook();
            }
        });

        mySellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellBook();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(
                this,
                myCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            myBookNameText.setText(name);
            myBookQuantityText.setText(Integer.toString(quantity));
            myBookPriceText.setText(Double.toString(price) + " $");
            mySupplierNameText.setText(supplierName);
            mySupplierPhoneText.setText(supplierPhone);

            myQuantity = quantity;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        myBookNameText.setText("");
        myBookQuantityText.setText("");
        myBookPriceText.setText("");
        mySupplierNameText.setText("");
        mySupplierPhoneText.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_delete_book:
                showDeleteConfirmDialog();
                return true;

            case R.id.edit_a_book:
                Intent intent = new Intent(this, EditorActivity.class);
                intent.setData(myCurrentUri);
                startActivity(intent);
                return true;

            case R.id.call_supplier:
                callSupplier();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.book_delete_dialog);
        builder.setPositiveButton(R.string.delete_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (myCurrentUri != null) {
            int rowDeleted = getContentResolver().delete(myCurrentUri, null, null);

            if (rowDeleted == 0) {
                Toast.makeText(this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.book_deleted, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void sellBook() {

        int numberOfRows = 0;
        if (myQuantity > 0) {
            myQuantity--;
            ContentValues contentValues = new ContentValues();
            contentValues.put(InventoryEntry.COLUMN_QUANTITY, myQuantity);
            numberOfRows = myContext.getContentResolver().update(
                    myCurrentUri,
                    contentValues,
                    null, null);
        }

        if (numberOfRows == 0) {
            Toast.makeText(myContext, R.string.string_no_book_left, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(myContext, R.string.string_sold_book, Toast.LENGTH_SHORT).show();
        }
    }

    private void buyBook() {

        myQuantity++;

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_QUANTITY, myQuantity);
        myContext.getContentResolver().update(
                myCurrentUri,
                contentValues,
                null, null);

        Toast.makeText(myContext, R.string.add_book, Toast.LENGTH_SHORT).show();
    }

    private void callSupplier() {
        String phone = mySupplierPhoneText.getText().toString().trim();

        if (phone.equals("")) {
            Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_SHORT).show();
        } else {
            Intent intentCall = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + phone));
            startActivity(intentCall);
        }
    }
}
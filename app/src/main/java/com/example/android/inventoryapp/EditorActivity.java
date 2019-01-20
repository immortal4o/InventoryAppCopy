package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri myCurrentUri;

    private EditText myBookNameEditText;

    private EditText myBookQuantityEditText;

    private EditText myBookPriceEditText;

    private EditText mySupplierNameEditText;

    private EditText mySupplierPhoneEditText;

    private boolean myBookHasChanges;

    private boolean hasRequiredFields = false;

    private View.OnTouchListener myViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            myBookHasChanges = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        myCurrentUri = intent.getData();

        if (myCurrentUri == null) {
            setTitle(R.string.string_add_a_book);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.string_edit_a_book);
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        myBookNameEditText = findViewById(R.id.edit_book_name);
        myBookQuantityEditText = findViewById(R.id.edit_book_quantity);
        myBookPriceEditText = findViewById(R.id.edit_book_price);
        mySupplierNameEditText = findViewById(R.id.edit_book_supplier_name);
        mySupplierPhoneEditText = findViewById(R.id.edit_book_supplier_phone);

        myBookNameEditText.setOnTouchListener(myViewTouchListener);
        myBookQuantityEditText.setOnTouchListener(myViewTouchListener);
        myBookPriceEditText.setOnTouchListener(myViewTouchListener);
        mySupplierNameEditText.setOnTouchListener(myViewTouchListener);
        mySupplierPhoneEditText.setOnTouchListener(myViewTouchListener);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE,
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

            myBookNameEditText.setText(name);
            myBookQuantityEditText.setText(Integer.toString(quantity));
            myBookPriceEditText.setText(Double.toString(price));
            mySupplierNameEditText.setText(supplierName);
            mySupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        myBookNameEditText.setText("");
        myBookQuantityEditText.setText("");
        myBookPriceEditText.setText("");
        mySupplierNameEditText.setText("");
        mySupplierPhoneEditText.setText("");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (myCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.edit_delete_a_book);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_delete_a_book:
                showDeleteConfirmDialog();
                return true;
            case R.id.save_a_book:
                saveBook();
                if (hasRequiredFields) {
                    finish();
                }
                return true;
            case android.R.id.home:
                if (!myBookHasChanges) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardDialog = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardDialog);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private boolean saveBook() {

        String nameString = myBookNameEditText.getText().toString().trim();
        String quantityString = myBookQuantityEditText.getText().toString().trim();
        String priceString = myBookPriceEditText.getText().toString().trim();
        String supplierNameString = mySupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mySupplierPhoneEditText.getText().toString().trim();

        if (myCurrentUri == null && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(supplierNameString)
                && TextUtils.isEmpty(supplierPhoneString)) {
            hasRequiredFields = true;
            return hasRequiredFields;
        }

        ContentValues contentValues = new ContentValues();

        if (!TextUtils.isEmpty(nameString)) {
            contentValues.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        } else {
            Toast.makeText(this, R.string.name_needed_toast, Toast.LENGTH_SHORT).show();
            return hasRequiredFields;

        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        contentValues.put(InventoryEntry.COLUMN_QUANTITY, quantity);

        Double price = 999.99;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        contentValues.put(InventoryEntry.COLUMN_PRICE, price);

        if (!TextUtils.isEmpty(supplierNameString)) {
        contentValues.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        } else {
            Toast.makeText(this, R.string.supplier_needed_toast, Toast.LENGTH_SHORT).show();
            return hasRequiredFields;
        }

        if (!TextUtils.isEmpty(supplierPhoneString)) {
        contentValues.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);
        } else {
            Toast.makeText(this, R.string.phone_needed_toast, Toast.LENGTH_SHORT).show();
            return hasRequiredFields;
        }

        if (myCurrentUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, contentValues);

            if (newUri == null) {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.book_saved, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(myCurrentUri, contentValues,
                    null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, R.string.error_updating, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.book_updated, Toast.LENGTH_SHORT).show();
            }
        }
        hasRequiredFields = true;
        return hasRequiredFields;
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

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener dialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);
        builder.setPositiveButton(R.string.discard, dialog);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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

    @Override
    public void onBackPressed() {
        if (!myBookHasChanges) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardDialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardDialog);
    }
}

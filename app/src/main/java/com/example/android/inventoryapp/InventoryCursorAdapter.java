package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;


public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_items, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ImageView sellOneView = view.findViewById(R.id.list_sell);
        TextView textViewName = view.findViewById(R.id.list_book_name);
        TextView textViewQuantity = view.findViewById(R.id.list_book_quantity);
        TextView textViewPrice = view.findViewById(R.id.list_book_price);

        int bookIdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);

        final int bookId = cursor.getInt(bookIdColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);

        textViewName.setText(bookName);
        textViewQuantity.setText(context.getText(R.string.quantity)
                + ": " + String.valueOf(bookQuantity));
        textViewPrice.setText(context.getText(R.string.price)
                + ": " + bookPrice + " $");

        sellOneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri bookUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, bookId);
                sellBook(context, bookUri, bookQuantity);
            }
        });
    }

    private void sellBook(Context context, Uri uri, int quantity) {

        int numberOfRows = 0;
        if (quantity > 0) {
            quantity--;
            ContentValues contentValues = new ContentValues();
            contentValues.put(InventoryEntry.COLUMN_QUANTITY, quantity);
            numberOfRows = context.getContentResolver().update(
                    uri,
                    contentValues,
                    null, null);
        }

        if (numberOfRows == 0) {
            Toast.makeText(context.getApplicationContext(),
                    R.string.string_no_book_left, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context.getApplicationContext(),
                    R.string.string_sold_book, Toast.LENGTH_SHORT).show();
        }
    }
}

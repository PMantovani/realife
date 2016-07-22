package com.example.mantovani.makeitdark.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Mantovani on 11-Jul-16.
 */
public class ProductivityProvider extends ContentProvider {

    // URI Matcher used by the content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ProductivityDbHelper mOpenHelper;

    static final int DAY = 100;
    static final int DAY_WITH_HOUR = 101;
    static final int DAY_WITH_INTERVAL = 102;

    //private static final SQLiteQueryBuilder sDayQueryBuilder;

    private static final String sDaySelection =
            ProductivityContract.DayEntry.TABLE_NAME +
                    "." + ProductivityContract.DayEntry.COLUMN_DATE +
                    " = ? ";

    private static final String sDayAndHourSelection =
            ProductivityContract.DayEntry.TABLE_NAME +
                    "." + ProductivityContract.DayEntry.COLUMN_DATE +
                    " = ? AND " + ProductivityContract.DayEntry.COLUMN_DATE +
                    " = ? ";

    private static final String sInitialAndFinalDaySelection =
            ProductivityContract.DayEntry.TABLE_NAME +
                    "." + ProductivityContract.DayEntry.COLUMN_DATE_INT +
                    " >= ? AND " + ProductivityContract.DayEntry.COLUMN_DATE_INT +
                    " <= ? ";

    private Cursor getDay(Uri uri, String[] projection, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String date = ProductivityContract.DayEntry.getDateFromUri(uri);
        String[] selectionArgs = {date};

        return db.query(
                ProductivityContract.DayEntry.TABLE_NAME,
                projection,
                sDaySelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getIntervalOfDays(Uri uri, String[] projection, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] selectionArgs = ProductivityContract.DayEntry.getInitialAndFinalDatesFromUri(uri);

        return db.query(
                ProductivityContract.DayEntry.TABLE_NAME,
                projection,
                sInitialAndFinalDaySelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // Creates an UriMatcher to distinguish between requests for different Uris
    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ProductivityContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ProductivityContract.PATH_DAY+"/*", DAY);
        // Day with hour will not work for now, nor it is being used
       // matcher.addURI(authority, ProductivityContract.PATH_DAY+"/*", DAY_WITH_HOUR);
        matcher.addURI(authority, ProductivityContract.PATH_DAY+"/*/*", DAY_WITH_INTERVAL);

        return matcher;
    }

    // Instantiate database helper
    @Override
    public boolean onCreate() {
        mOpenHelper = new ProductivityDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use UriMatcher to determine what kind of URI this is
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case DAY:
                return ProductivityContract.DayEntry.CONTENT_TYPE;
            case DAY_WITH_INTERVAL:
                return ProductivityContract.DayEntry.CONTENT_TYPE;
            case DAY_WITH_HOUR:
                return ProductivityContract.DayEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            // "day"
            case DAY: {
                // NOT sure whether this is correct
                retCursor = getDay(uri, projection, sortOrder);
                break;
            }
            // "day/#"
            case DAY_WITH_HOUR: {
                // NOT sure whether this is correct
                retCursor = getDay(uri, projection, sortOrder); // Should NOT work for now
                break;
            }
            case DAY_WITH_INTERVAL: {
                // Interval is included in uri
                retCursor = getIntervalOfDays(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert (Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case DAY: {
                long _id = db.insert(ProductivityContract.DayEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ProductivityContract.DayEntry.buildDayUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // Probably done so program doesn't crash
        if (selection == null) selection = "1";
        switch (match) {
            case DAY: {
                rowsDeleted = db.delete(ProductivityContract.DayEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case DAY: {
                rowsUpdated = db.update(ProductivityContract.DayEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    // Do we really need this?
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}

package com.example.mantovani.makeitdark.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mantovani.makeitdark.data.ProductivityContract.DayEntry;

/**
 * Created by Mantovani on 11-Jul-16.
 */
public class ProductivityDbHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "productivity.db";

    public ProductivityDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Creates a table to hold productivity information
        final String SQL_CREATE_DAY_TABLE = "CREATE TABLE " + DayEntry.TABLE_NAME + " (" +
                DayEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DayEntry.COLUMN_HOUR00 + " REAL, " +
                DayEntry.COLUMN_HOUR01 + " REAL, " +
                DayEntry.COLUMN_HOUR02 + " REAL, " +
                DayEntry.COLUMN_HOUR03 + " REAL, " +
                DayEntry.COLUMN_HOUR04 + " REAL, " +
                DayEntry.COLUMN_HOUR05 + " REAL, " +
                DayEntry.COLUMN_HOUR06 + " REAL, " +
                DayEntry.COLUMN_HOUR07 + " REAL, " +
                DayEntry.COLUMN_HOUR08 + " REAL, " +
                DayEntry.COLUMN_HOUR09 + " REAL, " +
                DayEntry.COLUMN_HOUR10 + " REAL, " +
                DayEntry.COLUMN_HOUR11 + " REAL, " +
                DayEntry.COLUMN_HOUR12 + " REAL, " +
                DayEntry.COLUMN_HOUR13 + " REAL, " +
                DayEntry.COLUMN_HOUR14 + " REAL, " +
                DayEntry.COLUMN_HOUR15 + " REAL, " +
                DayEntry.COLUMN_HOUR16 + " REAL, " +
                DayEntry.COLUMN_HOUR17 + " REAL, " +
                DayEntry.COLUMN_HOUR18 + " REAL, " +
                DayEntry.COLUMN_HOUR19 + " REAL, " +
                DayEntry.COLUMN_HOUR20 + " REAL, " +
                DayEntry.COLUMN_HOUR21 + " REAL, " +
                DayEntry.COLUMN_HOUR22 + " REAL, " +
                DayEntry.COLUMN_HOUR23 + " REAL, " +
                DayEntry.COLUMN_DATE + " TEXT NOT NULL UNIQUE " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_DAY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Executed when database version (schema) is updated
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DayEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}

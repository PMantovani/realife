package com.example.mantovani.makeitdark.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Mantovani on 11-Jul-16.
 */
public class ProductivityContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_DAY = "day";

    // Empty constructor to prevent accidentally instantiation of contract class
    public ProductivityContract() {}

    public static final class DayEntry implements BaseColumns {
        public static final String TABLE_NAME = "day";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DAY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DAY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DAY;


        // Columns to store the percentage of time spent on the hour per hour
        public static final String COLUMN_HOUR00 = "hour00";
        public static final String COLUMN_HOUR01 = "hour01";
        public static final String COLUMN_HOUR02 = "hour02";
        public static final String COLUMN_HOUR03 = "hour03";
        public static final String COLUMN_HOUR04 = "hour04";
        public static final String COLUMN_HOUR05 = "hour05";
        public static final String COLUMN_HOUR06 = "hour06";
        public static final String COLUMN_HOUR07 = "hour07";
        public static final String COLUMN_HOUR08 = "hour08";
        public static final String COLUMN_HOUR09 = "hour09";
        public static final String COLUMN_HOUR10 = "hour10";
        public static final String COLUMN_HOUR11 = "hour11";
        public static final String COLUMN_HOUR12 = "hour12";
        public static final String COLUMN_HOUR13 = "hour13";
        public static final String COLUMN_HOUR14 = "hour14";
        public static final String COLUMN_HOUR15 = "hour15";
        public static final String COLUMN_HOUR16 = "hour16";
        public static final String COLUMN_HOUR17 = "hour17";
        public static final String COLUMN_HOUR18 = "hour18";
        public static final String COLUMN_HOUR19 = "hour19";
        public static final String COLUMN_HOUR20 = "hour20";
        public static final String COLUMN_HOUR21 = "hour21";
        public static final String COLUMN_HOUR22 = "hour22";
        public static final String COLUMN_HOUR23 = "hour23";
        public static final String COLUMN_DATE = "date";

        public static Uri buildDayUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDayWithHour(String day, int hour) {
            return CONTENT_URI.buildUpon().appendPath(day)
                    .appendPath(Integer.toString(hour)).build();
        }

        public static Uri buildDay (String day) {
            return CONTENT_URI.buildUpon().appendPath(day).build();
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}

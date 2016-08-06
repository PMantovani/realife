package com.example.mantovani.makeitdark.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Mantovani on 11-Jul-16.
 */
public class ProductivityContract {
    public static final String CONTENT_AUTHORITY = "com.example.mantovani.makeitdark";
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
        public static final String COLUMN_HOUR00 = "h0";
        public static final String COLUMN_HOUR01 = "h1";
        public static final String COLUMN_HOUR02 = "h2";
        public static final String COLUMN_HOUR03 = "h3";
        public static final String COLUMN_HOUR04 = "h4";
        public static final String COLUMN_HOUR05 = "h5";
        public static final String COLUMN_HOUR06 = "h6";
        public static final String COLUMN_HOUR07 = "h7";
        public static final String COLUMN_HOUR08 = "h8";
        public static final String COLUMN_HOUR09 = "h9";
        public static final String COLUMN_HOUR10 = "h10";
        public static final String COLUMN_HOUR11 = "h11";
        public static final String COLUMN_HOUR12 = "h12";
        public static final String COLUMN_HOUR13 = "h13";
        public static final String COLUMN_HOUR14 = "h14";
        public static final String COLUMN_HOUR15 = "h15";
        public static final String COLUMN_HOUR16 = "h16";
        public static final String COLUMN_HOUR17 = "h17";
        public static final String COLUMN_HOUR18 = "h18";
        public static final String COLUMN_HOUR19 = "h19";
        public static final String COLUMN_HOUR20 = "h20";
        public static final String COLUMN_HOUR21 = "h21";
        public static final String COLUMN_HOUR22 = "h22";
        public static final String COLUMN_HOUR23 = "h23";
        public static final String COLUMN_DAY_PERCENTAGE = "percentage";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DATE_INT = "dateInt";
        public static final String COLUMN_PICKS = "picks";

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

        public static Uri buildWithDayInterval (String day1, String day2) {
            return CONTENT_URI.buildUpon().appendPath(day1)
                    .appendPath(day2).build();
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String[] getInitialAndFinalDatesFromUri(Uri uri) {
            String[] str = {uri.getPathSegments().get(1),
                            uri.getPathSegments().get(2)};
            return str;
        }

        public static String[] getDatesFromInitialDateAndInterval (Uri uri) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            ArrayList<String> datesArray = new ArrayList<>();
            String date1 = uri.getPathSegments().get(1);
            String date2 = uri.getPathSegments().get(2);

            try {
                Date parsedDate1 = sdf.parse(date1);
                Date parsedDate2 = sdf.parse(date2);
                Calendar cal1 = new GregorianCalendar();
                Calendar cal2 = new GregorianCalendar();
                cal1.setTime(parsedDate1);
                cal2.setTime(parsedDate2);
                int diffInDays = 0;

                // Check if nothing goes wrong on the same date
                while (parsedDate1.before(parsedDate2)) {
                    datesArray.add(sdf.format(parsedDate1));
                    cal1.add(Calendar.DATE, 1);
                    diffInDays++;
                    parsedDate1 = cal1.getTime();
                }
                String[] strArray = new String[datesArray.size()];
                strArray = datesArray.toArray(strArray);
                return strArray;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

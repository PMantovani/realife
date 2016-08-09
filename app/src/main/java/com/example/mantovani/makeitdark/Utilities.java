package com.example.mantovani.makeitdark;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Mantovani on 25-Jul-16.
 */
public class Utilities {

    public static String formatTime(long time, String type, boolean showSeconds, Context context) {
        if (type.equals(context.getString(R.string.days_letter)))
            time = time * 24 * 60 * 60 * 1000;
        else if (type.equals(context.getString(R.string.hours_letter)))
            time = time * 60 * 60 * 1000;
        else if (type.equals(context.getString(R.string.minutes_letter)))
            time = time * 60 * 1000;
        else if (type.equals(context.getString(R.string.seconds_letter)))
            time = time * 1000;
        else if (type.equals(context.getString(R.string.millisec_letter)))
            time = time * 1;
        else
            throw new IllegalArgumentException("Unknown type of time.");
        return formatTime(time, showSeconds, context);
    }

    public static String formatTime(long milliseconds, boolean showSeconds, Context context) {
        long seconds = milliseconds/1000;
        String formatted;

        int days = (int)(seconds/86400);
        int hours = (int)(seconds-(days*86400))/3600;
        int minutes = (int)(seconds-(days*86400)-(hours*3600))/60;
        seconds = (int)(seconds-(days*86400)-(hours*3600)-(minutes*60));

        if (days >= 1) { // More than a day
            if (showSeconds) {
                formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s%5$d%6$s%7$d%8$s", days,
                        context.getString(R.string.days_letter), hours,
                        context.getString(R.string.hours_letter), minutes,
                        context.getString(R.string.minutes_letter), seconds,
                        context.getString(R.string.seconds_letter));
            }
            else {
                formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s%5$d%6$s", days,
                        context.getString(R.string.days_letter), hours,
                        context.getString(R.string.hours_letter), minutes,
                        context.getString(R.string.minutes_letter));
            }
        }
        else if (hours >= 1) {  // More than an hour
            if (showSeconds) {
                formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s%5$d%6$s", hours,
                        context.getString(R.string.hours_letter), minutes,
                        context.getString(R.string.minutes_letter), seconds,
                        context.getString(R.string.seconds_letter));
            }
            else {
                formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s", hours,
                        context.getString(R.string.hours_letter), minutes,
                        context.getString(R.string.minutes_letter));
            }
        }
        else if (minutes >= 1) { // More than a minute
            if (showSeconds) {
                formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s", minutes,
                        context.getString(R.string.minutes_letter), seconds,
                        context.getString(R.string.seconds_letter));
            }
            else {
                formatted = String.format(Locale.US, "%1$d%2$s", minutes,
                        context.getString(R.string.minutes_letter));
            }
        }
        else {
            if (showSeconds) {
                formatted = String.format(Locale.US, "%1$d%2$s", seconds,
                        context.getString(R.string.seconds_letter));
            }
            else {
                formatted = "";
            }
        }
        return formatted;
    }

    public static int calculateColor(long minutes) {
        //int green = Color.rgb(153, 255, 153);
        //int yellow = Color.rgb(255, 255, 153);
        //int red = Color.rgb(255, 153, 153);
        final int FIRST_CUT = 5;
        final int SECOND_CUT = 30;
        final int THIRD_CUT = 60;
        if (minutes < 0) {
            return Color.BLACK;
        }
        else if (minutes < FIRST_CUT) {
            // Interpolate between green and yellow
            return Color.rgb(153+(int)((102/FIRST_CUT)*minutes),255,153);
        }
        else if (minutes < SECOND_CUT) {
            // Interpolate between yellow and red
            return Color.rgb(255,255-(int)(102/(SECOND_CUT-FIRST_CUT)*(minutes-FIRST_CUT)),153);
        }
        else {
            // Return red
            return (Color.rgb(255,153,153));
        }
    }

    // Adds time difference from now and last unlock
    public static long timeDiffFromLastUnlock(Context context) {
        // Get our shared preferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);

        long now = System.currentTimeMillis();
        long lastUnlock = sharedPrefs.getLong(context.getString(R.string.pref_last_unlock), now);
        long lastLock = sharedPrefs.getLong(context.getString(R.string.pref_last_lock), now);
        long diff = now - lastUnlock;
        if (lastUnlock > lastLock) {
            return diff;
        }
        return 0;
    }

    public static void createNotification(Context context, String message) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setContentTitle("ReaLife");
        mBuilder.setContentText(message);
        mBuilder.setAutoCancel(true); // Remove notification automatically after clicking in it
        // Create explicit intent to be executed when notification is clicked
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent); // Associate notification with intent
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(100, mBuilder.build());
    }

    public static void postToastMessage(final Context context, final String message) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getAppNameFromPackageName(String packageName, Context context) {
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    public static Drawable getAppIcon (String packageName, Context context) {
        try {
            return context.getApplicationContext()
                    .getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Check if package is a system package
    public static boolean isSystemPackage(String packageName, Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pm.getLaunchIntentForPackage(ai.packageName) != null) {
            //This app is a non-system app
            return false;
        } else {
            //System App
            return true;
        }
    }
}

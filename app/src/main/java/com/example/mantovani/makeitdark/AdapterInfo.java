package com.example.mantovani.makeitdark;

import android.graphics.drawable.Drawable;

/**
 * Created by Mantovani on 8/9/2016.
 */
public class AdapterInfo {
    private String packageName;
    private Drawable drawable;
    private long time;

    public AdapterInfo(String packageName, Drawable drawable, long time) {
        this.packageName = packageName;
        this.drawable = drawable;
        this.time = time;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getTime() {
        return time;
    }
}

package com.example.mantovani.makeitdark;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.mantovani.makeitdark.Utilities.getAppNameFromPackageName;

/**
 * Created by Mantovani on 8/8/2016.
 */
public class CustomListAdapter extends ArrayAdapter<AdapterInfo> {

    private final Activity context;
    private final List<AdapterInfo> item;

    public CustomListAdapter(Activity context, List<AdapterInfo> item) {
        super(context, R.layout.list_item_apps, item);

        this.context=context;
        this.item=item;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_item_apps, null,true);

        TextView txt = (TextView) rowView.findViewById(R.id.list_item_apps_textview);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_item_apps_imageView);

        Context appCont = getContext().getApplicationContext();
        String packageName = item.get(position).getPackageName();
        String appName = getAppNameFromPackageName(packageName, appCont);
        String formattedTime = Utilities.formatTime(item.get(position).getTime(), appCont.getString(R.string.millisec_letter), true, appCont);


        txt.setText(appName + ": " + formattedTime);
        imageView.setImageDrawable(item.get(position).getDrawable());
        return rowView;
    };
}

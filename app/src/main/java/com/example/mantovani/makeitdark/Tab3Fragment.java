package com.example.mantovani.makeitdark;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.example.mantovani.makeitdark.Utilities.getAppIcon;
import static com.example.mantovani.makeitdark.Utilities.getAppNameFromPackageName;
import static com.example.mantovani.makeitdark.Utilities.isSystemPackage;

/**
 * Created by Mantovani on 8/7/2016.
 */
public class Tab3Fragment extends Fragment {
    View rootView;
    CustomListAdapter cAdapter;
    Context appContext;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        appContext = getActivity().getApplicationContext();

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_3, container, false);

        cAdapter = new CustomListAdapter(getActivity(), new ArrayList<AdapterInfo>());

        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setAdapter(cAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        if (!hasPermission()) {
            buildDialog();
        }
        addStatsToAdapter();

        super.onResume();
    }

    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager) getActivity().getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), getActivity().getApplicationContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.ask_for_permission_dialog))
                .setTitle(getString(R.string.title_dialog));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                startActivityForResult(new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS), 0);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addStatsToAdapter() {
        Map map = getAppStats();
        Iterator iter = map.entrySet().iterator();
        List<Pair> list = new ArrayList<Pair>();

        while (iter.hasNext()) {
            Map.Entry<String, UsageStats> entry = (Map.Entry) iter.next();
            UsageStats stats = entry.getValue();
            long totalTime = stats.getTotalTimeInForeground();
            if (totalTime > 10000 && !isSystemPackage(entry.getKey(), appContext)) {
                // Only add if time in app > 10s and not a system package
                list.add(new Pair(entry.getKey(), totalTime));
            }
        }
        // Sorts list by descending order of time in app
        Collections.sort(list, Collections.<Pair>reverseOrder());

        // Adds all apps info to adapter
        cAdapter.clear();
        for (Pair item : list) {
            AdapterInfo info = new AdapterInfo(item.getStr(), getAppIcon(item.getStr(), appContext), item.getTime());
            cAdapter.add(info);
        }
        return;
    }

    public class Pair implements Comparable<Pair> {
        String str;
        long time;

        public Pair(String str, long time) {
            this.str = str;
            this.time = time;
        }

        public String getStr() {
            return str;
        }

        public long getTime() {
            return time;
        }

        @Override
        public int compareTo(Pair another) {
            if (this.time < another.time)
                return -1;
            if (this.time > another.time)
                return 1;
            return 0;
        }
    }

    private Map<String, UsageStats> getAppStats() {
        // Get usage stats for all apps
        UsageStatsManager usm = (UsageStatsManager)getContext().getSystemService("usagestats");
        // End time is now
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        // Start time is beggining of the day
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        // Get map of package name and UsageStats of all apps from startTime to endTime
        return usm.queryAndAggregateUsageStats(startTime, endTime);
    }
}

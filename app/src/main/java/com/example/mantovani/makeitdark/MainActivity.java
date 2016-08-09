package com.example.mantovani.makeitdark;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private String[] mReportsTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new Tab0Fragment())
                    .commit();
        }

        // Initialize Navigation Drawer
        mReportsTitles = getResources().getStringArray(R.array.reports_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mReportsTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerTitle = mTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        Intent serviceIntent = new Intent(this, MonitorService.class);
        this.startService(serviceIntent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        if (position == 0) {
            // open daily report fragment
            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new Tab0Fragment())
                    .commit();
        }
        else if (position == 1) {
            // open hourly report fragment
            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new Tab1Fragment())
                    .commit();
        }
        else if (position == 2) {
            // open history report fragment
            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new Tab2Fragment())
                    .commit();
        }
        else if (position == 3) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new Tab3Fragment())
                    .commit();
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mReportsTitles[position]); This shit doesn't work
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

// Exemplo de como funciona o content provider. Elaborar queries em cima deste modelo.
/*
    ContentResolver provider = getContentResolver();
    String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    // Get data
    Uri uri = ProductivityContract.DayEntry.buildDay(date);
    Cursor cursor = provider.query(uri, null, null, null, null);
if (cursor.moveToFirst()) {
        float ret = cursor.getFloat(cursor.getColumnIndex(ProductivityContract.DayEntry.COLUMN_HOUR00));
        Toast.makeText(MainActivity.this, Float.toString(ret), Toast.LENGTH_SHORT).show();
        }
        else {
        // Insert data
        //Uri dateUri = ProductivityContract.DayEntry.buildDay(date);
        Uri dateUri = ProductivityContract.DayEntry.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(ProductivityContract.DayEntry.COLUMN_HOUR00, 67);
        values.put(ProductivityContract.DayEntry.COLUMN_DATE, date);
        provider.insert(dateUri, values);
        Toast.makeText(MainActivity.this, "INSERIU NO DB", Toast.LENGTH_SHORT).show();
        }

        Uri dateUri = ProductivityContract.DayEntry.CONTENT_URI;
        String[] strs = {date};
        int rowsDeleted = provider.delete(dateUri, ProductivityContract.DayEntry.COLUMN_DATE+" = ?", strs);
        if (rowsDeleted > 0)
        Toast.makeText(MainActivity.this, "DELETOU DE BOA", Toast.LENGTH_SHORT).show();
        else
        Toast.makeText(MainActivity.this, "NAO DELETOU PORRA", Toast.LENGTH_SHORT).show();
*/

// Another example on how to query the content provider
        /*
        ContentResolver resolver = getContentResolver();

        Calendar lastHour = new GregorianCalendar();
        lastHour.add(GregorianCalendar.HOUR, -1);
        int hour = lastHour.get(GregorianCalendar.HOUR);
        Date dateLastHour = lastHour.getTime();

        String date = new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour);
        Uri uri = ProductivityContract.DayEntry.buildDay(date);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            float ret = cursor.getFloat(cursor.getColumnIndex("h"+Integer.toString(hour)));
            Toast.makeText(MainActivity.this, Float.toString(ret), Toast.LENGTH_SHORT).show();
            Log.d("MAKEITDARK", "IN DB: " + Float.toString(ret));
        }
        else {
            Toast.makeText(MainActivity.this, "NOT YET INSERTED IN DB", Toast.LENGTH_SHORT).show();
            Log.d("MAKEITDARK", "NOT YET INSERTED IN DB");
        } */
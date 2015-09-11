package com.example.shreekant.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.shreekant.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity   extends ActionBarActivity
                            implements ForecastFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT_TAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.v(LOG_TAG, "onCreate() screen resolution / display metrics = " + metrics);


        mLocation = Utility.getPreferredLocation(this);
        Log.v(LOG_TAG, "onCreate() Location = " + mLocation);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f); // remove shadow below action bar
        }
        Log.v(LOG_TAG, "onCreate() mTwoPane = " + mTwoPane);

        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
        //        ViewServer.get(this).addWindow(this);
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestroy()");
        super.onDestroy();
//        ViewServer.get(this).removeWindow(this);
    }

    @Override
    protected void onStart() {
        Log.v(LOG_TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.v(LOG_TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.v(LOG_TAG, "onResume()");
        super.onResume();

        // handle if location changed
        String prefLocation = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (prefLocation != null && !prefLocation.equals(mLocation)) {
            Log.v(LOG_TAG, "onResume(): New Location " + prefLocation);
            mLocation = prefLocation;
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }

            // Since DetailFragment is dynamic, use findFragmentByTag
            DetailFragment   df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(mLocation);
            }
        }
//        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onItemSelected(Uri locationDateUri) {
        Log.d(LOG_TAG, "MainActivity.onItemSelected callback. mTwoPane = " + mTwoPane);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.LOCATION_DATE_URI, locationDateUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);    // setArguments() must be done before committing transaction

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(locationDateUri);
            startActivity(intent);
        }
    }

}

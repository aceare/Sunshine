package com.example.shreekant.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
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
        else
        if (id == R.id.action_show_map) {
            showPreferredLocationMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPreferredLocationMap() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String prefLocation = sharedPref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)); // "Pune"
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", prefLocation).build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Toast.makeText(this, getString(R.string.msg_no_map_app_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStart();
        Log.v(LOG_TAG, "onStop()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy()");
    }
}

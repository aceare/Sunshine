package com.example.shreekant.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.shreekant.sunshine.app.data.WeatherContract;
import com.example.shreekant.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String VIEW_POSITION = "VIEW_POSITION";
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private static final int FORECAST_LOADER_ID = 0;
    private ForecastAdapter mForecastAdapter;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private ListView mListView;
    private int mPosition;              // position in listview
    private boolean mUseTodayLayout;    // whether to use (little detailed) special layout for the first item in list.

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated: initLoader");
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The CursorAdapter will take data from our cursor and populate the ListView.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri locationDateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    ((Callback) getActivity()).onItemSelected(locationDateUri);
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(VIEW_POSITION, 0);
//            Log.v(LOG_TAG, "restoring mPosition " + mPosition);
//            mListView.smoothScrollToPosition(mPosition);
////            mListView.setSelection(mPosition);
        }
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
//            Log.v(LOG_TAG, "onSaveInstanceState saving mPosition " + mPosition);
            outState.putInt(VIEW_POSITION, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateWeather() {

        String location = Utility.getPreferredLocation(getActivity());
        /*
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.execute(location);
        */

        /*
        Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.LOCATION_KEY, location);
        getActivity().startService(intent);
        */

        /*
        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        alarmIntent.putExtra(SunshineService.LOCATION_KEY, Utility.getPreferredLocation(getActivity()));
        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);
        AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        //Set the AlarmManager to wake up the system.
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        */

        // If the alarm has been set, cancel it.
//        if (alarmMgr!= null) {
//            alarmMgr.cancel(alarmIntent);
//        }

        /*
        Activity context = getActivity();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SunshineService.AlarmReceiver.class);
        intent.putExtra(SunshineService.LOCATION_KEY, location);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        0 * 1000, alarmIntent);
                     // 20 * 1000, alarmIntent);
                     // 1 * 60 * 1000, alarmIntent); // 1min
        */
        SunshineSyncAdapter.syncImmediately(getActivity());

    }

//    @Override
//    public void onStart() {
//        Log.v(LOG_TAG, "onStart: updateWeather()");
//        super.onStart();
//        updateWeather();
//    }

    public void onLocationChanged() {
        Log.v(LOG_TAG, "onLocationChanged: updateWeather() and restartLoader()");
        updateWeather();
        // Starts a new or restarts an existing Loader in this manager, registers the callbacks to it,
        // and (if the activity/fragment is currently started) starts loading it.
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    // LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // Create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationStartDateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                Utility.getPreferredLocation(getActivity()),
                System.currentTimeMillis());

//        return new CursorLoader(getActivity(),
//                WeatherContract.WeatherEntry.CONTENT_URI,
//                null,                                                               // projection
//                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",     // selection
//                new String[]{Utility.getPreferredLocation(getActivity())},          // selection params
//                null);      // sort order
        Log.v(LOG_TAG, "onCreateLoader: for uri " + weatherForLocationStartDateUri.toString());
        return new CursorLoader(getActivity(),
                weatherForLocationStartDateUri,
                FORECAST_COLUMNS,   // projection
                null,   // selection
                null,   // selection params
                null);  // sort order
    }

    // LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            Log.v(LOG_TAG, "onLoadFinished: scrolling to mPosition " + mPosition);
            mListView.smoothScrollToPosition(mPosition);
////          mListView.setSelection(mPosition);
        }
    }

    // LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mForecastAdapter.swapCursor(null);
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}

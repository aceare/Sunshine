package com.example.shreekant.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shreekant.sunshine.app.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int FORECAST_DETAILS_LOADER_ID = 0;
    private static final String FORECAST_SHARE_HASHTAG = " #Sunshine";
    static final String LOCATION_DATE_URI = "LOCATION_DATE_URI";

    private static final String[] FORECAST_DETAILS_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_DETAILS_WEATHER_ID = 0;
    static final int COL_DETAILS_WEATHER_DATE = 1;
    static final int COL_DETAILS_WEATHER_DESC = 2;
    static final int COL_DETAILS_WEATHER_MAX_TEMP = 3;
    static final int COL_DETAILS_WEATHER_MIN_TEMP = 4;
    static final int COL_DETAILS_WEATHER_HUMIDITY = 5;
    static final int COL_DETAILS_WEATHER_PRESSURE = 6;
    static final int COL_DETAILS_WEATHER_WIND_SPEED = 7;
    static final int COL_DETAILS_WEATHER_DEGREES = 8;
    static final int COL_DETAILS_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mDayNameView;
    private TextView mMonthDayView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    String mForecastStr;
    private ShareActionProvider mShareActionProvider;
    private Uri mLocationDateUri;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mLocationDateUri = arguments.getParcelable(DetailFragment.LOCATION_DATE_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDayNameView = (TextView) rootView.findViewById(R.id.detail_day_name_textview);
        mMonthDayView = (TextView) rootView.findViewById(R.id.detail_month_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // Get ShareActionProvider and attach intent to it
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (mShareActionProvider != null) {
            // If onLoadFinished happens before this, we can go ahead and set the share intent now. TODO / BAD?
            if (mForecastStr != null)
                mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.msg_no_share_provider), Toast.LENGTH_SHORT).show();
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mLocationDateUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mLocationDateUri = updatedUri;
            getLoaderManager().restartLoader(FORECAST_DETAILS_LOADER_ID, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_DETAILS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.

        if (mLocationDateUri != null) {
            // Create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(),
                    mLocationDateUri,
                    FORECAST_DETAILS_COLUMNS,   // projection
                    null,   // selection
                    null,   // selection params
                    null);  // sort order
        }
        return null;
    }

    // LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        // Read weather condition ID from cursor
        int weatherId = data.getInt(COL_DETAILS_WEATHER_CONDITION_ID);
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        long date = data.getLong(COL_DETAILS_WEATHER_DATE);
        String dateString = Utility.formatDate(date);

        String weatherDescription =
                data.getString(COL_DETAILS_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(COL_DETAILS_WEATHER_MAX_TEMP), isMetric);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(COL_DETAILS_WEATHER_MIN_TEMP), isMetric);

        String humidity = Utility.getFormattedHumidity(getActivity(),
                data.getDouble(COL_DETAILS_WEATHER_HUMIDITY));

        String wind = Utility.getFormattedWind(getActivity(),
                data.getDouble(COL_DETAILS_WEATHER_WIND_SPEED),
                data.getDouble(COL_DETAILS_WEATHER_DEGREES),
                isMetric);

        String pressure = Utility.getFormattedPressure(getActivity(),
                data.getDouble(COL_DETAILS_WEATHER_PRESSURE));

        mForecastStr = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
//        TextView detailTextView = (TextView)getView().findViewById(R.id.textview_forecast);
//        detailTextView.setText(mForecastStr);

        FragmentActivity activity = getActivity();
        mDayNameView.setText(Utility.getDayName(activity, date));
        mMonthDayView.setText(Utility.getFormattedMonthDay(activity, date));
        mHighTempView.setText(high);
        mLowTempView.setText(low);
        mHumidityView.setText(humidity);
        mWindView.setText(wind);
        mPressureView.setText(pressure);
        mDescriptionView.setText(weatherDescription);
        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    // LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.

    }

}

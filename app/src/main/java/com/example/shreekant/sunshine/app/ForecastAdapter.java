package com.example.shreekant.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    // whether to use (little detailed) special layout for the first item in list:
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    static final int VIEW_TYPE_TODAY  = 0;
    static final int VIEW_TYPE_FUTURE = 1;
    static final int VIEW_TYPE_COUNT = 2;

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (0 == position) && (mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        int  layoutId;

        if (VIEW_TYPE_TODAY == getItemViewType(cursor.getPosition()))
            layoutId = R.layout.list_item_forecast_today;
        else
            layoutId = R.layout.list_item_forecast;
        view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder holder;
        holder = (ViewHolder) view.getTag();

        String weatherDescription;
        weatherDescription = cursor.getString(ForecastFragment.COL_WEATHER_DESC);

        // Extract details from cursor data
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        Log.v("TMPTMP bindView", "row " + cursor.getPosition() + " weatherId=" + weatherId);
        if (VIEW_TYPE_TODAY == getItemViewType(cursor.getPosition()))
            holder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        else
            holder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        holder.iconView.setContentDescription(weatherDescription);
        holder.dateView.setText(Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        holder.descView.setText(weatherDescription);
        holder.maxTempView.setText(Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(context)));
        holder.minTempView.setText(Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(context)));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descView;
        public final TextView maxTempView;
        public final TextView minTempView;

        ViewHolder(View view) {
            iconView = ((ImageView) view.findViewById(R.id.list_item_icon));
            dateView = ((TextView) view.findViewById(R.id.list_item_date_textview));
            descView = ((TextView) view.findViewById(R.id.list_item_forecast_textview));
            maxTempView = ((TextView) view.findViewById(R.id.list_item_high_textview));
            minTempView = ((TextView) view.findViewById(R.id.list_item_low_textview));
        }
    }
}

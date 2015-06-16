package io.github.kmenager.sunshine.app.muzei;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;

import io.github.kmenager.sunshine.app.MainActivity;
import io.github.kmenager.sunshine.app.Utility;
import io.github.kmenager.sunshine.app.data.WeatherContract;
import io.github.kmenager.sunshine.app.sync.SunshineSyncAdapter;


public class WeatherMuzeiSource extends MuzeiArtSource {

    private final static String[] FORECAST_COLUMNS = new String[]{
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    private final static int INDEX_WEATHER_ID = 0;
    private final static int INDEX_SHORT_DESC = 1;

    public WeatherMuzeiSource() {
        super("WeatherMuzeiSource");
    }

    @Override
    protected void onUpdate(int reason) {
        String location = Utility.getPreferredLocation(this);
        Uri weatherLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis()
        );

        Cursor data = getContentResolver().query(
                weatherLocationUri,
                FORECAST_COLUMNS, null, null,
                WeatherContract.WeatherEntry.COLUMN_DATE + " 1SC"
        );

        if (data.moveToFirst()) {
            int weatherId = data.getInt(INDEX_WEATHER_ID);
            String desc = data.getString(INDEX_SHORT_DESC);

            String imageUrl = Utility.getImageUrlForWeatherCondition(weatherId);

            if (imageUrl != null) {
                publishArtwork(new Artwork.Builder()
                        .imageUri(Uri.parse(imageUrl))
                        .title(desc)
                        .byline(location)
                        .viewIntent(new Intent(this, MainActivity.class))
                        .build());
            }
        }

        data.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        boolean dataUpdated = intent != null &&
                SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction());
        if (dataUpdated && isEnabled()) {
            onUpdate(UPDATE_REASON_OTHER);
        }
    }
}

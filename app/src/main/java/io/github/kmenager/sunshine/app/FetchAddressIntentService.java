package io.github.kmenager.sunshine.app;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import io.github.kmenager.sunshine.app.sync.SunshineSyncAdapter;


public class FetchAddressIntentService extends IntentService {

    private static final String TAG = FetchAddressIntentService.class.getSimpleName();

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        @SunshineSyncAdapter.LocationStatus int locationStatus = SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN;

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            locationStatus = SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN;//getString(R.string.no_location_data_provided);
            Log.wtf(TAG, locationStatus + "");
            deliverResultToReceiver(Constants.FAILURE_RESULT, locationStatus);
            return;
        }

        //getAddress(location);


        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            locationStatus = SunshineSyncAdapter.LOCATION_STATUS_INVALID;
            Log.e(TAG, locationStatus + "", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            locationStatus = SunshineSyncAdapter.LOCATION_STATUS_INVALID;
            Log.e(TAG, locationStatus + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (locationStatus == SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN) {
                locationStatus = SunshineSyncAdapter.LOCATION_STATUS_INVALID;
                Log.e(TAG, "no address found");
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, locationStatus);
        } else {
            Address address = addresses.get(0);

            /*

            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "R.string.address_found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));

            */

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                Log.i(TAG, address.getAddressLine(i));
            }
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    address.getLocality(),
                    address.getCountryCode());
        }

    }

    private void getAddress(Location location) {
        String locationString = location.getLatitude() + "," + location.getLongitude();
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String GEOCODE_BASE_URL =
                    "http://maps.googleapis.com/maps/api/geocode/json?";
            final String QUERY_PARAM = "latlng";

            Uri builtUri = Uri.parse(GEOCODE_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationString)
                    .build();

            URL url = new URL(builtUri.toString());
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            // To avoid error connection on Samsung device
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            Log.d(TAG, url.toString());
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                deliverResultToReceiver(Constants.FAILURE_RESULT,
                        SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN);
                return;
            }
            forecastJsonStr = buffer.toString();
            getAddressFromJson(forecastJsonStr);
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            deliverResultToReceiver(Constants.FAILURE_RESULT,
                    SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
            deliverResultToReceiver(Constants.FAILURE_RESULT,
                    SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getAddressFromJson(String forecastJsonStr) throws JSONException {
        final String MGO_STATUS = "status";
        final String MGO_RESULTS = "results";

        final String MGO_ADDRESS_COMPONENTS = "address_components";
        final String MGO_TYPES = "types";
        final String MGO_LONG_NAME = "long_name";
        final String MGO_SHORT_NAME = "short_name";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            // do we have an error?
            String status = forecastJson.getString(MGO_STATUS);
            if (!status.equals(Constants.STATUS_GEOCODING_OK)) {

                if (status.equals(Constants.STATUS_GEOCODING_INVALID_REQUEST)) {
                    deliverResultToReceiver(Constants.FAILURE_RESULT,
                            SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN);
                } else if (status.equals(Constants.STATUS_GEOCODING_UNKNOWN_ERROR)) {
                    deliverResultToReceiver(Constants.FAILURE_RESULT,
                            SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN);
                } else {
                    deliverResultToReceiver(Constants.FAILURE_RESULT,
                            SunshineSyncAdapter.LOCATION_STATUS_INVALID);
                }
                return;
            }

            String cityName = "";
            String countryCode = "";

            JSONArray resultArray = forecastJson.getJSONArray(MGO_RESULTS);
            JSONArray addressComponent = resultArray.getJSONObject(0).getJSONArray(MGO_ADDRESS_COMPONENTS);
            for (int i = 0; i < addressComponent.length(); i++) {
                JSONObject address = addressComponent.getJSONObject(i);
                JSONArray typesArray = address.getJSONArray(MGO_TYPES);
                for (int j = 0; j < typesArray.length(); j++) {
                    if (typesArray.getString(j).equals(Constants.TYPES_GEOCODING_LOCALITY)) {
                        cityName = address.getString(MGO_LONG_NAME);
                    } else if (typesArray.getString(j).equals(Constants.TYPES_GEOCODING_COUNTRY)) {
                        countryCode = address.getString(MGO_SHORT_NAME);
                    }
                }
            }

            Log.d(TAG, cityName + "," + countryCode);
            if (cityName.isEmpty() && countryCode.isEmpty()) {
                deliverResultToReceiver(Constants.FAILURE_RESULT,
                        SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID);
            } else {
                deliverResultToReceiver(Constants.SUCCESS_RESULT, cityName, countryCode);
            }


        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
            deliverResultToReceiver(Constants.FAILURE_RESULT,
                    SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID);
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, int status) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.RESULT_MESSAGE_KEY, status);
        mReceiver.send(resultCode, bundle);
    }

    /**
     * Sends a resultCode and cityName to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String cityName, String countryCode) {
        Bundle bundle = new Bundle();
        String message = cityName + "," + countryCode;
        bundle.putString(Constants.RESULT_MESSAGE_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}

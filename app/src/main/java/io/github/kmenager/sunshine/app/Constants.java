package io.github.kmenager.sunshine.app;

public final class Constants {
    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME =
            "io.github.kmenager.sunshine.app";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_MESSAGE_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String RESULT_COUNTRY_CODE_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";


    public static final String STATUS_GEOCODING_OK = "OK" ;//indicates that no errors occurred and at least one address was returned.
    public static final String STATUS_GEOCODING_ZERO_RESULTS = "ZERO_RESULTS";// indicates that the reverse geocoding was successful but returned no results. This may occur if the geocoder was passed a latlng in a remote location.
    public static final String STATUS_GEOCODING_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT"; // indicates that you are over your quota.
    public static final String STATUS_GEOCODING_REQUEST_DENIED = "REQUEST_DENIED";//indicates that the request was denied. Possibly because the request includes a result_type or location_type parameter but does not include an API key or client ID.
    public static final String STATUS_GEOCODING_INVALID_REQUEST = "INVALID_REQUEST";    /* generally indicates one of the following:
    The query (address, components or latlng) is missing.
    An invalid result_type or location_type was given.*/
    public static final String STATUS_GEOCODING_UNKNOWN_ERROR = "UNKNOWN_ERROR";// indicates that the request could not be processed due to a server error. The request may succeed if you try again.

    public static final String TYPES_GEOCODING_LOCALITY = "locality";
    public static final String TYPES_GEOCODING_COUNTRY = "country";
}

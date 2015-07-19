package com.markesilva.spotifystreamer.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by marke on 7/18/2015.
 */
public class SpotifyProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SpotifyDbHelper mOpenHelper;

    static final int QUERY = 100;
    static final int ARTIST = 101;
    static final int TRACKS = 102;

//    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
//
//    static{
//        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
//
//        //This is an inner join which looks like
//        //weather INNER JOIN location ON weather.location_id = location._id
//        sWeatherByLocationSettingQueryBuilder.setTables(
//                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
//                        WeatherContract.LocationEntry.TABLE_NAME +
//                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
//                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
//                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
//                        "." + WeatherContract.LocationEntry._ID);
//    }
//
//    //location.location_setting = ?
//    private static final String sLocationSettingSelection =
//            WeatherContract.LocationEntry.TABLE_NAME+
//                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
//
//    //location.location_setting = ? AND date >= ?
//    private static final String sLocationSettingWithStartDateSelection =
//            WeatherContract.LocationEntry.TABLE_NAME+
//                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
//                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";
//
//    //location.location_setting = ? AND date = ?
//    private static final String sLocationSettingAndDaySelection =
//            WeatherContract.LocationEntry.TABLE_NAME +
//                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
//                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";
//
//    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
//        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
//        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
//
//        String[] selectionArgs;
//        String selection;
//
//        if (startDate == 0) {
//            selection = sLocationSettingSelection;
//            selectionArgs = new String[]{locationSetting};
//        } else {
//            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
//            selection = sLocationSettingWithStartDateSelection;
//        }
//
//        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
//                projection,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                sortOrder
//        );
//    }
//
//    private Cursor getWeatherByLocationSettingAndDate(
//            Uri uri, String[] projection, String sortOrder) {
//        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
//        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
//
//        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
//                projection,
//                sLocationSettingAndDaySelection,
//                new String[]{locationSetting, Long.toString(date)},
//                null,
//                null,
//                sortOrder
//        );
//    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SpotifyContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, SpotifyContract.PATH_QUERIES + "/*", QUERY);
        matcher.addURI(authority, SpotifyContract.PATH_ARTISTS + "/*", ARTIST);
        matcher.addURI(authority, SpotifyContract.PATH_TRACKS + "/*", TRACKS);

        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new SpotifyDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case QUERY:
                return SpotifyContract.SearchQueryEntry.CONTENT_ITEM_TYPE;
            case ARTIST:
                return SpotifyContract.ArtistEntry.CONTENT_TYPE;
            case TRACKS:
                return SpotifyContract.TrackEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            case QUERY:
            {
//                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            case ARTIST: {
//                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            case TRACKS: {
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        WeatherContract.WeatherEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
//        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case ARTIST: {
//                normalizeDate(values);
//                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case QUERY: {
//                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
//                if ( _id > 0 )
//                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case QUERY:
//                rowsDeleted = db.delete(
//                        WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARTIST:
//                rowsDeleted = db.delete(
//                        WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRACKS:
//                rowsDeleted = db.delete(
//                        WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

//    private void normalizeDate(ContentValues values) {
//        // normalize the date value
//        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
//            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
//            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
//        }
//    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case QUERY:
//                normalizeDate(values);
//                rowsUpdated = db.update(WeatherContract.WeatherEntry.TABLE_NAME, values, selection,
//                        selectionArgs);
                break;
            case ARTIST:
//                rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME, values, selection,
//                        selectionArgs);
                break;
            case TRACKS:
//                rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME, values, selection,
//                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case QUERY:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
//                        normalizeDate(value);
//                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
//                        if (_id != -1) {
//                            returnCount++;
//                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}

package com.markesilva.spotifystreamer.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.markesilva.spotifystreamer.utils.LogUtils;

/**
 * Created by marke on 7/18/2015.
 *
 * Content Provider to spotify information that has been queried from the Spotify API
 */
public class SpotifyProvider extends ContentProvider {

    //queries.query_string = ?
    public static final String sQueryStringSelection =
            SpotifyContract.SearchQueryEntry.TABLE_NAME +
                    "." + SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING + " = ? ";
    //artists.artist_name = ?
    public static final String sArtistStringSelection =
            SpotifyContract.ArtistEntry.TABLE_NAME +
                    "." + SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME + " = ? ";
    //artists.spotify_artist_id = ?
    public static final String sArtistIdStringSelection =
            SpotifyContract.ArtistEntry.TABLE_NAME +
                    "." + SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID + " = ? ";
    static final int QUERIES = 100;
    static final int QUERIES_WITH_QUERY = 101;
    static final int ARTISTS = 200;
    static final int ARTISTS_WITH_ARTIST = 201;
    static final int ARTISTS_WITH_QUERY = 202;
    static final int ARTISTS_WITH_ARTIST_ID = 203;
    static final int TRACKS = 300;
    static final int TRACKS_WITH_ARTIST = 301;
    static final int TRACKS_WITH_QUERY = 302;
    static final int TRACKS_WITH_ARTIST_ID = 303;
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = LogUtils.makeLogTag(SpotifyProvider.class);
    private static final SQLiteQueryBuilder sArtistsSettingQueryBuilder;
    private static final SQLiteQueryBuilder sTracksSettingQueryBuilder;

    static{
        sArtistsSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //artists INNER JOIN queries ON artists.search_id = queries._id
        sArtistsSettingQueryBuilder.setTables(
                SpotifyContract.ArtistEntry.TABLE_NAME + " INNER JOIN " +
                        SpotifyContract.SearchQueryEntry.TABLE_NAME +
                        " ON " + SpotifyContract.ArtistEntry.TABLE_NAME +
                        "." + SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID +
                        " = " + SpotifyContract.SearchQueryEntry.TABLE_NAME +
                        "." + SpotifyContract.SearchQueryEntry._ID);
    }

    static {
        sTracksSettingQueryBuilder = new SQLiteQueryBuilder();

        String tables =
                SpotifyContract.TrackEntry.TABLE_NAME +
                        " INNER JOIN " + SpotifyContract.SearchQueryEntry.TABLE_NAME +
                        " ON " + SpotifyContract.TrackEntry.TABLE_NAME +
                        "." + SpotifyContract.TrackEntry.COLUMN_SEARCH_ID +
                        " = " + SpotifyContract.SearchQueryEntry.TABLE_NAME +
                        "." + SpotifyContract.SearchQueryEntry._ID +
                        " INNER JOIN " + SpotifyContract.ArtistEntry.TABLE_NAME +
                        " ON " + SpotifyContract.TrackEntry.TABLE_NAME +
                        "." + SpotifyContract.TrackEntry.COLUMN_ARTIST_ID +
                        " = " + SpotifyContract.ArtistEntry.TABLE_NAME +
                        "." + SpotifyContract.ArtistEntry._ID;

        //This is an inner join which looks like
        //tracks INNER JOIN queries ON weather.search_id = queries._id INNER JOIN artists ON artists.
        sTracksSettingQueryBuilder.setTables(tables);
    }

    private SpotifyDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SpotifyContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, SpotifyContract.PATH_QUERIES, QUERIES);
        matcher.addURI(authority, SpotifyContract.PATH_QUERIES + "/*", QUERIES_WITH_QUERY);

        matcher.addURI(authority, SpotifyContract.PATH_ARTISTS, ARTISTS);
        matcher.addURI(authority, SpotifyContract.PATH_ARTISTS + "/" + SpotifyContract.PATH_ARTIST + "/*", ARTISTS_WITH_ARTIST);
        matcher.addURI(authority, SpotifyContract.PATH_ARTISTS + "/" + SpotifyContract.PATH_QUERY + "/*", ARTISTS_WITH_QUERY);
        matcher.addURI(authority, SpotifyContract.PATH_ARTISTS + "/" + SpotifyContract.PATH_ARTIST_ID + "/*", ARTISTS_WITH_ARTIST_ID);

        matcher.addURI(authority, SpotifyContract.PATH_TRACKS, TRACKS);
        matcher.addURI(authority, SpotifyContract.PATH_TRACKS + "/" + SpotifyContract.PATH_ARTIST + "/*", TRACKS_WITH_ARTIST);
        matcher.addURI(authority, SpotifyContract.PATH_TRACKS + "/" + SpotifyContract.PATH_QUERY + "/*", TRACKS_WITH_QUERY);
        matcher.addURI(authority, SpotifyContract.PATH_TRACKS + "/" + SpotifyContract.PATH_ARTIST_ID + "/*", TRACKS_WITH_ARTIST_ID);

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
            case QUERIES:
                return SpotifyContract.SearchQueryEntry.CONTENT_TYPE;
            case QUERIES_WITH_QUERY:
                return SpotifyContract.SearchQueryEntry.CONTENT_ITEM_TYPE;
            case ARTISTS:
                return SpotifyContract.ArtistEntry.CONTENT_TYPE;
            case ARTISTS_WITH_ARTIST:
                return SpotifyContract.ArtistEntry.CONTENT_TYPE;
            case ARTISTS_WITH_ARTIST_ID:
                return SpotifyContract.ArtistEntry.CONTENT_TYPE;
            case ARTISTS_WITH_QUERY:
                return SpotifyContract.ArtistEntry.CONTENT_TYPE;
            case TRACKS:
                return SpotifyContract.TrackEntry.CONTENT_TYPE;
            case TRACKS_WITH_ARTIST:
                return SpotifyContract.TrackEntry.CONTENT_TYPE;
            case TRACKS_WITH_ARTIST_ID:
                return SpotifyContract.TrackEntry.CONTENT_TYPE;
            case TRACKS_WITH_QUERY:
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
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case QUERIES:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SpotifyContract.SearchQueryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case QUERIES_WITH_QUERY:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SpotifyContract.SearchQueryEntry.TABLE_NAME,
                        projection,
                        sQueryStringSelection,
                        new String[]{SpotifyContract.SearchQueryEntry.getQueryFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ARTISTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SpotifyContract.ArtistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ARTISTS_WITH_QUERY:
            {
                String q = SpotifyContract.ArtistEntry.getQueryFromUri(uri);
                retCursor = sArtistsSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sQueryStringSelection,
                        new String[]{q},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ARTISTS_WITH_ARTIST:
            {
                String a = SpotifyContract.ArtistEntry.getArtistFromUri(uri);
                retCursor = sArtistsSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sArtistStringSelection,
                        new String[]{a},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ARTISTS_WITH_ARTIST_ID:
            {
                String i = SpotifyContract.ArtistEntry.getArtistIdFromUri(uri);
                retCursor = sArtistsSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sArtistIdStringSelection,
                        new String[]{i},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case TRACKS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SpotifyContract.TrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TRACKS_WITH_QUERY:
            {
                String q = SpotifyContract.ArtistEntry.getQueryFromUri(uri);
                retCursor = sTracksSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sQueryStringSelection,
                        new String[]{q},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case TRACKS_WITH_ARTIST:
            {
                String a = SpotifyContract.ArtistEntry.getArtistFromUri(uri);
                retCursor = sTracksSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sArtistStringSelection,
                        new String[]{a},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case TRACKS_WITH_ARTIST_ID:
            {
                String i = SpotifyContract.ArtistEntry.getArtistIdFromUri(uri);
                LogUtils.LOGV(LOG_TAG, "Searching for tracks with artist id " + i);
                retCursor = sTracksSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sArtistIdStringSelection,
                        new String[]{i},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (retCursor != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case QUERIES: {
                long _id = db.insert(SpotifyContract.SearchQueryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SpotifyContract.SearchQueryEntry.buildQueriesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ARTISTS: {
                long _id = db.insert(SpotifyContract.ArtistEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SpotifyContract.ArtistEntry.buildArtistUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRACKS: {
                long _id = db.insert(SpotifyContract.TrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SpotifyContract.TrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
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
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case QUERIES:
                rowsDeleted = db.delete(
                        SpotifyContract.SearchQueryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARTISTS:
                rowsDeleted = db.delete(
                        SpotifyContract.ArtistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRACKS:
                rowsDeleted = db.delete(
                        SpotifyContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
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

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case QUERIES:
                rowsUpdated = db.update(SpotifyContract.SearchQueryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ARTISTS:
                rowsUpdated = db.update(SpotifyContract.ArtistEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRACKS:
                rowsUpdated = db.update(SpotifyContract.TrackEntry.TABLE_NAME, values, selection,
                        selectionArgs);
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
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case QUERIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SpotifyContract.SearchQueryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case ARTISTS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SpotifyContract.ArtistEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TRACKS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SpotifyContract.TrackEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
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

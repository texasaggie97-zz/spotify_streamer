package com.markesilva.spotifystreamer.data;

/**
 * Created by marke on 7/18/2015.
 *
 * Helper class to encapsulate DB access
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.markesilva.spotifystreamer.data.SpotifyContract.ArtistEntry;
import com.markesilva.spotifystreamer.data.SpotifyContract.TrackEntry;
import com.markesilva.spotifystreamer.data.SpotifyContract.SearchQueryEntry;

/**
 * Manages a local database for Spotify data.
 */
public class SpotifyDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = SpotifyDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "spotify.db";

    public SpotifyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_QUERIES_TABLE = "CREATE TABLE " + SearchQueryEntry.TABLE_NAME + " (" +
                SearchQueryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SearchQueryEntry.COLUMN_QUERY_STRING + " TEXT UNIQUE NOT NULL, " +
                SearchQueryEntry.COLUMN_QUERY_TIME + " INTEGER NOT NULL, " +
                " UNIQUE (" + SearchQueryEntry.COLUMN_QUERY_STRING + ") ON CONFLICT REPLACE" +
                " );";

        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                ArtistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ArtistEntry.COLUMN_SEARCH_ID + " INTEGER NOT NULL, " +
                ArtistEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +

                // Set up the search id column as a foreign key to quries table.
                " FOREIGN KEY (" + ArtistEntry.COLUMN_SEARCH_ID + ") REFERENCES " +
                SearchQueryEntry.TABLE_NAME + " (" + SearchQueryEntry._ID + ") " +
                " );";

        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrackEntry.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_SEARCH_ID + " INTEGER NOT NULL, " +
                TrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_PREVIEW_URL + " TEXT NOT NULL, " +

                // Set up the artist id column as a foreign key to artist table.
                " FOREIGN KEY (" + TrackEntry.COLUMN_ARTIST_ID + ") REFERENCES " +
                ArtistEntry.TABLE_NAME + " (" + ArtistEntry._ID + "), " +

                // Set up the search id column as a foreign key to quries table.
                " FOREIGN KEY (" + TrackEntry.COLUMN_SEARCH_ID + ") REFERENCES " +
                SearchQueryEntry.TABLE_NAME + " (" + SearchQueryEntry._ID + ") " +

                " );";

        Log.v(LOG_TAG, SQL_CREATE_QUERIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_QUERIES_TABLE);
        Log.v(LOG_TAG, SQL_CREATE_ARTIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);
        Log.v(LOG_TAG, SQL_CREATE_TRACK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 4 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SearchQueryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}


package com.markesilva.spotifystreamer.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by marke on 7/18/2015.
 */
public class TestUtilities extends AndroidTestCase {

    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createQueryValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING, "Stirling");
        testValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME, TEST_DATE);

        return testValues;
    }

    static ContentValues createArtistValues(long searchQueryRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID, searchQueryRowId);
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME, "Stirling");
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, "42");
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_THUMBNAIL_URL, "http://google.com");

        return testValues;
    }

    static ContentValues createTrackValues(long searchQueryRowId, long artistRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(SpotifyContract.TrackEntry.COLUMN_SEARCH_ID, searchQueryRowId);
        testValues.put(SpotifyContract.TrackEntry.COLUMN_ARTIST_ID, artistRowId);
        testValues.put(SpotifyContract.TrackEntry.COLUMN_THUMBNAIL_URL, "http://google.com");
        testValues.put(SpotifyContract.TrackEntry.COLUMN_IMAGE_URL, "http://bing.com");
        testValues.put(SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME, "Radioactive");
        testValues.put(SpotifyContract.TrackEntry.COLUMN_TRACK_NAME, "You Spin Me");

        return testValues;
    }
}

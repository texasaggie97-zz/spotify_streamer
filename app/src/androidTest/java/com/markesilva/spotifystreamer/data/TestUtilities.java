package com.markesilva.spotifystreamer.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.markesilva.spotifystreamer.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by marke on 7/18/2015.
 *
 * Helper functions for unit tests
 */
public class TestUtilities extends AndroidTestCase {

    static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    static final String TEST_QUERY_STRING = "Stirling";
    static final String TEST_ARTIST_NAME = "Lindsey Stirling";
    static final String TEST_ARTIST_SPOTIFY_ID = "34534534";

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
        testValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING, TEST_QUERY_STRING);
        testValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME, TEST_DATE);

        return testValues;
    }

    static ContentValues createArtistValues(long searchQueryRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID, searchQueryRowId);
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME, TEST_ARTIST_NAME);
        testValues.put(SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, TEST_ARTIST_SPOTIFY_ID);
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
        testValues.put(SpotifyContract.TrackEntry.COLUMN_PREVIEW_URL, "http://altavist.com");

        return testValues;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

package com.markesilva.spotifystreamer.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by marke on 7/18/2015.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String LOG_TAG = TestUriMatcher.class.getSimpleName();

    // content://com.markesilva.spotifystreamer/queries"
    private static final Uri TEST_QUERIES_DIR = SpotifyContract.SearchQueryEntry.buildQueriesWithQuery("Stirling");
    private static final Uri TEST_ARTIST_DIR = SpotifyContract.ArtistEntry.buildArtistWithArtist("Lindsey Stirling");
    private static final Uri TEST_TRACK_DIR = SpotifyContract.TrackEntry.buildTrackWithTrack("Radioactive");

    public void testUriMatcher() {
        UriMatcher testMatcher = SpotifyProvider.buildUriMatcher();

        Log.v(LOG_TAG, "TEST_QUERIES_DIR = " + TEST_QUERIES_DIR);
        assertEquals("Error: The QUERIES URI was matched incorrectly.",
                testMatcher.match(TEST_QUERIES_DIR), SpotifyProvider.QUERY);
        Log.v(LOG_TAG, "TEST_ARTIST_DIR = " + TEST_ARTIST_DIR);
        assertEquals("Error: The ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_DIR), SpotifyProvider.ARTIST);
        Log.v(LOG_TAG, "TEST_TRACK_DIR = " + TEST_TRACK_DIR);
        assertEquals("Error: The TRACK URI was matched incorrectly.",
                testMatcher.match(TEST_TRACK_DIR), SpotifyProvider.TRACKS);
    }
}


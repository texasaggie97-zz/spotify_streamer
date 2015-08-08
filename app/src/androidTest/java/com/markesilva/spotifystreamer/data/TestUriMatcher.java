package com.markesilva.spotifystreamer.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by marke on 7/18/2015.
 *
 * Unit test for URI Matcher
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String LOG_TAG = TestUriMatcher.class.getSimpleName();

    // content://com.markesilva.spotifystreamer/queries"
    private static final Uri TEST_QUERIES_DIR = SpotifyContract.SearchQueryEntry.CONTENT_URI;
    private static final Uri TEST_QUERIES_WITH_QUERY = SpotifyContract.SearchQueryEntry.buildQueriesWithQuery(TestUtilities.TEST_QUERY_STRING);
    private static final Uri TEST_ARTISTS_DIR = SpotifyContract.ArtistEntry.CONTENT_URI;
    private static final Uri TEST_ARTISTS_WITH_QUERY = SpotifyContract.ArtistEntry.buildArtistsWithQuery(TestUtilities.TEST_QUERY_STRING);
    private static final Uri TEST_ARTISTS_WITH_ARTIST = SpotifyContract.ArtistEntry.buildArtistsWithArtist(TestUtilities.TEST_ARTIST_NAME);
    private static final Uri TEST_ARTISTS_WITH_ARTIST_ID = SpotifyContract.ArtistEntry.buildArtistsWithArtistId(TestUtilities.TEST_ARTIST_SPOTIFY_ID);
    private static final Uri TEST_TRACKS_DIR = SpotifyContract.TrackEntry.CONTENT_URI;
    private static final Uri TEST_TRACKS_WITH_QUERY = SpotifyContract.TrackEntry.buildTracksWithQuery(TestUtilities.TEST_QUERY_STRING);
    private static final Uri TEST_TRACKS_WITH_ARTIST = SpotifyContract.TrackEntry.buildTracksWithArtist(TestUtilities.TEST_ARTIST_NAME);
    private static final Uri TEST_TRACKS_WITH_ARTIST_ID = SpotifyContract.TrackEntry.buildTracksWithArtistId(TestUtilities.TEST_ARTIST_SPOTIFY_ID);

    public void testUriMatcher() {
        UriMatcher testMatcher = SpotifyProvider.buildUriMatcher();

        assertEquals("Error: The QUERIES URI was matched incorrectly.",
                testMatcher.match(TEST_QUERIES_DIR), SpotifyProvider.QUERIES);
        assertEquals("Error: The QUERIES URI was matched incorrectly.",
                testMatcher.match(TEST_QUERIES_WITH_QUERY), SpotifyProvider.QUERIES_WITH_QUERY);

        assertEquals("Error: The ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTISTS_DIR), SpotifyProvider.ARTISTS);
        assertEquals("Error: The ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTISTS_WITH_ARTIST), SpotifyProvider.ARTISTS_WITH_ARTIST);
        assertEquals("Error: The ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTISTS_WITH_ARTIST_ID), SpotifyProvider.ARTISTS_WITH_ARTIST_ID);
        assertEquals("Error: The ARTIST URI was matched incorrectly.",
                testMatcher.match(TEST_ARTISTS_WITH_QUERY), SpotifyProvider.ARTISTS_WITH_QUERY);

        assertEquals("Error: The TRACK URI was matched incorrectly.",
                testMatcher.match(TEST_TRACKS_DIR), SpotifyProvider.TRACKS);
        assertEquals("Error: The TRACK URI was matched incorrectly.",
                testMatcher.match(TEST_TRACKS_WITH_ARTIST), SpotifyProvider.TRACKS_WITH_ARTIST);
        assertEquals("Error: The TRACK URI was matched incorrectly.",
                testMatcher.match(TEST_TRACKS_WITH_QUERY), SpotifyProvider.TRACKS_WITH_QUERY);
        assertEquals("Error: The TRACK URI was matched incorrectly.",
                testMatcher.match(TEST_TRACKS_WITH_ARTIST_ID), SpotifyProvider.TRACKS_WITH_ARTIST_ID);
    }
}


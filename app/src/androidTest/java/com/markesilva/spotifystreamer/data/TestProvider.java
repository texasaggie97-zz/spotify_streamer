package com.markesilva.spotifystreamer.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.markesilva.spotifystreamer.data.SpotifyContract.SearchQueryEntry;
import com.markesilva.spotifystreamer.data.SpotifyContract.ArtistEntry;
import com.markesilva.spotifystreamer.data.SpotifyContract.TrackEntry;

/**
 * Created by marke on 7/18/2015.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                SearchQueryEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ArtistEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                TrackEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                SearchQueryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Queries table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Artist table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Track table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                SpotifyProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + SpotifyContract.CONTENT_AUTHORITY,
                    providerInfo.authority, SpotifyContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.markesilva.spotifystreamer.app/queries/Stirling
        String type = mContext.getContentResolver().getType(SearchQueryEntry.CONTENT_URI);
        assertEquals("Error: the SearchQueryEntry CONTENT_URI should return SearchQueryEntry.CONTENT_TYPE",
                SearchQueryEntry.CONTENT_TYPE, type);

        String testQuery = "Stirling";
        type = mContext.getContentResolver().getType(SearchQueryEntry.buildQueriesWithQuery(testQuery));
        assertEquals("Error: the SearchQueryEntry CONTENT_URI should return SearchQueryEntry.CONTENT_TYPE",
                SearchQueryEntry.CONTENT_ITEM_TYPE, type);

        String testArtist= "Lindsey Stirling";
        // content://com.markesilva.spotifystreamer.app/artist/Lindsey Stirling
        type = mContext.getContentResolver().getType(ArtistEntry.CONTENT_URI);
        assertEquals("Error: the ArtistEntry.CONTENT_URI should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                ArtistEntry.buildArtistsWithArtist(testArtist));
        assertEquals("Error: the ArtistEntry CONTENT_URI with location should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                ArtistEntry.buildArtistsWithQuery(testQuery));
        assertEquals("Error: the ArtistEntry CONTENT_URI with location should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        // content://com.markesilva.spotifystreamer.app/track/Radioactive
        type = mContext.getContentResolver().getType(TrackEntry.CONTENT_URI);
        assertEquals("Error: the TrackEntry.CONTENT_URI should return TrackEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                TrackEntry.buildTracksWithArtist(testArtist));
        assertEquals("Error: the ArtistEntry CONTENT_URI with location should return ArtistEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                TrackEntry.buildTracksWithQuery(testQuery));
        assertEquals("Error: the ArtistEntry CONTENT_URI with location should return ArtistEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicQuery() {
        // insert our test records into the database
        SpotifyDbHelper dbHelper = new SpotifyDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues queryValues = TestUtilities.createQueryValues();
        long queryRowId = db.insert(SearchQueryEntry.TABLE_NAME, null, queryValues);
        assertTrue("Unable to Insert SearchQueryEntry into the Database", queryRowId != -1);

        ContentValues artistValues = TestUtilities.createArtistValues(queryRowId);
        long artistRowId = db.insert(ArtistEntry.TABLE_NAME, null, artistValues);
        assertTrue("Unable to Insert ArtistEntry into the Database", artistRowId != -1);

        ContentValues trackValues = TestUtilities.createTrackValues(queryRowId, artistRowId);
        long trackRowId = db.insert(TrackEntry.TABLE_NAME, null, trackValues);
        assertTrue("Unable to Insert TrackEntry into the Database", trackRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor queryCursor = mContext.getContentResolver().query(
                SearchQueryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", queryCursor, queryValues);
        queryCursor.close();

        // Test the basic content provider query
        Cursor artistCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", artistCursor, artistValues);
        artistCursor.close();

        // Test the basic content provider query
        Cursor trackCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", trackCursor, trackValues);
        trackCursor.close();
    }

//    /*
//        This test uses the provider to insert and then update the data. Uncomment this test to
//        see if your update location is functioning correctly.
//     */
//    public void testUpdateLocation() {
//        // Create a new map of values, where column names are the keys
//        ContentValues values = TestUtilities.createNorthPoleLocationValues();
//
//        Uri locationUri = mContext.getContentResolver().
//                insert(LocationEntry.CONTENT_URI, values);
//        long locationRowId = ContentUris.parseId(locationUri);
//
//        // Verify we got a row back.
//        assertTrue(locationRowId != -1);
//        Log.d(LOG_TAG, "New row id: " + locationRowId);
//
//        ContentValues updatedValues = new ContentValues(values);
//        updatedValues.put(LocationEntry._ID, locationRowId);
//        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");
//
//        // Create a cursor with observer to make sure that the content provider is notifying
//        // the observers as expected
//        Cursor locationCursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI, null, null, null, null);
//
//        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
//        locationCursor.registerContentObserver(tco);
//
//        int count = mContext.getContentResolver().update(
//                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
//                new String[] { Long.toString(locationRowId)});
//        assertEquals(count, 1);
//
//        // Test to make sure our observer is called.  If not, we throw an assertion.
//        //
//        // Students: If your code is failing here, it means that your content provider
//        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//
//        locationCursor.unregisterContentObserver(tco);
//        locationCursor.close();
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                LocationEntry.CONTENT_URI,
//                null,   // projection
//                LocationEntry._ID + " = " + locationRowId,
//                null,   // Values for the "where" clause
//                null    // sort order
//        );
//
//        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
//                cursor, updatedValues);
//
//        cursor.close();
//    }
//
//
    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues queryValues = TestUtilities.createQueryValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(SearchQueryEntry.CONTENT_URI, true, tco);
        Uri queryInsertUri = mContext.getContentResolver().insert(SearchQueryEntry.CONTENT_URI, queryValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long queryRowId = ContentUris.parseId(queryInsertUri);

        // Verify we got a row back.
        assertTrue(queryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                SearchQueryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating SearchQueryEntry.",
                cursor, queryValues);
        cursor.close();

        // Fantastic.
        ContentValues artistValues = TestUtilities.createArtistValues(queryRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, tco);

        Uri artistInsertUri = mContext.getContentResolver()
                .insert(ArtistEntry.CONTENT_URI, artistValues);
        assertTrue(artistInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long artistRowId = ContentUris.parseId(artistInsertUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor artistCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistEntry insert.",
                artistCursor, artistValues);
        artistCursor.close();

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        artistValues.putAll(queryValues);

        // Get the joined Query, Artist and Track data
        Cursor joinCursor = mContext.getContentResolver().query(
                ArtistEntry.buildArtistsWithQuery(TestUtilities.TEST_QUERY_STRING),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Query and Artist Data.",
                joinCursor, artistValues);
        joinCursor.close();

        // Fantastic.
        ContentValues trackValues = TestUtilities.createTrackValues(queryRowId, artistRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, tco);

        Uri trackInsertUri = mContext.getContentResolver()
                .insert(TrackEntry.CONTENT_URI, trackValues);
        assertTrue(trackInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long trackRowId = ContentUris.parseId(trackInsertUri);

        // Verify we got a row back.
        assertTrue(trackRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor trackCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating TrackEntry insert.",
                trackCursor, trackValues);
        trackCursor.close();

        // Add the track values in with the artist data so that we can make
        // sure that the join worked and we actually get all the values back
        artistValues.putAll(trackValues);

        // Get the joined Query, Artist and Track data
        joinCursor = mContext.getContentResolver().query(
                TrackEntry.buildTracksWithQuery(TestUtilities.TEST_QUERY_STRING),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Query, Artist and Track Data.",
                joinCursor, artistValues);
        joinCursor.close();

        // Get the joined Query, Artist and Track data
        joinCursor = mContext.getContentResolver().query(
                TrackEntry.buildTracksWithArtist(TestUtilities.TEST_ARTIST_NAME),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Query, Artist and Track Data.",
                joinCursor, artistValues);
        joinCursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver queryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(SearchQueryEntry.CONTENT_URI, true, queryObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver artistsObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, artistsObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver tracksObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, tracksObserver);

        deleteAllRecordsFromProvider();

        // Students: If any of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        queryObserver.waitForNotificationOrFail();
        artistsObserver.waitForNotificationOrFail();
        tracksObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(queryObserver);
        mContext.getContentResolver().unregisterContentObserver(artistsObserver);
        mContext.getContentResolver().unregisterContentObserver(tracksObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertArtistsValues(long queryRowId) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            ContentValues artistsValues = new ContentValues();
            artistsValues.put(ArtistEntry.COLUMN_SEARCH_ID, queryRowId);
            artistsValues.put(ArtistEntry.COLUMN_ARTIST_NAME, "A" + (char)('a' + i));
            artistsValues.put(ArtistEntry.COLUMN_THUMBNAIL_URL, "http://google.com/A" + (char)('A' + i));
            artistsValues.put(ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, "42");
            returnContentValues[i] = artistsValues;
        }
        return returnContentValues;
    }

    static ContentValues[] createBulkInsertTracksValues(long queryRowId, long artistRowId) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            ContentValues tracksValues = new ContentValues();
            tracksValues.put(TrackEntry.COLUMN_SEARCH_ID, queryRowId);
            tracksValues.put(TrackEntry.COLUMN_ARTIST_ID, artistRowId);
            tracksValues.put(TrackEntry.COLUMN_THUMBNAIL_URL, "http://google.com/T" + (char)('A' + i));
            tracksValues.put(TrackEntry.COLUMN_IMAGE_URL, "http://google.com/I" + (char)('A' + i));
            tracksValues.put(TrackEntry.COLUMN_ALBUM_NAME, "A" + (char)('a' + i));
            tracksValues.put(TrackEntry.COLUMN_TRACK_NAME, "T" + (char)('a' + i));
            returnContentValues[i] = tracksValues;
        }
        return returnContentValues;
    }

    public void testBulkInsertArtists() {
        ContentValues testValues = TestUtilities.createQueryValues();
        Uri locationUri = mContext.getContentResolver().insert(SearchQueryEntry.CONTENT_URI, testValues);
        long queryRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(queryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                SearchQueryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
                cursor, testValues);

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertArtistsValues(queryRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(ArtistEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }

    public void testBulkInsertTracks() {
        ContentValues queryValues = TestUtilities.createQueryValues();
        Uri queryUri = mContext.getContentResolver().insert(SearchQueryEntry.CONTENT_URI, queryValues);
        long queryRowId = ContentUris.parseId(queryUri);

        // Verify we got a row back.
        assertTrue(queryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor queryCursor = mContext.getContentResolver().query(
                SearchQueryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
                queryCursor, queryValues);

        // Now artist
        ContentValues artistValues = TestUtilities.createArtistValues(queryRowId);
        Uri artistUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, artistValues);
        long artistRowId = ContentUris.parseId(artistUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor artistCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
                artistCursor, artistValues);

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertTracksValues(queryRowId, artistRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor trackCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(trackCursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        trackCursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, trackCursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    trackCursor, bulkInsertContentValues[i]);
        }
        trackCursor.close();
    }
}


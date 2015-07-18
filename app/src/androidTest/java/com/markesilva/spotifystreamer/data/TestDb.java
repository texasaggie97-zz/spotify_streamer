package com.markesilva.spotifystreamer.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.markesilva.spotifystreamer.data.SpotifyDbHelper;

import java.util.HashSet;

/**
 * Created by marke on 7/18/2015.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(SpotifyDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(SpotifyContract.SearchQueryEntry.TABLE_NAME);
        tableNameHashSet.add(SpotifyContract.ArtistEntry.TABLE_NAME);
        tableNameHashSet.add(SpotifyContract.TrackEntry.TABLE_NAME);

        mContext.deleteDatabase(SpotifyDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new SpotifyDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + SpotifyContract.SearchQueryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> queriesColumnHashSet = new HashSet<String>();
        queriesColumnHashSet.add(SpotifyContract.SearchQueryEntry._ID);
        queriesColumnHashSet.add(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING);
        queriesColumnHashSet.add(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            queriesColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required queries entry columns",
                queriesColumnHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + SpotifyContract.ArtistEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> artistsColumnHashSet = new HashSet<String>();
        artistsColumnHashSet.add(SpotifyContract.ArtistEntry._ID);
        artistsColumnHashSet.add(SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID);
        artistsColumnHashSet.add(SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME);
        artistsColumnHashSet.add(SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID);
        artistsColumnHashSet.add(SpotifyContract.ArtistEntry.COLUMN_THUMBNAIL_URL);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            artistsColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                artistsColumnHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + SpotifyContract.TrackEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> tracksColumnHashSet = new HashSet<String>();
        tracksColumnHashSet.add(SpotifyContract.TrackEntry._ID);
        tracksColumnHashSet.add(SpotifyContract.TrackEntry.COLUMN_ARTIST_ID);
        tracksColumnHashSet.add(SpotifyContract.TrackEntry.COLUMN_SEARCH_ID);
        tracksColumnHashSet.add(SpotifyContract.TrackEntry.COLUMN_THUMBNAIL_URL);
        tracksColumnHashSet.add(SpotifyContract.TrackEntry.COLUMN_IMAGE_URL);
        tracksColumnHashSet.add(SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME);
        tracksColumnHashSet.add(SpotifyContract.TrackEntry.COLUMN_TRACK_NAME);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            tracksColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                tracksColumnHashSet.isEmpty());
        db.close();
    }

    public void testQueryTable() {
        insertQuery();
    }

    public void testArtistTable() {
        insertArtist(-1);
    }

    public void testTrackTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        long queryRowId = insertQuery();
        // Make sure we have a valid row ID.
        assertFalse("Error: Artist Not Inserted Correctly", queryRowId == -1L);

        long artistRowId = insertArtist(queryRowId);
        // Make sure we have a valid row ID.
        assertFalse("Error: Artist Not Inserted Correctly", artistRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SpotifyDbHelper dbHelper = new SpotifyDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Weather): Create weather values
        ContentValues testValues = TestUtilities.createTrackValues(queryRowId, artistRowId);

        // Third Step (Weather): Insert ContentValues into database and get a row ID back
        long trackRowId = db.insert(SpotifyContract.TrackEntry.TABLE_NAME, null, testValues);
        assertTrue(trackRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                SpotifyContract.TrackEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from location query", weatherCursor.moveToFirst() );

        // Fifth Step: Validate the location Query
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                weatherCursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from weather query",
                weatherCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        weatherCursor.close();
        dbHelper.close();
    }

    public long insertQuery() {
        long queryRowId = -1;
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SpotifyDbHelper dbHelper = new SpotifyDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createQueryValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        queryRowId = db.insert(SpotifyContract.SearchQueryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(queryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                SpotifyContract.SearchQueryEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from queries query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Queries Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from queries query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();

        return queryRowId;
    }

    public long insertArtist(long queryRowId) {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SpotifyDbHelper dbHelper = new SpotifyDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // If qeuryRowId is -1, then we are supposed to call insertQuery() ourselves,
        // otherwise we use the row id we were given
        if (queryRowId == -1) {
            queryRowId = insertQuery();
        }
        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createArtistValues(queryRowId);

        // Third Step: Insert ContentValues into database and get a row ID back
        long artistRowId = db.insert(SpotifyContract.ArtistEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                SpotifyContract.ArtistEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from queries query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Queries Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from queries query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();

        return queryRowId;
    }
}

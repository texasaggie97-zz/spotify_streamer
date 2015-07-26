package com.markesilva.spotifystreamer.data;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class SpotifyContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.markesilva.spotifystreamer";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_QUERIES = "queries";
    public static final String PATH_QUERY = "query";
    public static final String PATH_ARTISTS = "artists";
    public static final String PATH_ARTIST = "artist";
    public static final String PATH_ARTIST_ID = "artist_id";
    public static final String PATH_TRACKS = "tracks";
    public static final String PATH_TRACK = "track";

    /* Inner class that defines the table contents of the search query table */
    public static final class SearchQueryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUERIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUERIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUERIES;

        // Table name
        public static final String TABLE_NAME = "queries";

        // The search query that found the artist in this row
        public static final String COLUMN_QUERY_STRING = "query_string";

        // Date/Time of when the query was done
        public static final String COLUMN_QUERY_TIME = "query_time";

        public static Uri buildQueriesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildQueriesWithQuery(String q) {
            return CONTENT_URI.buildUpon().appendPath(q).build();
        }

        public static String getQueryFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the table contents of the artist table */
    public static final class ArtistEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTISTS).build();

        public static final Uri CONTENT_URI_WITH_QUERY =
                CONTENT_URI.buildUpon().appendPath(PATH_QUERY).build();

        public static final Uri CONTENT_URI_WITH_ARTIST =
                CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final Uri CONTENT_URI_WITH_ARTIST_ID =
                CONTENT_URI.buildUpon().appendPath(PATH_ARTIST_ID).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;

        // Table name
        public static final String TABLE_NAME = "artists";

        // Id of the search query
        public static final String COLUMN_SEARCH_ID = "artist_search_id";

        public static final String COLUMN_ARTIST_NAME = "artist_name";

        // Spotify id for this artist
        public static final String COLUMN_ARTIST_SPOTIFY_ID = "spotify_artist_id";

        // Artist thumbnail url.
        public static final String COLUMN_THUMBNAIL_URL = "artist_thumbnail_url";

        public static Uri buildArtistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildArtistsWithQuery(String q) {
            return CONTENT_URI_WITH_QUERY.buildUpon().appendPath(q).build();
        }

        public static Uri buildArtistsWithArtist(String a) {
            return CONTENT_URI_WITH_ARTIST.buildUpon().appendPath(a).build();
        }

        public static Uri buildArtistsWithArtistId(String i) {
            return CONTENT_URI_WITH_ARTIST_ID.buildUpon().appendPath(i).build();
        }

        public static String getArtistFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getArtistIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getQueryFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    /* Inner class that defines the table contents of the track table */
    public static final class TrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACKS).build();

        public static final Uri CONTENT_URI_WITH_QUERY =
                CONTENT_URI.buildUpon().appendPath(PATH_QUERY).build();

        public static final Uri CONTENT_URI_WITH_ARTIST =
                CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;

        public static final String TABLE_NAME = "top_tracks";

        // Spotify id for this artist
        public static final String COLUMN_ARTIST_ID = "track_artist_id";

        // Id of the search query
        public static final String COLUMN_SEARCH_ID = "track_search_id";

        // Track thumbnail url.
        public static final String COLUMN_THUMBNAIL_URL = "track_thumbnail_url";
        // Track image url.
        public static final String COLUMN_IMAGE_URL = "image_url";

        // Album name
        public static final String COLUMN_ALBUM_NAME = "album_name";
        // Track name
        public static final String COLUMN_TRACK_NAME = "track_name";
        // Preview url
        public static final String COLUMN_PREVIEW_URL = "preview_url";

        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTracksWithQuery(String q) {
            return CONTENT_URI_WITH_QUERY.buildUpon().appendPath(q).build();
        }

        public static Uri buildTracksWithArtist(String a) {
            return CONTENT_URI_WITH_ARTIST.buildUpon().appendPath(a).build();
        }

        public static String getArtistFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getQueryFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

//        /*
//            Student: This is the buildWeatherLocation function you filled in.
//         */
//        public static Uri buildWeatherLocation(String locationSetting) {
//            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
//        }
//
//        public static Uri buildWeatherLocationWithStartDate(
//                String locationSetting, long startDate) {
//            long normalizedDate = normalizeDate(startDate);
//            return CONTENT_URI.buildUpon().appendPath(locationSetting)
//                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
//        }
//
//        public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
//            return CONTENT_URI.buildUpon().appendPath(locationSetting)
//                    .appendPath(Long.toString(normalizeDate(date))).build();
//        }
//
//        public static String getLocationSettingFromUri(Uri uri) {
//            return uri.getPathSegments().get(1);
//        }
//
//        public static long getDateFromUri(Uri uri) {
//            return Long.parseLong(uri.getPathSegments().get(2));
//        }
//
//        public static long getStartDateFromUri(Uri uri) {
//            String dateString = uri.getQueryParameter(COLUMN_DATE);
//            if (null != dateString && dateString.length() > 0)
//                return Long.parseLong(dateString);
//            else
//                return 0;
//        }
    }
}


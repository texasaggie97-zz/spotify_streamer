package com.markesilva.spotifystreamer;

import android.support.v4.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.markesilva.spotifystreamer.data.SpotifyContract;
import com.markesilva.spotifystreamer.data.SpotifyProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final private String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private String mArtistQuery = "zzz";
    private ArtistListAdapter mArtistListAdapter = null;
    private ListView mArtistListView = null;
    private SpotifyApi mSpotifyApi = null;
    private SpotifyService mSpotify = null;

    private static final int ARTIST_LOADER = 0;
    // Specify columns we need
    private static final String[] ARTIST_COLUMNS = {
            SpotifyContract.SearchQueryEntry.TABLE_NAME + "." + SpotifyContract.SearchQueryEntry._ID,
            SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING,
            SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME,
            SpotifyContract.ArtistEntry.TABLE_NAME + "." + SpotifyContract.ArtistEntry._ID,
            SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME,
            SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID,
            SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID,
            SpotifyContract.ArtistEntry.COLUMN_THUMBNAIL_URL
    };

    // These indices are ties to ARTIST_COLUMNS. If that changes, these must change too
    static final int COL_QEURY_ID = 0;
    static final int COL_QEURY_STRING = 1;
    static final int COL_QEURY_TIME = 2;
    static final int COL_ARTIST_ID = 3;
    static final int COL_ARTIST_NAME = 4;
    static final int COL_ARTIST_SEARCH_ID = 5;
    static final int COL_ARTIST_SPOTIFY_ID = 6;
    static final int COL_ARTIST_THUMBNAIL_URL = 7;

    public interface Callback {
        // The activity needs to be the one to dispatch this since it can be to another
        // activity via and intent or updating a different fragment in twopane mode
        void onItemSelected(String artistName, String artistSpotifyId);

        // We need to tell the activity who we are
        void setArtistListFragment(MainActivityFragment frag);
    }

    public MainActivityFragment() {
    }

    public static String ARTIST_QUERY = "artist_query";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Tell the main activity who we are so they can tell us when to update the artist list
        ((Callback) getActivity()).setArtistListFragment(this);

        Uri artistUri = SpotifyContract.ArtistEntry.buildArtistsWithArtist(mArtistQuery);
        Cursor cur = getActivity().getContentResolver().query(artistUri, ARTIST_COLUMNS, null, null, null);

        mArtistListAdapter = new ArtistListAdapter(getActivity(), cur, 0);

        // Set up the adapter for the list view
        mArtistListView = (ListView) rootView.findViewById(R.id.artist_list);
        if (mArtistListView == null) {
            Log.v(LOG_TAG, "mArtistListView is null!?");
        } else {
            mArtistListView.setAdapter(mArtistListAdapter);
            mArtistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        ((Callback) getActivity()).onItemSelected(
                                cursor.getString(COL_ARTIST_NAME),
                                cursor.getString(COL_ARTIST_SPOTIFY_ID));
                    }
                }
            });

            // Set up Spotify
            mSpotifyApi = new SpotifyApi();
            mSpotify = mSpotifyApi.getService();

            setRetainInstance(true);
        }
        return rootView;
    }

    // onSaveInstanceState and onActivityCreated with no additional items is enough to handle
    // rotation and not emptying the list
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ARTIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri artistListUri = SpotifyContract.ArtistEntry.buildArtistsWithQuery(mArtistQuery);
        Log.v(LOG_TAG, "mArtistQuery was " + mArtistQuery);
        return new CursorLoader(getActivity(),
                artistListUri,
                ARTIST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mArtistListAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mArtistListAdapter.swapCursor(null);
    }

    public void updateArtistList(String artist) {
        GetArtistInfoTask artistTask = new GetArtistInfoTask(getActivity(), artist);
        mArtistQuery = artist;
        artistTask.execute(artist);
        getLoaderManager().restartLoader(ARTIST_LOADER, null, this);
    }

    // We need to do the actual web query on a thread other than the UI thread. We use AsyncTask for this
    private class GetArtistInfoTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();
        private final int MAX_ARTISTS = 500;
        private String mArtistQuery;
        private Context mContext;

        public GetArtistInfoTask(Context context, String artistQuery) {
            mContext = context;
            mArtistQuery = artistQuery;
        }

        protected List<Artist> doInBackground(String... artist) {
            if (artist.length == 0) {
                return null;
            }
            ArtistsPager p = null;
            List<Artist> allArtists = new ArrayList<>();
            int offset = 0;
            do {
                try {
                    Map<String, Object> options = new HashMap<>();

                    options.put("offset", Integer.toString(offset));

                    p = mSpotify.searchArtists(artist[0], options);

                    // we just add these artists to the list
                    allArtists.addAll(p.artists.items);
                    if (p.artists.next != null) {
                        offset += p.artists.limit;
                    }
                } catch (RetrofitError e) {
                    Log.e(LOG_TAG, "Execption getting artist info" + e);
                }
            } while ((p != null) && (p.artists.next != null) && (offset < 500));

            return allArtists;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);

            if ((artists != null) && (artists.size() < MAX_ARTISTS)) {
                // We found some artists. First we need to delete any records for this search
                Cursor cursor = mContext.getContentResolver().query(
                        SpotifyContract.SearchQueryEntry.CONTENT_URI,
                        null,
                        SpotifyProvider.sQueryStringSelection,
                        new String[]{mArtistQuery},
                        null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(SpotifyContract.SearchQueryEntry._ID);
                    long queryId = cursor.getInt(idx);
                    mContext.getContentResolver().delete(
                            SpotifyContract.ArtistEntry.CONTENT_URI,
                            SpotifyContract.ArtistEntry.TABLE_NAME + "." + SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID + " = ? ",
                            new String[]{String.valueOf(queryId)});
                }
                cursor.close();

                // Now we need to insert the new search query info
                ContentValues queryValues = new ContentValues();
                queryValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING, mArtistQuery);
                long julianTime = System.currentTimeMillis();
                queryValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME, julianTime);
                Uri queryInsertUri = mContext.getContentResolver().insert(
                        SpotifyContract.SearchQueryEntry.CONTENT_URI,
                        queryValues);
                long queryRowId = ContentUris.parseId(queryInsertUri);

                // Put together the vector of the artists we found
                Vector<ContentValues> cvVector = new Vector<>();

                for (Artist a : artists) {
                    ContentValues c = new ContentValues();
                    c.put(SpotifyContract.ArtistEntry.COLUMN_SEARCH_ID, queryRowId);

                    Image thumbnail = null;
                    // store the smallest image that is still at 75 pixels high
                    for (Image i : a.images) {
                        if ((thumbnail == null) || ((i.height < thumbnail.height) && (i.height > 75))) {
                            thumbnail = i;
                        }
                    }
                    if (thumbnail != null) {
                        c.put(SpotifyContract.ArtistEntry.COLUMN_THUMBNAIL_URL, thumbnail.url);
                    } else {
                        c.put(SpotifyContract.ArtistEntry.COLUMN_THUMBNAIL_URL, "");
                    }

                    c.put(SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME, a.name);
                    c.put(SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, a.id);

                    cvVector.add(c);
                    Log.v(LOG_TAG, "Artist = " + a.name);
                }

                ContentValues[] cvArray = new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);
                int inserted = mContext.getContentResolver().bulkInsert(
                        SpotifyContract.ArtistEntry.CONTENT_URI,
                        cvArray);

                Log.d(LOG_TAG, "GetArtistInfoTask Complete. " + inserted + " records inserted");
            }

            if ((artists == null) || (artists.size() == 0)) {
                Toast.makeText(getActivity(), "No matching artists found. Please refine your search", Toast.LENGTH_LONG).show();
            }
            if ((artists != null) && (artists.size() >= MAX_ARTISTS)) {
                Toast.makeText(getActivity(), "Showing first 500 matches. Please refine search term", Toast.LENGTH_LONG).show();
            }
        }
    }
}

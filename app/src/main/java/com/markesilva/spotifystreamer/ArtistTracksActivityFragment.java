package com.markesilva.spotifystreamer;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markesilva.spotifystreamer.data.SpotifyContract;
import com.markesilva.spotifystreamer.data.SpotifyProvider;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * Fragment for the top track view.
 */
public class ArtistTracksActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    final private String LOG_TAG = ArtistTracksActivityFragment.class.getSimpleName();
    private static final int TRACK_LOADER = 1;
    static final String ARTIST_NAME = "artistname";
    static final String ARTIST_ID = "artistid";
    SpotifyApi mSpotifyApi = null;
    SpotifyService mSpotify = null;
    TrackListAdapter mTrackListAdapter = null;
    ListView mTrackListView = null;
    String mArtistName;
    String mArtistId;

    // We need to talk to the music service
    private MediaPlayerService mMusicService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;
    private ServiceConnection mMusicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)service;
            //get service
            mMusicService = binder.getService();
            //pass list
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), MediaPlayerService.class);
            getActivity().bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mMusicConnection);
    }
    private static final String[] TRACK_COLUMNS = {
            SpotifyContract.SearchQueryEntry.TABLE_NAME + "." + SpotifyContract.SearchQueryEntry._ID,
            SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING,
            SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME,
            SpotifyContract.TrackEntry.TABLE_NAME + "." + SpotifyContract.TrackEntry._ID,
            SpotifyContract.TrackEntry.COLUMN_ARTIST_ID,
            SpotifyContract.TrackEntry.COLUMN_PREVIEW_URL,
            SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME,
            SpotifyContract.TrackEntry.COLUMN_IMAGE_URL,
            SpotifyContract.TrackEntry.COLUMN_TRACK_NAME,
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
    static final int COL_TRACK_ID = 3;
    static final int COL_TRACK_ARTIST_ID = 4;
    static final int COL_TRACK_PREVIEW_URL = 5;
    static final int COL_TRACK_ALBUM_NAME = 6;
    static final int COL_TRACK_IMAGE_URL = 7;
    static final int COL_TRACK_NAME = 8;
    static final int COL_ARTIST_ID = 9;
    static final int COL_ARTIST_NAME = 10;
    static final int COL_ARTIST_SEARCH_ID = 11;
    static final int COL_ARTIST_SPOTIFY_ID = 12;
    static final int COL_ARTIST_THUMBNAIL_URL = 13;

    public ArtistTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);

        Bundle args = getArguments();
        if (args != null) {
            mArtistName = args.getString(ARTIST_NAME);
            mArtistId = args.getString(ARTIST_ID);

            Log.v(LOG_TAG, "Starting from intent: " + mArtistName);
            AppCompatActivity a = ((AppCompatActivity) getActivity());
            if (a != null) {
                ActionBar b = a.getSupportActionBar();
                if (b != null) {
                    b.setSubtitle(mArtistName);
                }
            }
        }

        Uri artistUri = SpotifyContract.TrackEntry.buildTracksWithQuery(mArtistId);
        final Cursor cur = getActivity().getContentResolver().query(artistUri, TRACK_COLUMNS, null, null, null);

        mTrackListAdapter = new TrackListAdapter(getActivity(), cur, 0);

        // Set up the adapter for the list view
        mTrackListView = (ListView) rootView.findViewById(R.id.track_list);
        if (mTrackListView == null) {
            Log.v(LOG_TAG, "mTrackListView is null!?");
        } else {
            mTrackListView.setAdapter(mTrackListAdapter);
            mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        Intent playerIntent = new Intent(getActivity(), PreviewPlayerActivity.class);
                        Uri trackListUri = SpotifyContract.TrackEntry.buildTracksWithArtistId(mArtistId);
                        playerIntent.putExtra(PreviewPlayerActivity.TRACK_URI_KEY, trackListUri);
                        playerIntent.putExtra(PreviewPlayerActivity.ROW_NUM_KEY, position);
                        mMusicService.reset();
                        startActivity(playerIntent);
                    }
                }
            });

            // Set up Spotify
            mSpotifyApi = new SpotifyApi();
            mSpotify = mSpotifyApi.getService();

            setRetainInstance(true);
        }

        updateTrackList(mArtistId);
        return rootView;
    }

    // onSaveInstanceState and onActivityCreated with no additional items is enough to handle
    // rotation and not emptying the list
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRACK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri trackListUri = SpotifyContract.TrackEntry.buildTracksWithArtistId(mArtistId);
        Log.v(LOG_TAG, "Query Uri == " + trackListUri);
        return new CursorLoader(getActivity(),
                trackListUri,
                TRACK_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(LOG_TAG, "Loader complete. " + cursor.getCount() + " records found");
        mTrackListAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mTrackListAdapter.swapCursor(null);
    }

    private void updateTrackList(String artistId) {
        GetTrackInfoTask trackTask = new GetTrackInfoTask(getActivity(), artistId);
        trackTask.execute(artistId);
        getLoaderManager().restartLoader(TRACK_LOADER, null, this);
    }

    // We need to do the actual web query on a thread other than the UI thread. We use AsyncTask for this
    private class GetTrackInfoTask extends AsyncTask<String, Void, Tracks> {
        private final String LOG_TAG = GetTrackInfoTask.class.getSimpleName();
        private Context mContext;
        private String mArtistId;

        public GetTrackInfoTask(Context context, String artisId) {
            mContext = context;
            mArtistId = artisId;
        }

        protected Tracks doInBackground(String... artistId) {
            // If we didn;t get an artist ID then we have nothing to do
            if (artistId.length == 0) {
                return null;
            }
            Tracks p = null;

            try {
                Map<String, Object> options = new HashMap<>();

                // We are being very US centric here. This should come from settings or using
                // some other method for determining location
                options.put("country", "US");
                p = mSpotify.getArtistTopTrack(artistId[0], options);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Execption getting artist info" + e);
            }

            return p;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            if (tracks != null) {
                // We found some artists. First we need to delete any records for this search
                Cursor cursor = mContext.getContentResolver().query(
                        SpotifyContract.SearchQueryEntry.CONTENT_URI,
                        null,
                        SpotifyProvider.sQueryStringSelection,
                        new String[]{mArtistId},
                        null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(SpotifyContract.SearchQueryEntry._ID);
                    long queryId = cursor.getInt(idx);
                    mContext.getContentResolver().delete(
                            SpotifyContract.TrackEntry.CONTENT_URI,
                            SpotifyContract.TrackEntry.TABLE_NAME + "." + SpotifyContract.TrackEntry.COLUMN_SEARCH_ID + " = ? ",
                            new String[]{String.valueOf(queryId)});
                }
                cursor.close();

                // Now we need to insert the new search query info
                ContentValues queryValues = new ContentValues();
                queryValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_STRING, mArtistId);
                long julianTime = System.currentTimeMillis();
                queryValues.put(SpotifyContract.SearchQueryEntry.COLUMN_QUERY_TIME, julianTime);
                Uri queryInsertUri = mContext.getContentResolver().insert(
                        SpotifyContract.SearchQueryEntry.CONTENT_URI,
                        queryValues);
                long queryRowId = ContentUris.parseId(queryInsertUri);

                // Now we need to get the row id of the associated artist item
                String[] projection = {SpotifyContract.ArtistEntry.TABLE_NAME + "." + SpotifyContract.ArtistEntry._ID};
                cursor = mContext.getContentResolver().query(
                        SpotifyContract.ArtistEntry.buildArtistsWithArtistId(mArtistId),
                        projection,
                        null,
                        null,
                        null);
                long artistRowId = -1;
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(SpotifyContract.ArtistEntry._ID);
                    artistRowId = cursor.getInt(idx);
                    Log.v(LOG_TAG, "Artist ID: " + artistRowId);
                }
                cursor.close();

                Vector<ContentValues> cvVector = new Vector<>();

                // for each track returned, add to the internal list
                for (Track t : tracks.tracks) {
                    ContentValues c = new ContentValues();
                    c.put(SpotifyContract.TrackEntry.COLUMN_SEARCH_ID, queryRowId);
                    c.put(SpotifyContract.TrackEntry.COLUMN_ARTIST_ID, artistRowId);

                    Image thumbnail = null;
                    Image image = null;
                    for (Image i: t.album.images) {
                        // store the largest image for playback
                        if ((image == null) || (i.height > image.height)) {
                            image = i;
                        }
                        // store the smallest image that is still at least 75 pixels tall, for the list of tracks
                        if ((thumbnail == null) || ((i.height < thumbnail.height) && (i.height >= 75))) {
                            thumbnail = i;
                        }
                    }

                    if (thumbnail != null) {
                        c.put(SpotifyContract.TrackEntry.COLUMN_THUMBNAIL_URL, thumbnail.url);
                    } else {
                        c.put(SpotifyContract.TrackEntry.COLUMN_THUMBNAIL_URL, "");
                    }
                    if (image != null) {
                        c.put(SpotifyContract.TrackEntry.COLUMN_IMAGE_URL, image.url);
                    } else {
                        c.put(SpotifyContract.TrackEntry.COLUMN_IMAGE_URL, "");
                    }

                    c.put(SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME, t.album.name);
                    c.put(SpotifyContract.TrackEntry.COLUMN_TRACK_NAME, t.name);
                    c.put(SpotifyContract.TrackEntry.COLUMN_PREVIEW_URL, t.preview_url);

                    cvVector.add(c);
                    Log.v(LOG_TAG, "Track = " + t.name);
                }

                ContentValues[] cvArray = new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);
                int inserted = mContext.getContentResolver().bulkInsert(SpotifyContract.TrackEntry.CONTENT_URI, cvArray);
                Log.d(LOG_TAG, "GetTrackInfoTask complete. " + inserted + " records inserted");
            }

            if ((tracks == null) || (tracks.tracks.size() == 0)) {
                Toast.makeText(getActivity(), "No tracks for selected artist found. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // listview adapter for track list
    private class TrackListAdapter extends CursorAdapter {
        Context mContext = null;

        public TrackListAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mContext = context;
        }

        // private view holder class
        private class ViewHolder {
            ImageView imageView;
            TextView txtAlbum;
            TextView txtTrack;
        }

        @Override
        public View newView(Context context, Cursor c, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.track_listitem, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.track_listitem_image);
            holder.txtAlbum = (TextView) view.findViewById(R.id.track_listitem_album);
            holder.txtTrack = (TextView) view.findViewById(R.id.track_listitem_track);
            view.setTag(holder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

            if (holder != null) {
                if (cursor.getString(COL_TRACK_IMAGE_URL).trim().equals("")) {
                    Picasso.with(context).load(R.drawable.default_image).error(R.drawable.image_download_error).into(holder.imageView);
                } else {
                    Picasso.with(context).load(cursor.getString(COL_TRACK_IMAGE_URL)).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(holder.imageView);
                }
                holder.txtAlbum.setText(cursor.getString(COL_TRACK_ALBUM_NAME));
                holder.txtTrack.setText(cursor.getString(COL_TRACK_NAME));
            }
        }
    }
}

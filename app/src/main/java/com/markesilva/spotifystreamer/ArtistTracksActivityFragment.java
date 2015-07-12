package com.markesilva.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * Fragment for the top track view.
 */
public class ArtistTracksActivityFragment extends Fragment {
    final private String LOG_TAG = ArtistTracksActivityFragment.class.getSimpleName();
    static final String ARTIST_NAME = "artistname";
    static final String ARTIST_ID = "artistid";
    SpotifyApi mSpotifyApi = null;
    SpotifyService mSpotify = null;
    List<TrackListRow> mTrackList = null;
    TrackListAdapter mTrackListAdapter = null;
    ListView mTrackListView = null;
    String mArtistName;
    String mArtistId;

    public ArtistTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_tracks, container, false);

        if (mTrackList == null) {
            mTrackList = new ArrayList<>();
        }

        mTrackListAdapter = new TrackListAdapter(getActivity(), mTrackList);

        // Set up the adapter for the list view
        mTrackListView = (ListView) rootView.findViewById(R.id.track_list);
        if (mTrackListView == null) {
            Log.v(LOG_TAG, "mTrackListView is null!?");
        } else {
            mTrackListView.setAdapter(mTrackListAdapter);
            // Don't need a click listener yet
            /*mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArtistListRow rowItem = (ArtistListRow) mTrackListView.getItem(position);
                    String artistId = rowItem.getId();
                    Intent artistTracksIntent = new Intent(getActivity(), ArtistTracksActivity.class);
                    artistTracksIntent.putExtra("artistId", artistId);
                    startActivity(artistTracksIntent);
                }
            });*/

            // Set up Spotify
            mSpotifyApi = new SpotifyApi();
            mSpotify = mSpotifyApi.getService();

            setRetainInstance(true);
        }

        Bundle args = getArguments();
        if (args != null) {
            mArtistName = args.getString(ARTIST_NAME);
            mArtistId = args.getString(ARTIST_ID);

            Log.v(LOG_TAG, "Starting from intent: " + mArtistName);
            ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(mArtistName);
            updateTrackList(mArtistId);
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
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateTrackList(String artist) {
        GetTrackInfoTask trackTask = new GetTrackInfoTask();
        trackTask.execute(artist);
    }

    // We need to do the actual web query on a thread other than the UI thread. We use AsyncTask for this
    private class GetTrackInfoTask extends AsyncTask<String, Void, Tracks> {
        private final String LOG_TAG = GetTrackInfoTask.class.getSimpleName();

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
                mTrackList.clear();

                // for each track returned, add to the internal list
                for (Track t : tracks.tracks) {
                    TrackListRow rowItem = new TrackListRow(t.album.images, t.album.name, t.name, t.preview_url);
                    mTrackList.add(rowItem);
                    Log.v(LOG_TAG, "Track = " + t.name);
                }
                mTrackListAdapter.setList(mTrackList);
                mTrackListAdapter.notifyDataSetChanged();
            }

            if ((tracks == null) || (tracks.tracks.size() == 0)) {
                Toast.makeText(getActivity(), "No tracks for selected artist found. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Container for all information we need for top tracks for an artist
    private class TrackListRow {
        private String album;
        private String track;
        private String previewURL;
        private Image image;
        private Image thumbnail;

        public TrackListRow(List<Image> images, String album, String track, String previewURL) {
            this.image = null;
            this.thumbnail = null;
            for (Image i : images) {
                // store the largest image for playback
                if ((image == null) || (i.height > image.height)) {
                    this.image = i;
                }
                // store the smallest image that is still at least 75 pixels tall, for the list of tracks
                if ((thumbnail == null) || ((i.height < thumbnail.height) && (i.height > 75))) {
                    this.thumbnail = i;
                }
            }
            this.album = album;
            this.track = track;
            this.previewURL = previewURL;
        }

        // Get/Set
        public Image getImage() {
            return image;
        }

        public Image getThumbnail() {
            return thumbnail;
        }

        public String getAlbum() {
            return album;
        }

        public String getTrack() {
            return track;
        }

        public String getPreviewURL() {
            return previewURL;
        }

        @Override
        public String toString() {
            return album + "/" + track;
        }
    }

    // listview adapter for track list
    private class TrackListAdapter extends BaseAdapter {
        List<TrackListRow> mTrackList = null;
        Context mContext = null;

        public TrackListAdapter(Context c, List<TrackListRow> info) {
            mContext = c;
            mTrackList = info;
        }

        // private view holder class
        private class ViewHolder {
            ImageView imageView;
            TextView txtAlbum;
            TextView txtTrack;
        }

        public void setList(List<TrackListRow> info) {
            mTrackList = info;
        }

        @Override
        public int getCount() {
            if (mTrackList != null) {
                return mTrackList.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int index) {
            return mTrackList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return mTrackList.indexOf(getItem(index));
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.track_listitem, null);
                holder = new ViewHolder();
                holder.txtAlbum = (TextView) convertView.findViewById(R.id.track_listitem_album);
                holder.txtTrack = (TextView) convertView.findViewById(R.id.track_listitem_track);
                holder.imageView = (ImageView) convertView.findViewById(R.id.track_listitem_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TrackListRow rowItem = (TrackListRow) getItem(index);

            if (rowItem.getImage() != null) {
                Picasso.with(mContext).load(rowItem.getThumbnail().url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(holder.imageView);
            }
            holder.txtAlbum.setText(rowItem.getAlbum());
            holder.txtTrack.setText(rowItem.getTrack());

            return convertView;
        }
    }
}

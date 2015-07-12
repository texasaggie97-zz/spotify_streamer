package com.markesilva.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    final private String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public interface Callback {
        // The activity needs to be the one to dispatch this since it can be to another
        // activity via and intent or updating a different fragment in twopane mode
        public void onItemSelected(ArtistListRow a);

        // We need to tell the activity who we are
        public void setArtistListFragment(MainActivityFragment frag);
    }

    public MainActivityFragment() {
    }

    ArtistListAdapter mArtistListAdapter = null;
    List<ArtistListRow> mArtistList = null;
    ListView mArtistListView = null;
    SpotifyApi mSpotifyApi = null;
    SpotifyService mSpotify = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (mArtistList == null) {
            mArtistList = new ArrayList<>();
        }

        // Tell the main activity who we are so they can tell us when to update the artist list
        ((Callback) getActivity()).setArtistListFragment(this);

        mArtistListAdapter = new ArtistListAdapter(getActivity(), mArtistList);

        // Set up the adapter for the list view
        mArtistListView = (ListView) rootView.findViewById(R.id.artist_list);
        if (mArtistListView == null) {
            Log.v(LOG_TAG, "mArtistListView is null!?");
        } else {
            mArtistListView.setAdapter(mArtistListAdapter);
            mArtistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArtistListRow rowItem = (ArtistListRow) mArtistListAdapter.getItem(position);
                    ((Callback) getActivity()).onItemSelected(rowItem);
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
        super.onActivityCreated(savedInstanceState);
    }

    public void updateArtistList(String artist) {
        GetArtistInfoTask artistTask = new GetArtistInfoTask();
        artistTask.execute(artist);
    }

    // We need to do the actual web query on a thread other than the UI thread. We use AsyncTask for this
    private class GetArtistInfoTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();

        protected List<Artist> doInBackground(String... artist) {
            if (artist.length == 0) {
                return null;
            }
            ArtistsPager p = null;
            List<Artist> allArists = new ArrayList<>();
            int offset = 0;
            do {
                try {
                    Map<String, Object> options = new HashMap<>();

                    options.put("offset", Integer.toString(offset));

                    p = mSpotify.searchArtists(artist[0], options);

                    // we just add these artists to the list
                    allArists.addAll(p.artists.items);
                    if (p.artists.next != null) {
                        offset += p.artists.limit;
                    }
                } catch (RetrofitError e) {
                    Log.e(LOG_TAG, "Execption getting artist info" + e);
                }
            } while ((p != null) && (p.artists.next != null) && (offset < 500));

            return allArists;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);

            if (artists != null) {
                mArtistList.clear();

                for (Artist a : artists) {
                    ArtistListRow rowItem = new ArtistListRow(a.images, a.name, a.id);
                    mArtistList.add(rowItem);
                    Log.v(LOG_TAG, "Artist = " + a.name);
                }
                mArtistListAdapter.setList(mArtistList);
                mArtistListAdapter.notifyDataSetChanged();
            }

            if ((artists == null) || (artists.size() == 0)) {
                Toast.makeText(getActivity(), "No matching artists found. Please refine your search", Toast.LENGTH_LONG).show();
            }
            if ((artists == null) || (artists.size() >= 500)) {
                Toast.makeText(getActivity(), "Showing first 500 matches. Please refine search term", Toast.LENGTH_LONG).show();
            }
        }
    }
}

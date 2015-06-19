package com.markesilva.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    final private String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public MainActivityFragment() {
    }

    ArtistListAdapter mArtistListAdapter = null;
    List<ArtistListRow> mArtistList = null;
    ListView mArtistListView = null;
    SpotifyApi mSpotifyApi = null;
    SpotifyService mSpotify = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up the adapter for the list view
        mArtistListView = (ListView) rootView.findViewById(R.id.artist_list);
        mArtistList = new ArrayList<ArtistListRow>();
        if (mArtistListView == null)
        {
            Log.v(LOG_TAG, "mArtistListView is null!?");
        }
        else
        {
            mArtistListAdapter = new ArtistListAdapter(getActivity(), mArtistList);
            mArtistListView.setAdapter(mArtistListAdapter);
            mArtistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArtistListRow rowItem = (ArtistListRow) mArtistListAdapter.getItem(position);
                    String artistName = rowItem.getName();
                    Intent artistTracksIntent = new Intent(getActivity(), ArtistTracksActivity.class);
                    artistTracksIntent.putExtra("artistName", artistName);
                    startActivity(artistTracksIntent);
                }
            });

        }

        // Setup the TextEdit callback
        EditText editText = (EditText) rootView.findViewById(R.id.artist_input);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    updateArtistList(v.getText().toString());
                    handled = true;
                }
                else
                {
                    Log.v(LOG_TAG, "actionId = " + actionId);
                }
                return handled;
            }
        });

        // Set up Spotify
        mSpotifyApi = new SpotifyApi();
        mSpotify = mSpotifyApi.getService();

        return rootView;
    }

    private void updateArtistList(String artist)
    {
        GetArtistInfoTask artistTask = new GetArtistInfoTask();
        artistTask.execute(artist);
    }

    private class GetArtistInfoTask extends AsyncTask<String, Void, List<Artist>>
    {
        private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();

        protected List<Artist> doInBackground(String... artist)
        {
            if (artist.length == 0)
            {
                return null;
            }
            ArtistsPager p = null;
            List<Artist> allArists = new ArrayList<Artist>();
            int offset = 0;
            do {
                try
                {
                    Map<String, Object> options = new HashMap<String, Object>();

                    options.put("offset", Integer.toString(offset));

                    p = mSpotify.searchArtists(artist[0], options);

                    // we just add these artists to the list
                    allArists.addAll(p.artists.items);
                    if ((p != null) && (p.artists.next != null))
                    {
                        offset += p.artists.limit;
                    }
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG, "Execption getting artist info" + e);
                }
            } while((p != null) && (p.artists.next != null));

            return allArists;
        }

        @Override
        protected void onPostExecute(List<Artist> artists)
        {
            super.onPostExecute(artists);

            if (artists != null)
            {
                mArtistList.clear();

                for (Artist a: artists)
                {
                    ArtistListRow rowItem = new ArtistListRow(a.images, a.name);
                    mArtistList.add(rowItem);
                    Log.v(LOG_TAG, "Artist = " + a.name);
                }
                // mArtistListAdapter.setList(artistList);
                mArtistListAdapter.notifyDataSetChanged();
            }
        }
    }
}

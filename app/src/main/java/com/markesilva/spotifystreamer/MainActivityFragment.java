package com.markesilva.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

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
        if (mArtistListView == null)
        {
            Log.v(LOG_TAG, "mArtistListView is null!?");
        }
        else
        {
            ArtistListAdapter adapter = new ArtistListAdapter(getActivity(), mArtistList);
            mArtistListView.setAdapter(adapter);
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

    private class GetArtistInfoTask extends AsyncTask<String, Void, ArtistsPager>
    {
        private final String LOG_TAG = GetArtistInfoTask.class.getSimpleName();

        protected ArtistsPager doInBackground(String... artist)
        {
            if (artist.length == 0)
            {
                return null;
            }
            ArtistsPager p = null;
            try
            {
                p = mSpotify.searchArtists(artist[0]);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Execption getting artist info" + e);
            }

            return p;
        }

        @Override
        protected void onPostExecute(ArtistsPager p)
        {
            super.onPostExecute(p);

            if (p != null)
            {
                for (int i = 0; i < p.artists.total; i++)
                {
                    Artist a = p.artists.items.get(i);
                    Log.v(LOG_TAG, "Artist = " + a.name);
                }
            }
        }
    }
}

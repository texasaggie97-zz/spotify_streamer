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

import java.io.Serializable;
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
public class MainActivityFragment extends Fragment implements View.OnKeyListener {

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

        if (mArtistList == null)
        {
            mArtistList = new ArrayList<>();
        }

        mArtistListAdapter = new ArtistListAdapter(getActivity(), mArtistList);
        EditText artistName = (EditText) rootView.findViewById(R.id.artist_input);
        artistName.setOnKeyListener(this);

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
                    String artistName = rowItem.getName();
                    Intent artistTracksIntent = new Intent(getActivity(), ArtistTracksActivity.class);
                    artistTracksIntent.putExtra("artistName", artistName);
                    startActivity(artistTracksIntent);
                }
            });

            // Set up Spotify
            mSpotifyApi = new SpotifyApi();
            mSpotify = mSpotifyApi.getService();

            setRetainInstance(true);
        }
        return rootView;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event)
    {
        EditText v = (EditText) view;
        Log.v(LOG_TAG, "A key pressed");

        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
        {
            if (!event.isShiftPressed())
            {
                Log.v(LOG_TAG, "Enter key pressed");
                updateArtistList(v.getText().toString());
                return true;
            }
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", (Serializable) mArtistList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
        {
            //probably orientation change
            mArtistList = (List<ArtistListRow>) savedInstanceState.getSerializable("list");
        }
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
            List<Artist> allArists = new ArrayList<>();
            int offset = 0;
            do {
                try
                {
                    Map<String, Object> options = new HashMap<>();

                    options.put("offset", Integer.toString(offset));

                    p = mSpotify.searchArtists(artist[0], options);

                    // we just add these artists to the list
                    allArists.addAll(p.artists.items);
                    if (p.artists.next != null)
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
                mArtistListAdapter.setList(mArtistList);
                mArtistListAdapter.notifyDataSetChanged();
            }
        }
    }
}

package com.markesilva.spotifystreamer;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback {

    final private String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String ARTISTTRACKFRAGMENT_TAG = "ATFTAG";
    private MainActivityFragment mFrag;
    private SearchView mSearch;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearch = (SearchView) findViewById(R.id.artist_input);
        mSearch.setQueryHint(getResources().getString(R.string.search_hint));

        mSearch.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Toast.makeText(getBaseContext(), String.valueOf(hasFocus), Toast.LENGTH_SHORT).show();
            }
        });

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getBaseContext(), query, Toast.LENGTH_SHORT).show();
                if (mFrag != null) {
                    mFrag.updateArtistList(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mFrag != null) {
                    mFrag.updateArtistList(newText);
                }
                return false;
            }
        });

        if (findViewById(R.id.artist_tracks_container) != null) {
            // This view will only be present in large screens (res/layout-sw600dp)
            mTwoPane = true;
            // We need to replace the or add the fragment into the conatiner frame
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.artist_tracks_container, new ArtistTracksActivityFragment(), ARTISTTRACKFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    public void setArtistListFragment(MainActivityFragment frag) {
        mFrag = frag;
    }


    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            MainActivityFragment frag = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_activity_fragment);
            frag.updateArtistList(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(ArtistListRow a) {
        if (mTwoPane) {
            // If we are on a large screen, add or replace the track list fragment
            Bundle args = new Bundle();
            args.putString(ArtistTracksActivityFragment.ARTIST_NAME, a.getName());
            args.putString(ArtistTracksActivityFragment.ARTIST_ID, a.getId());

            ArtistTracksActivityFragment fragment = new ArtistTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_tracks_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, ArtistTracksActivity.class)
                    .putExtra(ArtistTracksActivityFragment.ARTIST_NAME, a.getName())
                    .putExtra(ArtistTracksActivityFragment.ARTIST_ID, a.getId());
            startActivity(intent);
        }
    }
}

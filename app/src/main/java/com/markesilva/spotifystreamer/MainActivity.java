package com.markesilva.spotifystreamer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.markesilva.spotifystreamer.utils.NotificationHelper;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private static final String ARTISTTRACKFRAGMENT_TAG = "ATFTAG";
    final private String LOG_TAG = MainActivity.class.getSimpleName();
    private MainActivityFragment mFrag;
    private SearchView mSearch;

    private boolean mTwoPane;

    // The main activity will own the music service so that it is available at all times
    private Activity mActivity = this;

    // Set up the BroadcastReceiver
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive");
            HandleBroadcast h = new HandleBroadcast(intent, mActivity);
            h.execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearch = (SearchView) findViewById(R.id.artist_input);
        mSearch.setQueryHint(getResources().getString(R.string.search_hint));

        mSearch.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mFrag != null) {
                    Log.d(LOG_TAG, "onQueryTextSubmit");
                    mFrag.updateArtistList(query.trim());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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
            ActionBar a = getSupportActionBar();
            if (a != null) {
                a.setElevation(0f);
            }
        }

        // We don't want to show the notification at this time so always pass is false.
        NotificationHelper.setActivity(this);
        NotificationHelper.configureNotification(false);
    }

    public void setArtistListFragment(MainActivityFragment frag) {
        Log.d(LOG_TAG, "setArtistListFragment");
        mFrag = frag;
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_SONG_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_STATE_UPDATED));
        startService(new Intent(mActivity, MediaPlayerService.class));
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if (isFinishing()) {
            stopService(new Intent(mActivity, MediaPlayerService.class));
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(LOG_TAG, "onNewIntent");
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(LOG_TAG, "handleIntent");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            MainActivityFragment frag = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_activity_fragment);
            frag.updateArtistList(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String artistName, String artistSpotifyId) {
        Log.d(LOG_TAG, "onItemSelected");
        if (mTwoPane) {
            // If we are on a large screen, add or replace the track list fragment
            Bundle args = new Bundle();
            args.putString(ArtistTracksActivityFragment.ARTIST_NAME, artistName);
            args.putString(ArtistTracksActivityFragment.ARTIST_ID, artistSpotifyId);

            ArtistTracksActivityFragment fragment = new ArtistTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_tracks_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, ArtistTracksActivity.class)
                    .putExtra(ArtistTracksActivityFragment.ARTIST_NAME, artistName)
                    .putExtra(ArtistTracksActivityFragment.ARTIST_ID, artistSpotifyId);
            startActivity(intent);
        }
    }

    private class HandleBroadcast extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = HandleBroadcast.class.getSimpleName();
        private Intent mIntent;
        private Activity mActivity;

        public HandleBroadcast(Intent intent, Activity activity) {
            Log.d(LOG_TAG, "HandleBroadcast");
            mActivity = activity;
            mIntent = intent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "doInBackground");
            String action = mIntent.getAction();
            if (action != null) {
                switch (action) {
                    case MediaPlayerService.BROADCAST_STATE_UPDATED:
                        NotificationHelper.updatePlayButton(mIntent, Utility.getPreferredNotificationEnabled(mActivity));
                        break;
                    case MediaPlayerService.BROADCAST_SONG_UPDATED:
                        NotificationHelper.updateNotificationViews(mIntent, Utility.getPreferredNotificationEnabled(mActivity));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action" + action);
                }
            }
            return null;
        }
    }
}

package com.markesilva.spotifystreamer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
import com.markesilva.spotifystreamer.utils.ReloadPlayer;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    final private String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String ARTISTTRACKFRAGMENT_TAG = "ATFTAG";
    private MainActivityFragment mFrag;
    private SearchView mSearch;

    // Notification
    private NotificationHelper mNotificationHelper = new NotificationHelper(this);

    private boolean mTwoPane;

    // The main activity will own the music service so that it is available at all times
    private Intent mPlayIntent;
    private ServiceConnection mMusicConnection;
    private MediaPlayerService mMusicService;
    private boolean mMusicBound = false;
    private ReloadPlayer mReloadPlayer;
    private Activity mActivity = this;

    // Set up the BroadcastReceiver
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HandleBroadcast h = new HandleBroadcast(intent, mActivity);
            h.execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        mMusicConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)service;
                //get service
                mMusicService = binder.getService();

                mMusicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMusicBound = false;
            }
        };

        // We don't want to show the notification at this time so always pass is false.
        mNotificationHelper.configureNotification(false);
    }

    public void setArtistListFragment(MainActivityFragment frag) {
        mFrag = frag;
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_SONG_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_STATE_UPDATED));
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MediaPlayerService.class);
            bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        unbindService(mMusicConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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
        MenuInflater inflater = getMenuInflater();
        mReloadPlayer = new ReloadPlayer();
        mReloadPlayer.menuInflator(mMusicService, this, inflater, menu);
        inflater.inflate(R.menu.menu_main, menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.reload_player) {
            return mReloadPlayer.relauchPlayer(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String artistName, String artistSpotifyId) {
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
            mActivity = activity;
            mIntent = intent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String action = mIntent.getAction();
            if (action != null) {
                switch (action) {
                    case MediaPlayerService.BROADCAST_STATE_UPDATED:
                        mNotificationHelper.updatePlayButton(mIntent, Utility.getPreferredNotificationEnabled(mActivity));
                        break;
                    case MediaPlayerService.BROADCAST_SONG_UPDATED:
                        mNotificationHelper.updateNotificationViews(mIntent, Utility.getPreferredNotificationEnabled(mActivity));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action" + action);
                }
            }
            return null;
        }
    }
}

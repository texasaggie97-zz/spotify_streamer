package com.markesilva.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.markesilva.spotifystreamer.utils.ReloadPlayer;


public class ArtistTracksActivity extends AppCompatActivity {

    // We need access to the music player service
    private Intent mPlayIntent;
    private ServiceConnection mMusicConnection;
    private MediaPlayerService mMusicService;
    private boolean mMusicBound = false;
    private ReloadPlayer mReloadPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks);

        if (savedInstanceState == null) {
            // Create the fragment and add it

            Bundle args = new Bundle();
            args.putString(ArtistTracksActivityFragment.ARTIST_NAME, getIntent().getStringExtra(ArtistTracksActivityFragment.ARTIST_NAME));
            args.putString(ArtistTracksActivityFragment.ARTIST_ID, getIntent().getStringExtra(ArtistTracksActivityFragment.ARTIST_ID));

            ArtistTracksActivityFragment frag = new ArtistTracksActivityFragment();
            frag.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_tracks_container, frag)
                    .commit();
        }

        mMusicConnection = new ServiceConnection(){

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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MediaPlayerService.class);
            bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mMusicConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        mReloadPlayer = new ReloadPlayer();
        mReloadPlayer.menuInflator(mMusicService, this, inflater, menu);
        inflater.inflate(R.menu.menu_artist_tracks, menu);
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
}

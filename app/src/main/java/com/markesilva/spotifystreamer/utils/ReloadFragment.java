package com.markesilva.spotifystreamer.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.markesilva.spotifystreamer.MediaPlayerService;
import com.markesilva.spotifystreamer.PreviewPlayerActivity;
import com.markesilva.spotifystreamer.R;

/**
 * Created by marke on 8/1/2015.
 * <p/>
 * Utility class for helping with relaunching the preview player from multiple places
 */
public class ReloadFragment extends Fragment {
    private static final String LOG_TAG = ReloadFragment.class.getSimpleName();

    // We need to be able to query the media player service t oget the current state
    private Intent mPlayIntent;
    private ServiceConnection mMusicConnection;
    private MediaPlayerService mMusicService;
    private boolean mMusicBound = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reload, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMusicConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(LOG_TAG, "onServiceConnected");
                MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder) service;
                //get service
                mMusicService = binder.getService();
                //pass list
                mMusicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "onServiceDisconnected");
                mMusicBound = false;
            }
        };

        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), MediaPlayerService.class);
            getActivity().bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayIntent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.reload_player, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reload_player) {
            return relauchPlayer(item);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean relauchPlayer(MenuItem item) {
        Log.d(LOG_TAG, "relauchPlayer");
        if (mMusicService.isPlaying() != MediaPlayerService.tPlayerState.idle) {
            Intent playerIntent = new Intent(getActivity(), PreviewPlayerActivity.class);
            getActivity().startActivity(playerIntent);
            return true;
        } else {
            Toast.makeText(getActivity(), "Player is not active", Toast.LENGTH_LONG).show();
            return true;
        }
    }
}

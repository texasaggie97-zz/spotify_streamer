package com.markesilva.spotifystreamer.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.markesilva.spotifystreamer.MediaPlayerService;
import com.markesilva.spotifystreamer.PreviewPlayerActivity;
import com.markesilva.spotifystreamer.R;

/**
 * Created by marke on 8/1/2015.
 *
 * Utility class for helping with relaunching the preview player from multiple places
 */
public class ReloadPlayer {
    private static final String LOG_TAG = ReloadPlayer.class.getSimpleName();
    private AppCompatActivity mActivity;
    private Menu mMenu;
    private MediaPlayerService mMediaPlayerService;

    public void menuInflator(MediaPlayerService svc, AppCompatActivity a, MenuInflater inflater, Menu m) {
        mActivity = a;
        mMenu = m;
        mMediaPlayerService = svc;
        inflater.inflate(R.menu.reload_player, mMenu);
    }


    public boolean relauchPlayer(MenuItem item) {
        if (mMediaPlayerService.isPlaying() == MediaPlayerService.tPlayerState.playing) {
            Intent playerIntent = new Intent(mActivity, PreviewPlayerActivity.class);
            mActivity.startActivity(playerIntent);
            return true;
        } else {
            Toast.makeText(mActivity, "Player is not active", Toast.LENGTH_LONG).show();
            return true;
        }
    }
}

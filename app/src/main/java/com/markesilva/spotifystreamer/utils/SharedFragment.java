package com.markesilva.spotifystreamer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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
public class SharedFragment extends Fragment {
    private static final String LOG_TAG = LogUtils.makeLogTag(SharedFragment.class);

    // Handling the media player service
    private MediaPlayerService.tPlayerState mPlayerState;
    // sharing
    private ShareActionProvider mShareTrackActionProvider;
    private String mTrackName;
    private String mArtistName;
    private String mTrackUrl;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MediaPlayerService.BROADCAST_STATE_UPDATED:
                        updateRunState(intent);
                        break;
                    case MediaPlayerService.BROADCAST_SONG_UPDATED:
                        updateTrackInfo(intent);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action" + action);
                }
            }
        }
    };

    public void updateRunState(Intent intent) {
        mPlayerState = (MediaPlayerService.tPlayerState) intent.getSerializableExtra(MediaPlayerService.BROADCAST_STATE_UPDATED_STATE);
    }

    public void updateTrackInfo(Intent intent) {
        LogUtils.LOGV(LOG_TAG, "updateTrackInfo");
        mTrackName = intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_TRACK);
        mArtistName = intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_ARTIST);
        mTrackUrl = intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_TRACK_URL);

        mShareTrackActionProvider.setShareIntent(createTrackShareIntent());
        getActivity().invalidateOptionsMenu();
    }

    private Intent createTrackShareIntent() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "I am currently listening to " + mTrackName + " by " + mArtistName);
        return i;
    }

    private Intent createUrlShareIntent() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, mTrackUrl);
        return i;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reload, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.LOGV(LOG_TAG, "onCreateView");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LogUtils.LOGV(LOG_TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.shared_menu, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share_track);

        // Get the provider and hold onto it to set/change the share intent.
        mShareTrackActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_SONG_UPDATED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_STATE_UPDATED));

        // We need to tell the media service to rebroadcast song data
        Intent i = new Intent(getActivity(), MediaPlayerService.class);
        i.setAction(MediaPlayerService.ACTION_SEND_UPDATES);
        getActivity().startService(i);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.LOGV(LOG_TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reload_player) {
            return relauchPlayer(item);
        } else if (id == R.id.action_share_preview_url) {
            startActivity(Intent.createChooser(createUrlShareIntent(), getString(R.string.share_via)));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean relauchPlayer(MenuItem item) {
        LogUtils.LOGV(LOG_TAG, "relauchPlayer");
        if (mPlayerState != MediaPlayerService.tPlayerState.idle) {
            Intent playerIntent = new Intent(getActivity(), PreviewPlayerActivity.class);
            getActivity().startActivity(playerIntent);
            return true;
        } else {
            Toast.makeText(getActivity(), "Player is not active", Toast.LENGTH_LONG).show();
            return true;
        }
    }
}

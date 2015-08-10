package com.markesilva.spotifystreamer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
public class SharedFragment extends Fragment {
    private static final String LOG_TAG = SharedFragment.class.getSimpleName();

    private MediaPlayerService.tPlayerState mPlayerState;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MediaPlayerService.BROADCAST_STATE_UPDATED:
                        updateRunState(intent);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.shared_menu, menu);
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

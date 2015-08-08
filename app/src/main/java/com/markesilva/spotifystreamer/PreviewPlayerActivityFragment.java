package com.markesilva.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class PreviewPlayerActivityFragment extends Fragment {
    private static final String LOG_TAG = PreviewPlayerActivityFragment.class.getSimpleName();
    private static final String POSITION_KEY = "position";

    private TextView mAlbumText;
    private TextView mTrackText;
    private TextView mArtistText;
    private ImageView mImageView;
    private SeekBar mSeekBar;
    private ImageButton mNext;
    private ImageButton mPrev;
    private ImageButton mPlay;
    private View mRootView;
    private Uri mTrackUri;
    private int mPosition;

    // Set up the BroadcastReceiver
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
                        updateViews(intent);
                        break;
                    case MediaPlayerService.BROADCAST_DURATION_UPDATED:
                        updateDuration(intent);
                        break;
                    case MediaPlayerService.BROADCAST_POSITION_UPDATED:
                        updatePosition(intent);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action" + action);
                }
            }
        }
    };

    PreviewPlayerActivity mActivity;

    public PreviewPlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.fragment_preview_player, container, false);

        mActivity = (PreviewPlayerActivity) getActivity();
        mTrackUri = mActivity.getIntent().getParcelableExtra(PreviewPlayerActivity.TRACK_URI_KEY);
        // We check to see if a cursor position has been saves in the instance data
        if ((savedInstanceState == null) || (savedInstanceState.getInt(POSITION_KEY, -1) == -1)) {
            mPosition = mActivity.getIntent().getIntExtra(PreviewPlayerActivity.ROW_NUM_KEY, -1);
        } else {
            mPosition = savedInstanceState.getInt(POSITION_KEY, -1);
        }

        mAlbumText = (TextView) mRootView.findViewById(R.id.player_album_name);
        mTrackText = (TextView) mRootView.findViewById(R.id.player_track_name);
        mArtistText = (TextView) mRootView.findViewById(R.id.player_artist_name);
        mImageView = (ImageView) mRootView.findViewById(R.id.player_track_image);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.player_progress_bar);
        mNext = (ImageButton) mRootView.findViewById(R.id.player_next_button);
        mPrev = (ImageButton) mRootView.findViewById(R.id.player_back_button);
        mPlay = (ImageButton) mRootView.findViewById(R.id.player_play_pause_button);

        if ((mNext == null) || (mPrev == null) || (mPlay == null)) {
            // If any of these are null, then something is wrong
            Log.e(LOG_TAG, "ERROR: One or more button widgets were not found!");
        } else {
            mNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, MediaPlayerService.class);

                    // Add the data
                    intent.setAction(MediaPlayerService.ACTION_NEXT);

                    mActivity.startService(intent);
                }
            });
            mPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, MediaPlayerService.class);

                    // Add the data
                    intent.setAction(MediaPlayerService.ACTION_PREV);

                    mActivity.startService(intent);
                }
            });
            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, MediaPlayerService.class);

                    // Add the data
                    intent.setAction(MediaPlayerService.ACTION_PLAY);

                    mActivity.startService(intent);
                }
            });
        }

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int songPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songPosition = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(mActivity, MediaPlayerService.class);

                // Add the data
                intent.setAction(MediaPlayerService.ACTION_UPDATE_POSITION);
                intent.putExtra(MediaPlayerService.ACTION_UPDATE_POSITION_POSITION, songPosition);

                mActivity.startService(intent);
            }
        });

        // Update the music service
        Intent intent = new Intent(mActivity, MediaPlayerService.class);

        intent.setAction(MediaPlayerService.ACTION_UPDATE_TRACK_LIST);
        intent.putExtra(MediaPlayerService.ACTION_UPDATE_TRACK_LIST_URI, mTrackUri);
        intent.putExtra(MediaPlayerService.ACTION_UPDATE_TRACK_LIST_POSITION, mPosition);

        mActivity.startService(intent);

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_SONG_UPDATED));
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_STATE_UPDATED));
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_DURATION_UPDATED));
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver, new IntentFilter(MediaPlayerService.BROADCAST_POSITION_UPDATED));
    }

    public void updateViews(Intent intent) {
        Log.d(LOG_TAG, "updateViews");
        if (mAlbumText != null) {
            mAlbumText.setText(intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_ALBUM));
        }
        if (mTrackText != null) {
            mTrackText.setText(intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_TRACK));
        }
        if (mArtistText != null) {
            mArtistText.setText(intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_ARTIST));
        }
        if (mImageView != null) {
            String image_url = intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_IMAGE_URL);
            if (image_url.trim().equals("")) {
                Picasso.with(mActivity).load(R.drawable.default_image).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(mImageView);
            } else {
                Picasso.with(mActivity).load(image_url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(mImageView);
            }
        }
    }

    public void updateRunState(Intent intent) {
        if (mPlay != null) {
            MediaPlayerService.tPlayerState playerState = (MediaPlayerService.tPlayerState) intent.getSerializableExtra(MediaPlayerService.BROADCAST_STATE_UPDATED_STATE);
            if ((playerState == MediaPlayerService.tPlayerState.playing) || (playerState == MediaPlayerService.tPlayerState.preparing)) {
                mPlay.setImageResource(R.drawable.ic_pause_black_48dp);
            } else {
                mPlay.setImageResource(R.drawable.ic_play_arrow_black_48dp);
            }
        }
    }

    public void updateDuration(Intent intent) {
        if (mSeekBar != null) {
            mSeekBar.setMax(intent.getIntExtra(MediaPlayerService.BROADCAST_DURATION_UPDATED_DURATION, 0));
        }
    }

    public void updatePosition(Intent intent) {
        if (mSeekBar != null) {
            int pos = intent.getIntExtra(MediaPlayerService.BROADCAST_POSITION_UPDATED_POSITION, 0);
            mSeekBar.setProgress(pos);
        }
    }
}

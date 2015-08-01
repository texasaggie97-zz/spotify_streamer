package com.markesilva.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.markesilva.spotifystreamer.data.SpotifyContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


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
    private int mPlayDrawable;
    private int mPauseDrawable;
    private View mRootView;
    private Uri mTrackUri;
    private int mPosition;
    // private ServiceConnection mMusicConnection;
    private Intent mPlayIntent;
    private MediaPlayerService mMusicService;
    private boolean mMusicBound = false;
    private ServiceConnection mMusicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)service;
            //get service
            mMusicService = binder.getService();
            //pass list
            if (mMusicService.isPlaying() == MediaPlayerService.tPlayerState.idle) {
                mMusicService.setSongs(mTrackUri, mPosition);
                mMusicService.playSong();
                mPlay.setImageResource(mPauseDrawable);
            }
            mMusicService.setViews(mAlbumText, mTrackText, mArtistText, mImageView);
            mMusicService.setSeekBar(mSeekBar);
            mMusicService.updateViews();
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
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
        if (mMusicService != null) {
            mMusicService.setSongs(mTrackUri, mPosition);
        }

        mAlbumText = (TextView) mRootView.findViewById(R.id.player_album_name);
        mTrackText = (TextView) mRootView.findViewById(R.id.player_track_name);
        mArtistText = (TextView) mRootView.findViewById(R.id.player_artist_name);
        mImageView = (ImageView) mRootView.findViewById(R.id.player_track_image);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.player_progress_bar);
        mNext = (ImageButton) mRootView.findViewById(R.id.player_next_button);
        mPrev = (ImageButton) mRootView.findViewById(R.id.player_back_button);
        mPlay = (ImageButton) mRootView.findViewById(R.id.player_play_pause_button);
        mPauseDrawable = getResources().getIdentifier("@android:drawable/ic_media_pause", null, null);
        mPlayDrawable = getResources().getIdentifier("@android:drawable/ic_media_play", null, null);

        if ((mNext == null) || (mPrev == null) || (mPlay == null)) {
            // If any of these are null, then something is wrong
            Log.e(LOG_TAG, "ERROR: One or more button widgets were not found!");
        } else {
            mNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMusicService.nextSong();
                    mMusicService.updateViews();
                }
            });
            mPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMusicService.prevSong();
                    mMusicService.updateViews();
                }
            });
            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMusicService.pausePlay();
                    if (mMusicService.isPlaying() == MediaPlayerService.tPlayerState.playing) {
                        mPlay.setImageResource(mPauseDrawable);
                    } else {
                        mPlay.setImageResource(mPlayDrawable);
                    }

                    mMusicService.setSeekBar(mSeekBar);
                    mMusicService.updateViews();
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
                mMusicService.seekTo(songPosition);
            }
        });

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(mActivity, MediaPlayerService.class);
            mActivity.bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMusicService != null) {
            mMusicService.setSeekBar(null);
        }
        mActivity.unbindService(mMusicConnection);
    }
}

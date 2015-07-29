package com.markesilva.spotifystreamer;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
public class PreviewPlayerActivityFragment extends Fragment
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = PreviewPlayerActivityFragment.class.getSimpleName();
    private static final String POSITION_KEY = "position";
    private Cursor mCursor;
    private int mPosition;
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
    private MediaPlayer mMediaPlayer;
    private MediaObserver mMediaObserver;
    private enum tPlayState {
        idle,
        playing,
        preparing,
        paused,
    }
    tPlayState mPlayState;

    private static final String[] TRACK_COLUMNS = {
            SpotifyContract.TrackEntry.COLUMN_PREVIEW_URL,
            SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME,
            SpotifyContract.TrackEntry.COLUMN_IMAGE_URL,
            SpotifyContract.TrackEntry.COLUMN_TRACK_NAME,
            SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME,
            SpotifyContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID,
    };

    // These indices are ties to ARTIST_COLUMNS. If that changes, these must change too
    static final int COL_TRACK_PREVIEW_URL = 0;
    static final int COL_TRACK_ALBUM_NAME = 1;
    static final int COL_TRACK_IMAGE_URL = 2;
    static final int COL_TRACK_NAME = 3;
    static final int COL_ARTIST_NAME = 4;
    static final int COL_ARTIST_SPOTIFY_ID = 5;

    public PreviewPlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.fragment_preview_player, container, false);

        Uri u = getActivity().getIntent().getParcelableExtra(PreviewPlayerActivity.TRACK_URI_KEY);
        // We check to see if a cursor position has been saves in the instance data
        if ((savedInstanceState == null) || (savedInstanceState.getInt(POSITION_KEY, -1) == -1)) {
            mPosition = getActivity().getIntent().getIntExtra(PreviewPlayerActivity.ROW_NUM_KEY, -1);
        } else {
            mPosition = savedInstanceState.getInt(POSITION_KEY, -1);
        }

        if (u != null) {
            mCursor = getActivity().getContentResolver().query(u, TRACK_COLUMNS, null, null, null);
            if (mPosition != -1) {
                mCursor.moveToPosition(mPosition);
            }
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
                    if (!mCursor.moveToNext()) {
                        mCursor.moveToFirst();
                    }
                    mPosition = mCursor.getPosition();
                    updateViews();
                    if (mPlayState == tPlayState.playing) {
                        playSong();
                    }
                }
            });
            mPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mCursor.moveToPrevious()) {
                        mCursor.moveToLast();
                    }
                    mPosition = mCursor.getPosition();
                    updateViews();
                    if (mPlayState == tPlayState.playing) {
                        playSong();
                    }
                }
            });
            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (mPlayState) {
                        case idle:
                        case preparing:
                            playSong();
                            break;
                        case playing:
                            mPlayState = tPlayState.paused;
                            mMediaPlayer.pause();
                            mPlay.setImageResource(mPauseDrawable);
                            break;
                        case paused:
                            mPlayState = tPlayState.playing;
                            mMediaPlayer.start();
                            mPlay.setImageResource(mPlayDrawable);
                            break;

                    }
                }
            });
        }

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int songPosition = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayState == tPlayState.playing) {
                    songPosition = progress;
                } else {
                    seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayState == tPlayState.playing) {
                    mMediaPlayer.seekTo(songPosition);
                }
            }
        });

        mPlayDrawable = getResources().getIdentifier("@android:drawable/ic_media_play", null, null);
        mPauseDrawable = getResources().getIdentifier("@android:drawable/ic_media_pause", null, null);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mPlayState = tPlayState.idle;
        updateViews();
        playSong();
        return mRootView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putInt(POSITION_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    private void playSong() {
        String previewUrl = mCursor.getString(COL_TRACK_PREVIEW_URL);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(previewUrl);
            mMediaPlayer.prepareAsync();
            mPlayState = tPlayState.preparing;
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Preview could not be loaded", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mSeekBar.setMax(mp.getDuration());
        mMediaObserver = new MediaObserver();
        mp.start();
        (new Thread(mMediaObserver)).start();
        mPlayState = tPlayState.playing;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.stop();
        mSeekBar.setProgress(mp.getCurrentPosition());
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        if (mMediaObserver != null) {
            mMediaObserver.stop();
        }
        releaseMediaPlayer();
        if (mCursor != null) {
            mCursor.close();
        }
        super.onDestroy();
    }

    private void updateViews() {
        if (mPosition != -1) {
            mAlbumText.setText(mCursor.getString(COL_TRACK_ALBUM_NAME));
            mTrackText.setText(mCursor.getString(COL_TRACK_NAME));
            mArtistText.setText(mCursor.getString(COL_ARTIST_NAME));
            String image_url = mCursor.getString(COL_TRACK_IMAGE_URL);
            if (image_url.trim().equals("")) {
                Picasso.with(getActivity()).load(R.drawable.default_image).error(R.drawable.image_download_error).into(mImageView);
            } else {
                Picasso.with(getActivity()).load(image_url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(mImageView);
            }
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                if (mMediaPlayer != null) {
                    mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }
        }
    }
}

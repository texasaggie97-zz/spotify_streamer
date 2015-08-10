package com.markesilva.spotifystreamer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.markesilva.spotifystreamer.data.SpotifyContract;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by marke on 7/29/2015.
 *
 * Background music
 */
public class MediaPlayerService extends IntentService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    // Incoming Intent actions and values
    public static final String ACTION_PLAY = "com.markesilva.spotifystreamer.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.markesilva.spotifystreamer.NEXT";
    public static final String ACTION_PREV = "com.markesilva.spotifystreamer.PREVIOUS";
    public static final String ACTION_UPDATE_POSITION = "com.markesilva.spotifystreamer.UPDATE_POSITION";
    public static final String ACTION_UPDATE_POSITION_POSITION = "position";
    public static final String ACTION_UPDATE_TRACK_LIST = "com.markesilva.spotifystreamer.UPDATE_TRACK_LIST";
    public static final String ACTION_UPDATE_TRACK_LIST_URI = "uri";
    public static final String ACTION_UPDATE_TRACK_LIST_POSITION = "position";

    public static final String ACTION_SEND_UPDATES = "com.markesilva.spotifystreamer.SEND_UPDATES";

    public static final String ACTION_RESET = "com.markesilva.spotifystreamer.RESET";

    // Broadcasts
    public static final String BROADCAST_SONG_UPDATED = "com.markesilva.spotifystreamer.SONG_UPDATED";
    public static final String BROADCAST_SONG_UPDATED_ALBUM = "album";
    public static final String BROADCAST_SONG_UPDATED_TRACK = "track";
    public static final String BROADCAST_SONG_UPDATED_ARTIST = "artist";
    public static final String BROADCAST_SONG_UPDATED_IMAGE_URL = "image_url";
    public static final String BROADCAST_SONG_UPDATED_THUMBNAIL_URL = "thumnail_url";
    public static final String BROADCAST_SONG_UPDATED_POSITION = "position";

    public static final String BROADCAST_DURATION_UPDATED = "com.markesilva.spotifystreamer.DURATION_UPDATED";
    public static final String BROADCAST_DURATION_UPDATED_DURATION = "duration";

    public static final String BROADCAST_POSITION_UPDATED = "com.markesilva.spotifystreamer.POSITION_UPDATED";
    public static final String BROADCAST_POSITION_UPDATED_POSITION = "position";

    public static final String BROADCAST_STATE_UPDATED = "com.markesilva.spotifystreamer.STATE_UPDATED";
    public static final String BROADCAST_STATE_UPDATED_STATE = "state";
    // These indices are ties to ARTIST_COLUMNS. If that changes, these must change too
    static final int COL_TRACK_PREVIEW_URL = 0;
    static final int COL_TRACK_ALBUM_NAME = 1;
    static final int COL_TRACK_IMAGE_URL = 2;
    static final int COL_TRACK_THUMBNAIL_URL = 3;
    static final int COL_TRACK_NAME = 4;
    static final int COL_ARTIST_NAME = 5;
    // Private or override items
    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();
    private static final String[] TRACK_COLUMNS = {
            SpotifyContract.TrackEntry.COLUMN_PREVIEW_URL,
            SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME,
            SpotifyContract.TrackEntry.COLUMN_IMAGE_URL,
            SpotifyContract.TrackEntry.COLUMN_THUMBNAIL_URL,
            SpotifyContract.TrackEntry.COLUMN_TRACK_NAME,
            SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME,
    };
    private final IBinder mMusicBind = new MusicBinder();
    tPlayerState mPlayerState = tPlayerState.idle;
    private MediaPlayer mMediaPlayer;
    private Uri mSearchUri;
    private Cursor mCursor;
    private int mPosition;
    private MediaObserver mMediaObserver;
    public MediaPlayerService(String name) {
        super(name);
    }

    public MediaPlayerService() {
        super("MediaPlayerService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Intent: " + intent);
        String action = intent.getAction();
        if (action != null) {
            switch(action) {
                case ACTION_NEXT:
                    nextSong();
                    break;
                case ACTION_PREV:
                    prevSong();
                    break;
                case ACTION_PLAY:
                    pausePlay();
                    break;
                case ACTION_UPDATE_POSITION:
                    int pos = intent.getIntExtra(ACTION_UPDATE_POSITION_POSITION, -1);
                    if (pos != -1) {
                        seekTo(pos);
                    }
                    break;
                case ACTION_UPDATE_TRACK_LIST:
                    int song = intent.getIntExtra(ACTION_UPDATE_TRACK_LIST_POSITION, -1);
                    Uri uri = intent.getParcelableExtra(ACTION_UPDATE_TRACK_LIST_URI);
                    setSongs(uri, song);
                    break;
                case ACTION_SEND_UPDATES:
                    sendUpdates();
                    break;
                case ACTION_RESET:
                    reset();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid action" + action);
            }
        }
    }

    private void setSongs(Uri u, int pos) {
        Log.d(LOG_TAG, "setSongs");
        // Nothing to do
        if ((u == null) || (pos == -1)) {
            sendUpdates();
            return;
        }

        boolean restartSong = false;
        if ((mSearchUri == null) || !mSearchUri.equals(u)) {
            mSearchUri = u;
            mCursor = getContentResolver().query(mSearchUri, TRACK_COLUMNS, null, null, null);
            restartSong = true;
        }
        if (mPosition != pos) {
            mPosition = pos;
            mCursor.moveToPosition(mPosition);
            restartSong = true;
        }
        if (restartSong) {
            playSong();
        } else {
            sendUpdates();
        }
    }

    private void playSong() {
        Log.d(LOG_TAG, "playSong");
        String previewUrl = mCursor.getString(COL_TRACK_PREVIEW_URL);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(previewUrl);
            mMediaPlayer.prepareAsync();
            mPlayerState = tPlayerState.preparing;
            sendSongUpdate();
            sendStateUpdate();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Preview could not be loaded", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void seekTo(int p) {
        Log.d(LOG_TAG, "seekTo");
        mMediaPlayer.seekTo(p);
        sendPositionUpdate();
    }

    private void nextSong() {
        Log.d(LOG_TAG, "nextSong");
        if (!mCursor.moveToNext()) {
            mCursor.moveToFirst();
        }
        mPosition = mCursor.getPosition();
        if (mPlayerState == tPlayerState.playing) {
            playSong();
        }
    }

    private void prevSong() {
        Log.d(LOG_TAG, "prevSong");
        if (!mCursor.moveToPrevious()) {
            mCursor.moveToLast();
        }
        mPosition = mCursor.getPosition();
        if (mPlayerState == tPlayerState.playing) {
            playSong();
        }
    }

    private void pausePlay() {
        Log.d(LOG_TAG, "pausePlay");
        if (mPlayerState == tPlayerState.playing) {
            mMediaPlayer.pause();
            mPlayerState = tPlayerState.paused;
            if (mMediaObserver != null) {
                mMediaObserver.stop();
                mMediaObserver = null;
            }
        } else {
            mMediaPlayer.start();
            mPlayerState = tPlayerState.playing;
            mMediaObserver = new MediaObserver();
            (new Thread(mMediaObserver)).start();
        }
        sendStateUpdate();
    }

    private void reset() {
        if (mPlayerState != tPlayerState.idle) {
            mPlayerState = tPlayerState.idle;
            mMediaPlayer.reset();
        }
    }

    public tPlayerState isPlaying() {
        Log.d(LOG_TAG, "isPlaying");
        return mPlayerState;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
        if (!mCursor.moveToNext()) {
            mCursor.moveToFirst();
        }
        mPosition = mCursor.getPosition();
        if (mPlayerState == tPlayerState.playing) {
            playSong();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(LOG_TAG, "OnError - Error code: " + what + " Extra code: " + extra);

        switch (what) {
            case -1004:
                Log.d(LOG_TAG, "MEDIA_ERROR_IO");
                break;
            case -1007:
                Log.d(LOG_TAG, "MEDIA_ERROR_MALFORMED");
                break;
            case 200:
                Log.d(LOG_TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                break;
            case 100:
                Log.d(LOG_TAG, "MEDIA_ERROR_SERVER_DIED");
                break;
            case -110:
                Log.d(LOG_TAG, "MEDIA_ERROR_TIMED_OUT");
                break;
            case 1:
                Log.d(LOG_TAG, "MEDIA_ERROR_UNKNOWN");
                break;
            case -1010:
                Log.d(LOG_TAG, "MEDIA_ERROR_UNSUPPORTED");
                break;
            default:
                Log.d(LOG_TAG, "UNKNOWN WHAT");
                break;
        }

        switch (extra) {
            case 800:
                Log.d(LOG_TAG, "MEDIA_INFO_BAD_INTERLEAVING");
                break;
            case 702:
                Log.d(LOG_TAG, "MEDIA_INFO_BUFFERING_END");
                break;
            case 701:
                Log.d(LOG_TAG, "MEDIA_INFO_METADATA_UPDATE");
                break;
            case 802:
                Log.d(LOG_TAG, "MEDIA_INFO_METADATA_UPDATE");
                break;
            case 801:
                Log.d(LOG_TAG, "MEDIA_INFO_NOT_SEEKABLE");
                break;
            case 1:
                Log.d(LOG_TAG, "MEDIA_INFO_UNKNOWN");
                break;
            case 3:
                Log.d(LOG_TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
                break;
            case 700:
                Log.d(LOG_TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING");
                break;
            default:
                Log.d(LOG_TAG, "UNKNOWN EXTRA");
                break;
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        mPlayerState = tPlayerState.playing;
        sendStateUpdate();
        sendDurationUpdate();
        mp.start();
        mMediaObserver = new MediaObserver();
        (new Thread(mMediaObserver)).start();
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (mMediaObserver != null) {
            mMediaObserver.stop();
            mMediaObserver = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (m != null) {
            m.cancelAll();
        }
        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void initMusicPlayer() {
        Log.d(LOG_TAG, "initMusicPlayer");
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    private void sendSongUpdate() {
        Log.d(LOG_TAG, "sendSongUpdate");
        if (mCursor != null) {
            Intent intent = new Intent(BROADCAST_SONG_UPDATED);

            // Add the data
            intent.putExtra(BROADCAST_SONG_UPDATED_TRACK, mCursor.getString(COL_TRACK_NAME));
            intent.putExtra(BROADCAST_SONG_UPDATED_ALBUM, mCursor.getString(COL_TRACK_ALBUM_NAME));
            intent.putExtra(BROADCAST_SONG_UPDATED_ARTIST, mCursor.getString(COL_ARTIST_NAME));
            intent.putExtra(BROADCAST_SONG_UPDATED_IMAGE_URL, mCursor.getString(COL_TRACK_IMAGE_URL));
            intent.putExtra(BROADCAST_SONG_UPDATED_THUMBNAIL_URL, mCursor.getString(COL_TRACK_THUMBNAIL_URL));

            // Put the cursor position into the broadcast so clients can keep track where we are in the search results
            intent.putExtra(BROADCAST_SONG_UPDATED_POSITION, mPosition);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void sendStateUpdate() {
        Log.d(LOG_TAG, "sendStateUpdate");
        Intent intent = new Intent(BROADCAST_STATE_UPDATED);

        // Add the data
        intent.putExtra(BROADCAST_STATE_UPDATED_STATE, mPlayerState);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendPositionUpdate() {
        // Too noisy
        // Log.d(LOG_TAG, "sendPositionUpdate");
        if (mPlayerState != tPlayerState.idle) {
            Intent intent = new Intent(BROADCAST_POSITION_UPDATED);

            // Add the data
            intent.putExtra(BROADCAST_POSITION_UPDATED_POSITION, mMediaPlayer.getCurrentPosition());

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void sendDurationUpdate() {
        Log.d(LOG_TAG, "sendDurationUpdate");
        if (mPlayerState != tPlayerState.idle) {
            Intent intent = new Intent(BROADCAST_DURATION_UPDATED);

            // Add the data
            intent.putExtra(BROADCAST_DURATION_UPDATED_DURATION, mMediaPlayer.getDuration());

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void sendUpdates() {
        sendStateUpdate();
        sendDurationUpdate();
        sendSongUpdate();
    }

    public enum tPlayerState {
        idle,
        preparing,
        playing,
        paused,
    }

    public class MusicBinder extends Binder {
        public MediaPlayerService getService() {
            Log.d(LOG_TAG, "getService");
            return MediaPlayerService.this;
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
                if ((mMediaPlayer != null) && (mPlayerState == tPlayerState.playing)) {
                    sendPositionUpdate();
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

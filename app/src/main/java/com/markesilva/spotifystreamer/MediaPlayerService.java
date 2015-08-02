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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.markesilva.spotifystreamer.data.SpotifyContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by marke on 7/29/2015.
 *
 * Background music
 */
public class MediaPlayerService extends IntentService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();
    private MediaPlayer mMediaPlayer;

    // These are the views that we may need to update in the player
    private SeekBar mPlayerSeekBar;
    private TextView mPlayerAlbum;
    private TextView mPlayerTrack;
    private TextView mPlayerArtist;
    private ImageView mPlayerImage;
    private ImageButton mPlayerPlayButton;

    // Intents from notification
    public static final String ACTION_PLAY = "com.markesilva.spotifystreamer.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.markesilva.spotifystreamer.NEXT";
    public static final String ACTION_PREV = "com.markesilva.spotifystreamer.PREVIOUS";

    private Uri mSearchUri;
    private Cursor mCursor;
    private int mPosition;
    private MediaObserver mMediaObserver;
    private final IBinder mMusicBind = new MusicBinder();
    public enum tPlayerState {
        idle,
        preparing,
        playing,
        paused,
    }
    tPlayerState mPlayerState = tPlayerState.idle;

    private static final String[] TRACK_COLUMNS = {
            SpotifyContract.TrackEntry.COLUMN_PREVIEW_URL,
            SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME,
            SpotifyContract.TrackEntry.COLUMN_IMAGE_URL,
            SpotifyContract.TrackEntry.COLUMN_TRACK_NAME,
            SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME,
    };

    // These indices are ties to ARTIST_COLUMNS. If that changes, these must change too
    static final int COL_TRACK_PREVIEW_URL = 0;
    static final int COL_TRACK_ALBUM_NAME = 1;
    static final int COL_TRACK_IMAGE_URL = 2;
    static final int COL_TRACK_NAME = 3;
    static final int COL_ARTIST_NAME = 4;

    public MediaPlayerService(String name) {
        super(name);
    }

    public MediaPlayerService() {
        super("MediaPlayerService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "");
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
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
                    updateNotificationViews();
                    break;
                case ACTION_PREV:
                    prevSong();
                    updateNotificationViews();
                    break;
                case ACTION_PLAY:
                    pausePlay();
                    updateNotificationViews();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid action" + action);
            }
        }
    }

    public void playSong() {
        Log.d(LOG_TAG, "playSong");
        String previewUrl = mCursor.getString(COL_TRACK_PREVIEW_URL);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(previewUrl);
            mMediaPlayer.prepareAsync();
            mPlayerState = tPlayerState.preparing;
            updateViews();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Preview could not be loaded", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void setSong(int s) {
        Log.d(LOG_TAG, "setSong");
        mPosition = s;
        mCursor.moveToPosition(mPosition);
        playSong();
    }

    public void pause() {
        Log.d(LOG_TAG, "pause");
        mMediaPlayer.pause();
    }

    public void start() {
        Log.d(LOG_TAG, "start");
        mMediaPlayer.start();
    }

    public void seekTo(int p) {
        Log.d(LOG_TAG, "seekTo");
        mMediaPlayer.seekTo(p);
    }

    public int getCurrentPosition() {
        Log.d(LOG_TAG, "getCurrentPosition");
        return mMediaPlayer.getCurrentPosition();
    }

    public void nextSong() {
        Log.d(LOG_TAG, "nextSong");
        if (!mCursor.moveToNext()) {
            mCursor.moveToFirst();
        }
        mPosition = mCursor.getPosition();
        if (mPlayerState == tPlayerState.playing) {
            playSong();
        }
    }

    public void prevSong() {
        Log.d(LOG_TAG, "prevSong");
        if (!mCursor.moveToPrevious()) {
            mCursor.moveToLast();
        }
        mPosition = mCursor.getPosition();
        if (mPlayerState == tPlayerState.playing) {
            playSong();
        }
    }

    public void pausePlay() {
        Log.d(LOG_TAG, "pausePlay");
        if (mPlayerState == tPlayerState.playing) {
            mMediaPlayer.pause();
            mPlayerState = tPlayerState.paused;
        } else {
            mMediaPlayer.start();
            mPlayerState = tPlayerState.playing;
        }
    }

    public void reset() {
        if (mPlayerState != tPlayerState.idle) {
            mPlayerState = tPlayerState.idle;
            mMediaPlayer.reset();
        }
    }

    public tPlayerState isPlaying() {
        Log.d(LOG_TAG, "isPlaying");
        return mPlayerState;
    }

    public void setPlayerViews(TextView album, TextView track, TextView artist, ImageView image, SeekBar seekBar, ImageButton play)
    {
        mPlayerAlbum = album;
        mPlayerTrack = track;
        mPlayerArtist = artist;
        mPlayerImage = image;
        mPlayerPlayButton = play;
        setSeekBar(seekBar);
    }

    // Setting the SeekBar gets it's own function since there is more to it
    public void setSeekBar(SeekBar seekBar) {
        Log.d(LOG_TAG, "setSeekBar");
        if (seekBar == null) {
            if (mMediaObserver != null) {
                mMediaObserver.stop();
                mMediaObserver = null;
            }
        } else {
            mPlayerSeekBar = seekBar;
            if ((mPlayerState != tPlayerState.preparing) && (mPlayerState != tPlayerState.idle)) {
                mPlayerSeekBar.setMax(mMediaPlayer.getDuration());
            }
            mMediaObserver = new MediaObserver();
            (new Thread(mMediaObserver)).start();
        }
    }

    public void updateViews() {
        Log.d(LOG_TAG, "updateViews");
        if (mPosition != -1) {
            RemoteViews playerView = new RemoteViews(getPackageName(), R.layout.fragment_preview_player);
            // These are the player views
            if (mPlayerAlbum != null) {
                mPlayerAlbum.setText(mCursor.getString(COL_TRACK_ALBUM_NAME));
            }
            if (mPlayerTrack != null) {
                mPlayerTrack.setText(mCursor.getString(COL_TRACK_NAME));
            }
            if (mPlayerArtist != null) {
                 mPlayerArtist.setText(mCursor.getString(COL_ARTIST_NAME));
            }
            if (mPlayerImage != null) {
                String image_url = mCursor.getString(COL_TRACK_IMAGE_URL);
                if (image_url.trim().equals("")) {
                    Picasso.with(this).load(R.drawable.default_image).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(mPlayerImage);
                } else {
                    Picasso.with(this).load(image_url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(mPlayerImage);
                }
            }
            if (mPlayerPlayButton != null) {
                if ((mPlayerState == tPlayerState.playing) || (mPlayerState == tPlayerState.preparing)) {
                    mPlayerPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
                } else {
                    mPlayerPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                }
            }
        }
        updateNotificationViews();
    }

    public void updateNotificationViews() {
        Log.d(LOG_TAG, "updateNotificationViews");
        if (mPosition != -1) {
            // Notification update
            RemoteViews notificationViews = new RemoteViews(getPackageName(), R.layout.notification);
            notificationViews.setTextViewText(R.id.notification_track, mCursor.getString(COL_TRACK_NAME));
            String image_url = mCursor.getString(COL_TRACK_IMAGE_URL);
            if (image_url.trim().equals("")) {
                Picasso.with(this).load(R.drawable.default_image).into(notificationViews, R.id.notification_thumbnail, new int[] {MainActivity.NOTIFICATION_ID});
            } else {
                Picasso.with(this).load(image_url).into(notificationViews, R.id.notification_thumbnail, new int[] {MainActivity.NOTIFICATION_ID});
            }
            if ((mPlayerState == tPlayerState.playing) || (mPlayerState == tPlayerState.preparing)) {
                notificationViews.setImageViewResource(R.id.notification_play_pause_button, R.drawable.ic_pause_black_24dp);
            } else {
                notificationViews.setImageViewResource(R.id.notification_play_pause_button, R.drawable.ic_play_arrow_black_24dp);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContent(notificationViews);

            NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            m.notify(MainActivity.NOTIFICATION_ID, mBuilder.build());
        }
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
    public boolean onError(MediaPlayer mp,int what, int extra){
            Log.d(LOG_TAG, "OnError - Error code: " + what + " Extra code: " + extra);

        switch(what){
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

        switch(extra) {
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
    public void onPrepared (MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        if (mPlayerSeekBar != null) {
            mPlayerSeekBar.setMax(mMediaPlayer.getDuration());
        }
        mPlayerState = tPlayerState.playing;
        mp.start();
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

    public void initMusicPlayer() {
        Log.d(LOG_TAG, "initMusicPlayer");
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setSongs(Uri u, int pos) {
        Log.d(LOG_TAG, "setSongs");
        mSearchUri = u;
        mCursor = getContentResolver().query(mSearchUri, TRACK_COLUMNS, null, null, null);
        mPosition = pos;
        mCursor.moveToPosition(mPosition);
    }

    public class MusicBinder extends Binder {
        MediaPlayerService getService() {
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
                    if (mPlayerSeekBar != null) {
                        mPlayerSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    }
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

package com.markesilva.spotifystreamer.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.markesilva.spotifystreamer.MainActivity;
import com.markesilva.spotifystreamer.MediaPlayerService;
import com.markesilva.spotifystreamer.PreviewPlayerActivity;
import com.markesilva.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by marke on 8/2/2015.
 *
 * Helper class for creating and updating the notification
 */
public class NotificationHelper {
    private static final String LOG_TAG = NotificationHelper.class.getSimpleName();
    MainActivity mActivity;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 100;

    public NotificationHelper(MainActivity a) {
        Log.d(LOG_TAG, "NotificationHelper");
        mActivity = a;
    }

    public void configureNotification() {
        Log.d(LOG_TAG, "configureNotification");
        RemoteViews remoteViews = new RemoteViews(mActivity.getPackageName(), R.layout.notification);
        mBuilder = new NotificationCompat.Builder(mActivity).setSmallIcon(R.mipmap.ic_launcher).setContent(remoteViews);

        // Create intent to launch player activity
        Intent resultIntent = new Intent(mActivity, PreviewPlayerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mActivity);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification, resultPendingIntent);

        resultIntent = new Intent(MediaPlayerService.ACTION_PLAY);
        resultPendingIntent = PendingIntent.getService(mActivity.getApplicationContext(), 100, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_play_pause_button, resultPendingIntent);

        resultIntent = new Intent(MediaPlayerService.ACTION_NEXT);
        resultPendingIntent = PendingIntent.getService(mActivity.getApplicationContext(), 101, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_next_button, resultPendingIntent);

        resultIntent = new Intent(MediaPlayerService.ACTION_PREV);
        resultPendingIntent = PendingIntent.getService(mActivity.getApplicationContext(), 102, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_back_button, resultPendingIntent);

        mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    // This function should not run on the main thread
    public void updateNotificationViews(Intent intent) {
        Log.d(LOG_TAG, "updateNotificationViews");
        // Notification update
        RemoteViews remoteViews = new RemoteViews(mActivity.getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.notification_track, intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_TRACK));
        String image_url = intent.getStringExtra(MediaPlayerService.BROADCAST_SONG_UPDATED_THUMBNAIL_URL);

        if (image_url.trim().equals("")) {
            remoteViews.setImageViewResource(R.id.notification_thumbnail, R.drawable.default_image);
        } else {
            try {
                Bitmap b = Picasso.with(mActivity).load(image_url).get();
                remoteViews.setImageViewBitmap(R.id.notification_thumbnail, b);
            } catch (IOException e) {
                remoteViews.setImageViewResource(R.id.notification_thumbnail, R.drawable.default_image);
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }

        mBuilder.setContent(remoteViews);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void updatePlayButton(Intent intent) {
        Log.d(LOG_TAG, "updatePlayButton");

        RemoteViews remoteViews = new RemoteViews(mActivity.getPackageName(), R.layout.notification);
        MediaPlayerService.tPlayerState playerState = (MediaPlayerService.tPlayerState) intent.getSerializableExtra(MediaPlayerService.BROADCAST_STATE_UPDATED_STATE);
        if ((playerState == MediaPlayerService.tPlayerState.playing) || (playerState == MediaPlayerService.tPlayerState.preparing)) {
            remoteViews.setImageViewResource(R.id.notification_play_pause_button, R.drawable.ic_pause_black_24dp);
        } else {
            remoteViews.setImageViewResource(R.id.notification_play_pause_button, R.drawable.ic_play_arrow_black_24dp);
        }

        mBuilder.setContent(remoteViews);
        mNotificationManager.notify(NotificationHelper.NOTIFICATION_ID, mBuilder.build());
    }
}

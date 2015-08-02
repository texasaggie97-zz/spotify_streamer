package com.markesilva.spotifystreamer.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.markesilva.spotifystreamer.MainActivity;
import com.markesilva.spotifystreamer.MediaPlayerService;
import com.markesilva.spotifystreamer.PreviewPlayerActivity;
import com.markesilva.spotifystreamer.R;

/**
 * Created by marke on 8/2/2015.
 *
 * Helper class for creating and updating the notification
 */
public class NotificationHelper {
    private static final String LOG_TAG = NotificationHelper.class.getSimpleName();
    MainActivity mActivity;
    public static final int NOTIFICATION_ID = 100;

    public void configureNotification(MainActivity a) {
        mActivity = a;
        RemoteViews remoteViews = new RemoteViews(mActivity.getPackageName(), R.layout.notification);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mActivity).setSmallIcon(R.mipmap.ic_launcher).setContent(remoteViews);

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

        NotificationManager mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}

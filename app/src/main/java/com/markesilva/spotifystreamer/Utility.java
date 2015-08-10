package com.markesilva.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by marke on 8/7/2015.
 *
 * Helper functions
 */
public class Utility {
    public static String getPreferredLocale(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Country needs to be upper case. We change the display to show uppercase here.
        return prefs.getString(context.getString(R.string.pref_spotify_locale_key), context.getString(R.string.pref_spotify_locale_default)).toUpperCase();
    }

    public static boolean getPreferredNotificationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_notification_key), true);
    }
}

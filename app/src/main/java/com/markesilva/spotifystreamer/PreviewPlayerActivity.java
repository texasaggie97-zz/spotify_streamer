package com.markesilva.spotifystreamer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.markesilva.spotifystreamer.utils.LogUtils;
import com.markesilva.spotifystreamer.utils.NotificationHelper;

public class PreviewPlayerActivity extends AppCompatActivity {
    public final static String LOG_TAG = LogUtils.makeLogTag(PreviewPlayerActivity.class);
    public final static String TRACK_URI_KEY = "track_uri";
    public final static String ROW_NUM_KEY = "row_number";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // We have different themes depending on orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setTheme(R.style.LandDialogTheme);
        } else {
            setTheme(R.style.PortraitDialogTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_player);

        NotificationHelper.configureNotification(Utility.getPreferredNotificationEnabled(this));
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

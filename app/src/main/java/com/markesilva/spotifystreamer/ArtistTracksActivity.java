package com.markesilva.spotifystreamer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ArtistTracksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_tracks);

        if (savedInstanceState == null) {
            // Create the fragment and add it

            Bundle args = new Bundle();
            args.putString(ArtistTracksActivityFragment.ARTIST_NAME, getIntent().getStringExtra(ArtistTracksActivityFragment.ARTIST_NAME));
            args.putString(ArtistTracksActivityFragment.ARTIST_ID, getIntent().getStringExtra(ArtistTracksActivityFragment.ARTIST_ID));

            ArtistTracksActivityFragment frag = new ArtistTracksActivityFragment();
            frag.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_tracks_container, frag)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_tracks, menu);
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
}

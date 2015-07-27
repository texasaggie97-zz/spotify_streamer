package com.markesilva.spotifystreamer;

import android.content.ContentValues;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.markesilva.spotifystreamer.data.SpotifyContract;
import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class PreviewPlayerActivityFragment extends Fragment {

    public PreviewPlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_preview_player, container, false);

        ContentValues c = getActivity().getIntent().getParcelableExtra(PreviewPlayerActivity.INTENT_KEY);
        if (c != null) {
            ((TextView) rootView.findViewById(R.id.player_album_name)).setText(c.getAsString(SpotifyContract.TrackEntry.COLUMN_ALBUM_NAME));
            ((TextView) rootView.findViewById(R.id.player_track_name)).setText(c.getAsString(SpotifyContract.TrackEntry.COLUMN_TRACK_NAME));
            ((TextView) rootView.findViewById(R.id.player_artist_name)).setText(c.getAsString(SpotifyContract.ArtistEntry.COLUMN_ARTIST_NAME));
            String image_url = c.getAsString(SpotifyContract.TrackEntry.COLUMN_IMAGE_URL);
            ImageView iv = (ImageView) rootView.findViewById(R.id.player_track_image);
            if (image_url.trim().equals("")) {
                Picasso.with(getActivity()).load(R.drawable.default_image).error(R.drawable.image_download_error).into(iv);
            } else {
                Picasso.with(getActivity()).load(image_url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(iv);
            }
        }

        return rootView;
    }
}

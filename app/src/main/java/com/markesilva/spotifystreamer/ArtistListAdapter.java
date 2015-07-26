package com.markesilva.spotifystreamer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.markesilva.spotifystreamer.data.SpotifyContract;
import com.squareup.picasso.Picasso;


/**
 * Created by Mark on 6/16/2015.
 * listview adapter artist search results
 */
public class ArtistListAdapter extends CursorAdapter {
    private Context mContext = null;

    public ArtistListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    // private view holder class
    private class ViewHolder {
        ImageView imageView;
        TextView txtName;
    }

    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.artist_listitem, parent, false);
        ViewHolder holder;
        holder = new ViewHolder();
        holder.txtName = (TextView) view.findViewById(R.id.artist_listitem_artist);
        holder.imageView = (ImageView) view.findViewById(R.id.artist_listitem_image);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder;

        holder = (ViewHolder) view.getTag();
        String thumbnail_url = cursor.getString(MainActivityFragment.COL_ARTIST_THUMBNAIL_URL);
        if (thumbnail_url.trim().equals("")) {
            Picasso.with(context).load(R.drawable.default_image).error(R.drawable.image_download_error).into(holder.imageView);
        } else {
            Picasso.with(context).load(thumbnail_url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(holder.imageView);
        }
        holder.txtName.setText(cursor.getString(MainActivityFragment.COL_ARTIST_NAME));
    }
}

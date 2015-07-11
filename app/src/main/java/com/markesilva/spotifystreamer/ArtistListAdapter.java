package com.markesilva.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Mark on 6/16/2015.
 * listview adapter artist search results
 */
public class ArtistListAdapter extends BaseAdapter {
    private List<ArtistListRow> mArtistList = null;
    private Context mContext = null;

    public ArtistListAdapter(Context c, List<ArtistListRow> info) {
        mContext = c;
        mArtistList = info;
    }

    // private view holder class
    private class ViewHolder {
        ImageView imageView;
        TextView txtName;
    }

    public void setList(List<ArtistListRow> info) {
        mArtistList = info;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (mArtistList != null) {
            return mArtistList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int index) {
        return mArtistList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return mArtistList.indexOf(getItem(index));
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artist_listitem, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.artist_listitem_artist);
            holder.imageView = (ImageView) convertView.findViewById(R.id.artist_listitem_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ArtistListRow rowItem = (ArtistListRow) getItem(index);

        if (rowItem.getThumbnail() != null) {
            Picasso.with(mContext).load(rowItem.getThumbnail().url).placeholder(R.drawable.default_image).error(R.drawable.image_download_error).into(holder.imageView);
        }
        holder.txtName.setText(rowItem.getName());

        return convertView;
    }
}

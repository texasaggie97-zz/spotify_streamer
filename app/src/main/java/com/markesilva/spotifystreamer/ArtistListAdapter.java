package com.markesilva.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mark on 6/16/2015.
 */
public class ArtistListAdapter extends BaseAdapter
{
    List<ArtistListRow> mArtistList = null;
    Context mContext = null;

    public ArtistListAdapter(Context c, List<ArtistListRow> info)
    {
        mContext = c;
        mArtistList = info;
    }

    // private view holder class
    private class ViewHolder
    {
        ImageView imageView;
        TextView txtName;
    }

    @Override
    public int getCount()
    {
        // TODO Auto-generated method stub
        if (mArtistList != null)
        {
            return mArtistList.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int index)
    {
        return mArtistList.get(index);
    }

    @Override
    public long getItemId(int index)
    {
        return mArtistList.indexOf(getItem(index));
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent)
    {
        ViewHolder holder = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.artist_listitem, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.artist_listitem_artist);
            holder.imageView = (ImageView) convertView.findViewById(R.id.artist_listitem_image);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        ArtistListRow rowItem = (ArtistListRow) getItem(index);

        if (rowItem.getImageId() != -1)
        {
            holder.imageView.setImageResource(rowItem.getImageId());
        }
        holder.txtName.setText(rowItem.getName());

        return convertView;
    }
}

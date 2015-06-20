package com.markesilva.spotifystreamer;


import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Mark on 6/16/2015.
 * Items required for each artist returned by search
 */
public class ArtistListRow
{
    private String name;
    private String artistId;
    private Image thumbnail;

    public ArtistListRow(List<Image> images, String name, String artistId)
    {
        this.thumbnail = null;
        // store the smallest image
        for (Image i: images)
        {
            if ((thumbnail == null) || (i.height < thumbnail.height))
            {
                this.thumbnail = i;
            }
        }
        this.name = name;
        this.artistId = artistId;
    }

    // Get/Set
    public Image getThumbnail() { return thumbnail; }
    public String getName() { return name; }
    public String getId() { return artistId; }

    @Override
    public String toString()
    {
        return name;
    }
}

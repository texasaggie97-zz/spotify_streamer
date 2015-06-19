package com.markesilva.spotifystreamer;


import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Mark on 6/16/2015.
 */
public class ArtistListRow
{
    private String name;
    private Image image;

    public ArtistListRow(List<Image> images, String name)
    {
        if (images.size() > 0)
        {
            this.image = images.get(0);
        }
        else
        {
            this.image = null;
        }
        this.name = name;
    }

    // Get/Set
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString()
    {
        return name;
    }
}

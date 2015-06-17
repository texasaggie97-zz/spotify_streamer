package com.markesilva.spotifystreamer;

/**
 * Created by Mark on 6/16/2015.
 */
public class ArtistListRow
{
    private String name;
    private int imageId;

    public ArtistListRow(int imageId, String name)
    {
        this.imageId = imageId;
        this.name = name;
    }

    // Get/Set
    public int getImageId() { return imageId; }
    public void setImageIf(int imageId) { this.imageId = imageId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString()
    {
        return name;
    }
}

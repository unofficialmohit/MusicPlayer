package com.mg.music;

import android.net.Uri;

public class AudioFile {
    public String title;
    public String artist;
    public String filePath;
    public long albumId;
    Uri albumArtUri;
    public AudioFile(String title,String artist,String filePath,long albumId,Uri albumArtUri)
    {
        this.title=title;
        this.artist=artist;
        this.filePath=filePath;
        this.albumId=albumId;
        this.albumArtUri=albumArtUri;
    }
    public String getTitle()
    {
        return title;
    }
    public String getArtist()
    {
        return artist;
    }
    public String getFilePath()
    {
        return filePath;
    }
    public long getAlbumId(){ return  albumId;}
    public Uri getAlbumArtUri(){return albumArtUri;}

}

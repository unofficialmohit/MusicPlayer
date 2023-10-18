package com.mg.music;

import android.net.Uri;

public class AudioFile {
    public String title;
    public String artist;
    public String filePath;
    public long albumId;
    public String album;
    public String ModifiedDate;
    public long Duration;
    public String DateAdded;
    Uri albumArtUri;
    public AudioFile(String title,String artist,String filePath,long albumId,Uri albumArtUri,String album,String ModifiedDate,long Duration,String DateAdded)
    {
        this.title=title;
        this.artist=artist;
        this.filePath=filePath;
        this.albumId=albumId;
        this.albumArtUri=albumArtUri;
        this.album=album;
        this.ModifiedDate=ModifiedDate;
        this.Duration=Duration;
        this.DateAdded=DateAdded;
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
    public String getAlbum(){ return album;}
    public long getDuration(){ return  Duration;}
    public String getModifiedDate(){ return ModifiedDate;}
    public String getDateAdded(){return DateAdded;}

}

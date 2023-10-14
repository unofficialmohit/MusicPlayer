package com.mg.music;

public class AudioFile {
    public String title;
    public String artist;
    public String filePath;
    public AudioFile(String title,String artist,String filePath)
    {
        this.title=title;
        this.artist=artist;
        this.filePath=filePath;
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
}

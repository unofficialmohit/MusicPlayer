package com.mg.music;
import android.app.Application;
import android.media.MediaPlayer;

public class MyApplication extends Application {
    public MediaPlayer mediaPlayer;
    public void onCreate(){
        super.onCreate();
        mediaPlayer=new MediaPlayer();
        Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler(this));
    }
    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
}

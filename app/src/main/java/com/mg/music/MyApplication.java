package com.mg.music;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.MediaPlayer;
import android.os.Build;

public class MyApplication extends Application {
    public MediaPlayer mediaPlayer;
    public static final String Channel_ID_1="CHANNEL_1";
    public static final String Channel_ID_2="CHANNEL_2";
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREVIOUS";
    public static final String ACTION_PLAY="PLAY";

    public void onCreate(){
        super.onCreate();
        mediaPlayer=new MediaPlayer();
        createNotificationChannel();
        Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler(this));
    }
    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel(Channel_ID_1,"NOW PLAYING", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("THIS IS USED TO PLAY SONGS");
            notificationChannel.setSound(null, null);
            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

        }
    }
}

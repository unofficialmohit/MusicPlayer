package com.mg.music;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.mg.music.MusicList.pos;
import static com.mg.music.MyApplication.ACTION_NEXT;
import static com.mg.music.MyApplication.ACTION_PLAY;
import static com.mg.music.MyApplication.ACTION_PREV;
import static com.mg.music.MyApplication.Channel_ID_1;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class MyAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
    private Context context;
    private AudioManager audioManager;

    public MyAudioFocusChangeListener(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Resume playback or start playback if paused
                // You can play your audio here
                if (!MusicList.mediaPlayer.isPlaying()) {
                     MusicList.mediaPlayer.start();
                     MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
                     MainActivity.pauseButton.setBackgroundResource(R.drawable.pausebutton);
                    showNotification(R.drawable.pausebutton);

                    // Restore the volume to its original level here
                    MusicList.mediaPlayer.setVolume(1.0f, 1.0f); // 1.0f means full volume
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                MusicList.mediaPlayer.stop();
                MusicList.mediaPlayer.release();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (MusicList.mediaPlayer.isPlaying()) {
                    MusicList.mediaPlayer.pause();
                    MusicList.playPauseButton.setBackgroundResource(R.drawable.playbutton);
                    MainActivity.pauseButton.setBackgroundResource(R.drawable.playbutton);
                    showNotification(R.drawable.playbutton);
                }

                // You can pause your audio here
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Temporary loss of audio focus, lower volume
                // You can lower the volume of your audio here
                if (MusicList.mediaPlayer.isPlaying()) {
                    MusicList.mediaPlayer.setVolume(0.2f, 0.2f); // Adjust the volume level (e.g., 0.2f is 20% volume)
                }
                break;
        }
    }
    @SuppressLint("RestrictedApi")
    public void showNotification(int playPauseButton)
    {   Intent intent,prevIntent,playIntent,nextIntent;
        PendingIntent contentIntent,prevPendingIntent,playPendingIntent,nextPendingIntent;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
            intent = new Intent(context, MusicList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            prevIntent = new Intent(context, NotificationReceiver.class).setAction(ACTION_PREV);
            prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);
            playIntent = new Intent(context, NotificationReceiver.class).setAction(ACTION_PLAY);
            playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
            nextIntent = new Intent(context, NotificationReceiver.class).setAction(ACTION_NEXT);
            nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            intent = new Intent(context, MusicList.class);
            contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
            prevIntent = new Intent(context, NotificationReceiver.class).setAction(ACTION_PREV);
            prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playIntent = new Intent(context, NotificationReceiver.class).setAction(ACTION_PLAY);
            playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nextIntent = new Intent(context, NotificationReceiver.class).setAction(ACTION_NEXT);
            nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Bitmap picture = null;
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(MusicList.NowPlayingList.get(pos).getFilePath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                picture = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            } else {
                picture=BitmapFactory.decodeResource(context.getResources(),R.drawable.playing);
            }
            retriever.release();
        }
        catch (Exception ae)
        {
            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
        }
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context, Channel_ID_1)
                .setSmallIcon(R.drawable.musicbutton)
                .setLargeIcon(picture)
                .setContentTitle(MusicList.NowPlayingList.get(pos).getTitle())
                .setContentText(MusicList.NowPlayingList.get(pos).getArtist())
                .addAction(R.drawable.prevbutton, "Previous", prevPendingIntent)
                .addAction(playPauseButton, "PLAY", playPendingIntent)
                .addAction(R.drawable.nextbutton, "NEXT", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())   //.setMediaSession(mediaSession.getSessionToken())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setSilent(true);
        Notification notification = builder.build();
        NotificationManager notificationManager=(NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);

    }

}

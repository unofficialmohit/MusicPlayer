package com.mg.music;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.graphics.Color.parseColor;
import static com.mg.music.MusicList.NowPlayingList;
import static com.mg.music.MusicList.audioFocusListener;
import static com.mg.music.MusicList.audioManager;
import static com.mg.music.MusicList.getDominantColor;
import static com.mg.music.MusicList.pos;
import static com.mg.music.MyApplication.ACTION_NEXT;
import static com.mg.music.MyApplication.ACTION_PLAY;
import static com.mg.music.MyApplication.ACTION_PREV;
import static com.mg.music.MyApplication.Channel_ID_1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class NowPlayingPopup extends BottomSheetDialogFragment {
 public RecyclerView recyclerView;
 public AudioAdapter audioAdapter;

 Context context;
    NowPlayingPopup(Context context)
    {
        this.context=context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.now_playing_popup, container, false);

        recyclerView=view.findViewById(R.id.nowPlayingRecycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        audioAdapter=new AudioAdapter(NowPlayingList);
        recyclerView.setAdapter(audioAdapter);
        if (layoutManager != null) {
            layoutManager.scrollToPosition(pos);
        }
        audioAdapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                pos = position;   //sending position to static variable to use in mainActivity

                AudioFile clickedAudio = NowPlayingList.get(position);
                try {
                    showNotification(R.drawable.pausebutton);
                } catch (Exception ae) {
                    Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
                }
                MusicList.songName.setText(clickedAudio.getTitle());
                MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
                MainActivity.pauseButton.setBackgroundResource(R.drawable.pausebutton);
                MainActivity.nowPlayingText.setText(clickedAudio.getTitle());
                MainActivity.artistName.setText(clickedAudio.getArtist());
                String time=milliSecondsToTimer(clickedAudio.getDuration());
                MainActivity.ftiming.setText(time);


                playAudio(clickedAudio.getFilePath());
            }
        });

        return view;
    }
    public String milliSecondsToTimer(long milliseconds){

        String timerString="";
        int hours=(int)(milliseconds/(1000*3600));
        int minutes=(int)(milliseconds%(1000*3600))/(1000*60);
        int seconds=(int)(milliseconds%(1000*60))/(1000);
        if(hours>0)
        {
            timerString=String.format("%02d:%02d:%02d",hours,minutes,seconds);
        }
        else
        {
            timerString=String.format("%02d:%02d",minutes,seconds);
        }
        return timerString;
    }
    public void playAudio(String filePath) {
        getAudioFocus();
        if (filePath == null || !new File(filePath).exists()) {
            Toast.makeText(context, "Invalid file path", Toast.LENGTH_SHORT).show();
            return;
        }
        if (MusicList.mediaPlayer.isPlaying()) {
            MusicList.mediaPlayer.stop();
        }
        MusicList.mediaPlayer.reset();

        try{
            MusicList.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(MusicList.repeat==0) {
                        mediaPlayer.start();  //if want repeat mode on
                    }
                    else{
                        if (pos < NowPlayingList.size()-1) {
                            ++pos;
                        }
                        else if(pos== NowPlayingList.size()-1)
                        {
                            pos=0;
                        }
                        AudioFile clickedAudio= NowPlayingList.get(pos);
                        clickedAudio.getFilePath();
                        playAudio(clickedAudio.getFilePath());
                    }
                    showNotification(R.drawable.pausebutton);
                }
            });
            MusicList.mediaPlayer.setDataSource(filePath);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA) // Set the usage (e.g., media playback)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Set the content type (e.g., music)
                    .build();
            MusicList.mediaPlayer.setAudioAttributes(audioAttributes);
            MusicList.mediaPlayer.prepare();
            MusicList.mediaPlayer.start();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                MusicList.thumb.setImageBitmap(albumArtBitmap);
                MainActivity.imgView.setImageBitmap(albumArtBitmap);
                MainActivity.relativeLayout.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
            } else {
                MusicList.thumb.setImageResource(R.drawable.musicbutton);
                MusicList.thumb.setImageResource(R.drawable.playing);
                MainActivity.relativeLayout.setBackgroundColor(Color.BLACK);
                MusicList.miniPlayer.setBackgroundColor(parseColor("#1E1B1B"));
                MusicList.seekList.setBackgroundColor(parseColor("#1E1B1B"));
            }
            retriever.release();

        }
        catch (Exception ae)
        {
            ae.printStackTrace();
            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
        }

    }
    public void getAudioFocus()
    {
        if (MusicList.isFocused == 0) {
            MusicList.isFocused=1;
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioFocusListener = new MyAudioFocusChangeListener(context);
            int result = audioManager.requestAudioFocus(
                    audioFocusListener,
                    AudioManager.STREAM_MUSIC, // Audio stream type
                    AudioManager.AUDIOFOCUS_GAIN // Request permanent audio focus
            );

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Toast.makeText(context, "Audio Focus Not Available", Toast.LENGTH_SHORT).show();
            }
        }
    }
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
            retriever.setDataSource(NowPlayingList.get(pos).getFilePath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                picture = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            } else {
                picture=BitmapFactory.decodeResource(this.getResources(),R.drawable.playing);
            }
            retriever.release();
        }
        catch (Exception ae)
        {
            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Channel_ID_1)
                .setSmallIcon(R.drawable.musicbutton)
                .setLargeIcon(picture)
                .setContentTitle(NowPlayingList.get(pos).getTitle())
                .setContentText(NowPlayingList.get(pos).getArtist())
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

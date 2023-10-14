package com.mg.music;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.net.Uri;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {
public MediaPlayer mediaPlayer;
public TextView nowPlayingText;
public EditText songSelection;
public ImageView imgView;
public SeekBar seekbar;
public TextView itiming,ftiming;
Drawable drawplaybutton,drawpausebutton;
public ImageButton imageButton,pauseButton,nextButton,prevButton;
String path="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            imageButton=findViewById(R.id.crossed);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel channel=new NotificationChannel("music_channel","Music Channel", NotificationManager.IMPORTANCE_LOW);
//            NotificationManager notificationManager=getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }

        path = getIntent().getStringExtra("path");
        itiming = findViewById(R.id.itiming);
        ftiming = findViewById(R.id.ftiming);


        imgView = findViewById(R.id.art);
        imgView.setBackgroundResource(R.drawable.playing);


        nowPlayingText = findViewById(R.id.nowPlayingText);
        nowPlayingText.setSelected(true);

        prevButton=findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        nextButton=findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

//        playButton=findViewById(R.id.playButton);
//        playButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                playAudio( songSelection.getText());
//                seekbar.setVisibility(View.VISIBLE);
//
//            }
//        });
        drawpausebutton = ContextCompat.getDrawable(this, R.drawable.pausebutton);
        drawplaybutton = ContextCompat.getDrawable(this, R.drawable.playbutton);

        pauseButton = findViewById(R.id.playpauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        pauseButton.setBackgroundResource(R.drawable.playbutton);
                    } else {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                        mediaPlayer.start();
                        pauseButton.setBackgroundResource(R.drawable.pausebutton);

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Nothing to Play", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        stopButton=findViewById(R.id.stopButton);
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(mediaPlayer!=null)
//                {
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
//                    seekbar.setVisibility(View.INVISIBLE);
//                    mediaPlayer=null;
//                }
//            }
//        });

        seekbar = findViewById(R.id.seek);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b && mediaPlayer != null) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        try {
            MyApplication myapp=(MyApplication)getApplication();
            mediaPlayer=myapp.getMediaPlayer();
            if(path!=null)
            { playAudio(path);}
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                imgView.setImageBitmap(albumArtBitmap);
            } else {
                imgView.setImageResource(R.drawable.playing);
            }
            retriever.release();

        }
        catch (Exception ae)
        {
            Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    public void UpdateSeek()
    {
    if(mediaPlayer!=null)
    {
        int currentPosition=mediaPlayer.getCurrentPosition();
        int totalDuration=mediaPlayer.getDuration();
        seekbar.setMax(totalDuration);
        seekbar.setProgress(currentPosition);
        itiming.setText(milliSecondsToTimer(currentPosition));
        if(totalDuration==currentPosition)
        {
            playAudio( songSelection.getText().toString());
        }
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateSeek();
            }
        },500);
    }
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
    public void playAudio(String edittext) {

        String filePath=edittext.toString();
        if (filePath == null || !new File(filePath).exists()) {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        try{
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();
            filePath = filePath.substring(filePath.lastIndexOf("/")+1);
            nowPlayingText.setText(filePath);
            ftiming.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
            UpdateSeek();


        }
        catch (Exception ae)
        {
            ae.printStackTrace();
            String a= String.valueOf(ae);
           Toast.makeText(this, a, Toast.LENGTH_SHORT).show();
        }

    }

//    @Override
//    protected void onDestroy() {
//       super.onDestroy();
//        if(mediaPlayer!=null)
//        {
//            mediaPlayer.release();
//        }
//    }
}
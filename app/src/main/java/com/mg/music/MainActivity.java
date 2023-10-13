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
public Button playButton,pauseButton,browseButton,showUrl;
public ImageView imgView;
public SeekBar seekbar;
public TextView itiming,ftiming;
Drawable drawplaybutton,drawpausebutton;
public static final int REQUEST_CODE_PICKER_FILE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel channel=new NotificationChannel("music_channel","Music Channel", NotificationManager.IMPORTANCE_LOW);
//            NotificationManager notificationManager=getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }


        itiming=findViewById(R.id.itiming);
        ftiming=findViewById(R.id.ftiming);

        showUrl=findViewById(R.id.url);
        imgView = findViewById(R.id.art);
        imgView.setBackgroundResource(R.drawable.playing);

        songSelection=findViewById(R.id.songSelect);

        nowPlayingText=findViewById(R.id.nowPlayingText);
        nowPlayingText.setSelected(true);
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
        drawpausebutton= ContextCompat.getDrawable(this,R.drawable.pausebutton);
        drawplaybutton= ContextCompat.getDrawable(this,R.drawable.playbutton);

        pauseButton=findViewById(R.id.playpauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer!=null){
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                    pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawplaybutton, null, null, null);
                    pauseButton.setText("Play");
                }
                else
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    mediaPlayer.start();
                    pauseButton.setText("Pause");

                    pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawpausebutton, null, null, null);

                }
                }
                else
                {
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
        browseButton=findViewById(R.id.browseButton);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        openFilePicker();
            }
        });

        seekbar=findViewById(R.id.seek);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b && mediaPlayer!=null)
                {
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
            playAudio( songSelection.getText());
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
  public void NextPage(View view)
  {
//      Toast.makeText(this, "WORKING ON IT PLOX WAIT :-/", Toast.LENGTH_SHORT).show();
      if(songSelection.getVisibility()==View.INVISIBLE)
      {
          songSelection.setVisibility(View.VISIBLE);
          showUrl.setText("Hide Path");
      }else
      {
          songSelection.setVisibility(View.INVISIBLE);
          showUrl.setText("Show Path");

      }
  }


    public void openFilePicker() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"Select a File"),REQUEST_CODE_PICKER_FILE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_CODE_PICKER_FILE && resultCode == RESULT_OK && data != null) {
                Uri selectedFileUri = data.getData();
                String filePath = selectedFileUri.getPath();
                filePath = filePath.substring(filePath.indexOf("/document/primary:") + "/document/primary:".length());
                filePath = "/storage/emulated/0/" + filePath;
                songSelection.setText(filePath);
                playAudio( songSelection.getText());

                pauseButton.setText("Pause");

                pauseButton.setCompoundDrawablesWithIntrinsicBounds(drawpausebutton, null, null, null);
                seekbar.setVisibility(View.VISIBLE);
                songSelection.setVisibility(View.INVISIBLE);
                showUrl.setText("Show Path");

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath);
                byte[] albumArt = retriever.getEmbeddedPicture();
                if (albumArt != null) {
                    Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                    imgView.setImageBitmap(albumArtBitmap);
                } else {
                    imgView.setImageResource(R.drawable.playing);
                }
                retriever.release();
            }
        }
        catch(Exception ae)
        {
            Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void playAudio(Editable edittext) {

        String filePath=edittext.toString();
        if (filePath == null || !new File(filePath).exists()) {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mediaPlayer==null)
        {
            mediaPlayer=new MediaPlayer();
        }
        else
        {
            mediaPlayer.reset();
        }
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
            nowPlayingText.setText("Playing : "+ filePath);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null)
        {
            mediaPlayer.release();
        }
    }
}
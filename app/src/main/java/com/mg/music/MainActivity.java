package com.mg.music;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public MediaPlayer mediaPlayer;
    public TextView nowPlayingText,artistName;
    public EditText songSelection;
    public ImageView imgView;
    public SeekBar seekbar;
    public TextView itiming, ftiming;
    Drawable drawplaybutton, drawpausebutton;
    public ImageButton imageButton, pauseButton, nextButton, prevButton, repeatButton, shuffleButton;
    String path = "";
    int pos;
    String srch="";
    int repeat = 1;
    int shuffle = 0;
    int orderSort;
    int sortPos;
    public ArrayList<AudioFile> audioFiles = new ArrayList<>();

    public ArrayList<AudioFile> filteredAudioFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageButton = findViewById(R.id.crossed);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        pos =getIntent().getIntExtra("path",0);
        srch=getIntent().getStringExtra("find");
        sortPos=getIntent().getIntExtra("sortPosition",1);
        orderSort=getIntent().getIntExtra("sortOrder",1);
        loadAudioFiles();

        filteredAudioFiles = new ArrayList<>(audioFiles);

        filterAudioFiles(srch);


        AudioFile clickedAudio = audioFiles.get(pos);
        path = clickedAudio.getFilePath();

        itiming = findViewById(R.id.itiming);
        ftiming = findViewById(R.id.ftiming);

        shuffleButton = findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffle == 0) {
                    shuffleButton.setBackgroundResource(R.drawable.shuffleon);
                    shuffle = 1;
                    Collections.shuffle(audioFiles);

                } else {
                    shuffleButton.setBackgroundResource(R.drawable.shuffleoff);
                    shuffle = 0;
                    audioFiles.clear();
                    loadAudioFiles();
                    filterAudioFiles(srch);
                }
            }
        });

        repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeat == 1) {
                    repeat = 0;
                    repeatButton.setBackgroundResource(R.drawable.repeatone);

                } else {
                    repeat = 1;
                    repeatButton.setBackgroundResource(R.drawable.repeatall);
                }
            }
        });

        imgView = findViewById(R.id.art);
        imgView.setBackgroundResource(R.drawable.playing);




        prevButton = findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pos > 0 && mediaPlayer.getCurrentPosition() < 5000) {
                    --pos;
                }
                else if(pos==0 && mediaPlayer.getCurrentPosition() < 5000)
                {
                    pos=audioFiles.size()-1;
                }
                AudioFile clickedAudio = audioFiles.get(pos);
                path = clickedAudio.getFilePath();
                playAudio(path);
            }
        });
        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pos < audioFiles.size()-1) {
                    ++pos;
                }
                else if(pos==audioFiles.size()-1)
                {
                    pos=0;
                }
                AudioFile clickedAudio = audioFiles.get(pos);
                path = clickedAudio.getFilePath();
                playAudio(path);
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
            MyApplication myapp = (MyApplication) getApplication();
            mediaPlayer = myapp.getMediaPlayer();
            if (path != null) {
                playAudio(path);
            }
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
        },100);
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
                    if(repeat==0) {
                        mediaPlayer.start();  //if want repeat mode on
                    }
                    else{
                    ++pos;
                    AudioFile clickedAudio= audioFiles.get(pos);
                    path=clickedAudio.getFilePath();
                    playAudio(path);
                }
                }
            });
            mediaPlayer.setDataSource(filePath);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //depreciated
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA) // Set the usage (e.g., media playback)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Set the content type (e.g., music)
                    .build();

            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.prepare();
            mediaPlayer.start();
            AudioFile clickedAudio= audioFiles.get(pos);

            nowPlayingText = findViewById(R.id.nowPlayingText);
            nowPlayingText.setSelected(true);
            nowPlayingText.setText(clickedAudio.getTitle());

            artistName=findViewById(R.id.artistName);
            artistName.setText(clickedAudio.getArtist());


            ftiming.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

            UpdateSeek();
//            fetching album art directly from file path
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

//            fetching album art using Glide Library (it shows delay on album art on now playing , so not suitable )
//            AudioFile clickedAudio= audioFiles.get(pos);
//            RequestOptions requestOptions = new RequestOptions()
//                    .placeholder(R.drawable.playing) // Placeholder image while loading
//                    .error(R.drawable.playing); // Error image if loading fails
//            Glide.with(imgView.getContext())
//                    .load(clickedAudio.getAlbumArtUri())
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.playing))
//                    .into(imgView);

//                Toast.makeText(MusicList.this, clickedAudio.getFilePath(), Toast.LENGTH_SHORT).show();


        }
        catch (Exception ae)
        {
            ae.printStackTrace();
            String a= String.valueOf(ae);
           Toast.makeText(this, a, Toast.LENGTH_SHORT).show();
        }

    }
    public void loadAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(
                uri,
                null,
                null,
                null,
                null
        );
        if(cursor!=null && cursor.moveToFirst()){
            do{
                String title=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String filePath= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long albumId=cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri=Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri= ContentUris.withAppendedId(sArtworkUri,albumId);
                String album=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String ModifiedDate= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED));
                Long Duration=cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String DateAdded=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));

                audioFiles.add(new AudioFile(title,artist,filePath,albumId,albumArtUri,album,ModifiedDate,Duration,DateAdded));
            }while(cursor.moveToNext());
            cursor.close();
            sortMusic();
        }
    }
    public void filterAudioFiles(String query) {
        audioFiles.clear();
        if(query.isEmpty()){
            audioFiles.addAll(filteredAudioFiles);
        }
        else
        {
            for(AudioFile file:filteredAudioFiles)
            {
                if(file.getTitle().toLowerCase().contains(query.toLowerCase()) || file.getArtist().toLowerCase().contains(query.toLowerCase()))
                {
                    audioFiles.add(file);
                }
            }
        }
    }
    public void sortMusic()
    {
        if (orderSort == 1) {


            switch (sortPos) {

                case 1:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getDateAdded().compareTo(audioFile2.getDateAdded());
                        }
                    });
                    break;
                case 2:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getTitle().compareTo(audioFile2.getTitle());
                        }
                    });
                    break;
                case 3:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getArtist().compareTo(audioFile2.getArtist());
                        }
                    });
                    break;
                case 4:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getAlbum().compareTo(audioFile2.getAlbum());
                        }
                    });
                    break;
                case 5:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getModifiedDate().compareTo(audioFile2.getModifiedDate());
                        }
                    });

                    break;
                case 6:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return Long.compare(audioFile1.getDuration(), audioFile2.getDuration());
                        }
                    });

                    break;
                default:
                    Toast.makeText(this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else
        {
            switch (sortPos) {
                case 1:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getDateAdded().compareTo(audioFile1.getDateAdded());
                        }
                    });
                    break;
                case 2:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getTitle().compareTo(audioFile1.getTitle());
                        }
                    });
                    break;
                case 3:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getArtist().compareTo(audioFile1.getArtist());
                        }
                    });
                    break;
                case 4:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getAlbum().compareTo(audioFile1.getAlbum());
                        }
                    });
                    break;
                case 5:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getModifiedDate().compareTo(audioFile1.getModifiedDate());
                        }
                    });

                    break;
                case 6:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return Long.compare(audioFile2.getDuration(), audioFile1.getDuration());
                        }
                    });
                    break;

                default:
                    Toast.makeText(this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                    break;
            }
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
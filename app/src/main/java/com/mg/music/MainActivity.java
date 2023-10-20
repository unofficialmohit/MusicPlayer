package com.mg.music;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    public TextView nowPlayingText,artistName;
    public EditText songSelection;
    public ShapeableImageView imgView;
    public SeekBar seekbar;
    public TextView itiming, ftiming;
    Drawable drawplaybutton, drawpausebutton;
    public ImageButton imageButton, pauseButton, nextButton, prevButton, repeatButton, shuffleButton;
    String path = "";



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

        AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
        path = clickedAudio.getFilePath();

        itiming = findViewById(R.id.itiming);
        ftiming = findViewById(R.id.ftiming);

        shuffleButton = findViewById(R.id.shuffleButton);
        if(MusicList.shuffle==1)
        {
            shuffleButton.setBackgroundResource(R.drawable.shuffleon);
        }
        else
        {
            shuffleButton.setBackgroundResource(R.drawable.shuffleoff);
        }
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicList.shuffle == 0) {
                    shuffleButton.setBackgroundResource(R.drawable.shuffleon);
                    MusicList.shuffle = 1;
                    AudioFile Ax=MusicList.NowPlayingList.get(MusicList.pos);
                    MusicList.cacheAudioFiles.clear();
                    MusicList.cacheAudioFiles.addAll(MusicList.NowPlayingList);
                    Collections.shuffle(MusicList.NowPlayingList);
                    int index=0;
                    for(AudioFile x: MusicList.NowPlayingList)
                    {
                        if(Ax.getTitle().equals(x.getTitle()))
                        {
                            break;
                        }
                        index++;
                    }
                    MusicList.NowPlayingList.remove(index);
                    MusicList.NowPlayingList.add(0,Ax);
                    MusicList.pos=0;

                } else {
                    shuffleButton.setBackgroundResource(R.drawable.shuffleoff);
                    MusicList.shuffle = 0;
                    AudioFile Ax=MusicList.NowPlayingList.get(MusicList.pos);
                    MusicList.NowPlayingList.clear();
                    MusicList.NowPlayingList.addAll(MusicList.cacheAudioFiles);
                    int index=0;
                    for(AudioFile x:MusicList.NowPlayingList)
                    {
                        if(Ax.getTitle().equals(x.getTitle()))
                        {
                            break;
                        }
                            index++;
                    }
                    MusicList.pos=index;
                }
            }
        });

        repeatButton = findViewById(R.id.repeatButton);

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicList.repeat == 1) {
                    MusicList.repeat = 0;
                    repeatButton.setBackgroundResource(R.drawable.repeatone);

                } else {
                    MusicList.repeat = 1;
                    repeatButton.setBackgroundResource(R.drawable.repeatall);
                }
            }
        });

        imgView = findViewById(R.id.art);
        imgView.setBackgroundResource(R.drawable.playing);
        ShapeAppearanceModel shapeAppearanceModel = new
                ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 30) // Set radius for rounded corners
                .build();
        imgView.setShapeAppearanceModel(shapeAppearanceModel);




        prevButton = findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicList.pos > 0 && MusicList.mediaPlayer.getCurrentPosition() < 5000) {
                    --MusicList.pos;
                }
                else if(MusicList.pos==0 && MusicList.mediaPlayer.getCurrentPosition() < 5000)
                {
                    MusicList.pos=MusicList.NowPlayingList.size()-1;
                }
                AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);
                path = clickedAudio.getFilePath();
                playAudio(path);
            }
        });
        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicList.pos < MusicList.NowPlayingList.size()-1) {
                    ++MusicList.pos;
                }
                else if(MusicList.pos==MusicList.NowPlayingList.size()-1)
                {
                    MusicList.pos=0;
                }
                AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
                path = clickedAudio.getFilePath();
                playAudio(path);
            }
        });
        drawpausebutton = ContextCompat.getDrawable(this, R.drawable.pausebutton);
        drawplaybutton = ContextCompat.getDrawable(this, R.drawable.playbutton);

        pauseButton = findViewById(R.id.playpauseButton);
        if(MusicList.mediaPlayer.isPlaying())
        {
            pauseButton.setBackgroundResource(R.drawable.pausebutton);

        }
        else
        {
            pauseButton.setBackgroundResource(R.drawable.playbutton);

        }
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicList.mediaPlayer != null) {
                    if (MusicList.mediaPlayer.isPlaying()) {
                        MusicList.mediaPlayer.pause();
                        pauseButton.setBackgroundResource(R.drawable.playbutton);
                    } else {
                        MusicList.mediaPlayer.seekTo(MusicList.mediaPlayer.getCurrentPosition());
                        MusicList.mediaPlayer.start();
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
                if (b && MusicList.mediaPlayer != null) {
                    MusicList.mediaPlayer.seekTo(i);
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
            if (path != null) {
                resumeAudio(path);
                if(MusicList.repeat==1)
                {
                    repeatButton.setBackgroundResource(R.drawable.repeatall);
                }
                else
                {
                    repeatButton.setBackgroundResource(R.drawable.repeatone);
                }
                if(MusicList.shuffle==1)
                {
                    shuffleButton.setBackgroundResource(R.drawable.shuffleon);
                }
                else
                {
                    shuffleButton.setBackgroundResource(R.drawable.shuffleoff);
                }
            }
        }
        catch (Exception ae)
        {
            Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    public void UpdateSeek()
    {
        if(MusicList.mediaPlayer!=null)
        {
            int currentPosition=MusicList.mediaPlayer.getCurrentPosition();
            int totalDuration=MusicList.mediaPlayer.getDuration();
            seekbar.setMax(totalDuration);
            seekbar.setProgress(currentPosition);
            itiming.setText(milliSecondsToTimer(currentPosition));
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
    public void resumeAudio(String filePath) {

        if (MusicList.mediaPlayer.isPlaying()) {
            seekbar.setProgress(MusicList.mediaPlayer.getCurrentPosition(),true);
            pauseButton.setBackgroundResource(R.drawable.pausebutton);
        }


        try{

            MusicList.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(MusicList.repeat==0) {
                        mediaPlayer.start();  //if want repeat mode on
                    }
                    else{
                        if (MusicList.pos < MusicList.NowPlayingList.size()-1) {
                            ++MusicList.pos;
                        }
                        else if(MusicList.pos==MusicList.NowPlayingList.size()-1)
                        {
                            MusicList.pos=0;
                        }
                        AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);

                        MusicList.songName.setText(clickedAudio.getTitle());
                        try {

                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(clickedAudio.getFilePath());
                            byte[] albumArt = retriever.getEmbeddedPicture();
                            if (albumArt != null) {
                                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                                MusicList.thumb.setImageBitmap(albumArtBitmap);
                            } else {
                                MusicList.thumb.setImageResource(R.drawable.playing);
                            }
                            retriever.release();
                            MusicList.songName.setText(clickedAudio.getTitle());
                        }
                        catch (Exception ae)
                        {
                            Toast.makeText(MainActivity.this, ae.toString(), Toast.LENGTH_SHORT).show();
                        }
                        clickedAudio.getFilePath();
                        playAudio(clickedAudio.getFilePath());
                    }
                }
            });


            AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);

            nowPlayingText = findViewById(R.id.nowPlayingText);
            nowPlayingText.setSelected(true);
            nowPlayingText.setText(clickedAudio.getTitle());

            artistName=findViewById(R.id.artistName);
            artistName.setText(clickedAudio.getArtist());

            ftiming.setText(milliSecondsToTimer(MusicList.mediaPlayer.getDuration()));

            UpdateSeek();
//            fetching album art directly from file path
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
        catch (Exception ae)
        {
            ae.printStackTrace();
            Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
        }

    }
    public void playAudio(String filePath) {

        if (filePath == null || !new File(filePath).exists()) {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
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
                        if (MusicList.pos < MusicList.NowPlayingList.size()-1) {
                            ++MusicList.pos;
                        }
                        else if(MusicList.pos==MusicList.NowPlayingList.size()-1)
                        {
                            MusicList.pos=0;
                        }
                        AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);
                        MusicList.songName.setText(clickedAudio.getTitle());
                        try {

                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(clickedAudio.getFilePath());
                            byte[] albumArt = retriever.getEmbeddedPicture();
                            if (albumArt != null) {
                                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                                MusicList.thumb.setImageBitmap(albumArtBitmap);
                            } else {
                                MusicList.thumb.setImageResource(R.drawable.playing);
                            }
                            retriever.release();
                            MusicList.songName.setText(clickedAudio.getTitle());
                        }
                        catch (Exception ae)
                        {
                            Toast.makeText(MainActivity.this, ae.toString(), Toast.LENGTH_SHORT).show();
                        }
                        path=clickedAudio.getFilePath();
                        playAudio(path);
                    }
                }
            });
            MusicList.mediaPlayer.setDataSource(filePath);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //depreciated
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA) // Set the usage (e.g., media playback)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Set the content type (e.g., music)
                    .build();

            MusicList.mediaPlayer.setAudioAttributes(audioAttributes);
            MusicList.mediaPlayer.prepare();
            MusicList.mediaPlayer.start();
            AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);

            nowPlayingText = findViewById(R.id.nowPlayingText);
            nowPlayingText.setSelected(true);
            nowPlayingText.setText(clickedAudio.getTitle());

            artistName=findViewById(R.id.artistName);
            artistName.setText(clickedAudio.getArtist());


            ftiming.setText(milliSecondsToTimer(MusicList.mediaPlayer.getDuration()));

            UpdateSeek();
//            fetching album art directly from file path
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



//    @Override
//    protected void onDestroy() {
//       super.onDestroy();
//        if(mediaPlayer!=null)
//        {
//            mediaPlayer.release();
//        }
//    }
}
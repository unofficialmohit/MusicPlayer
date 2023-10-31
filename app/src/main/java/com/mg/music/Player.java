package com.mg.music;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.graphics.Color.parseColor;
import static com.mg.music.MusicList.getDominantColor;
import static com.mg.music.MyApplication.ACTION_NEXT;
import static com.mg.music.MyApplication.ACTION_PLAY;
import static com.mg.music.MyApplication.ACTION_PREV;
import static com.mg.music.MyApplication.Channel_ID_1;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.io.File;
import java.util.Collections;

public class Player extends BottomSheetDialogFragment {
    public Context context;
    public static TextView nowPlayingText,artistName;
    public static ShapeableImageView imgView;
    public SeekBar seekbar;
    public TextView itiming;
    public static TextView ftiming;
    Drawable drawplaybutton, drawpausebutton;
    public ImageButton nextButton, prevButton, repeatButton, shuffleButton;
    public static ImageButton pauseButton;
    String path = "";
    public static RelativeLayout relativeLayout;
    public View view;

    Player(Context context)
    {
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main, container, false);
        relativeLayout=view.findViewById(R.id.bgColor);
        MusicList.isMediaActive=1;
        AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
        path = clickedAudio.getFilePath();

        itiming = view.findViewById(R.id.itiming);
        ftiming = view.findViewById(R.id.ftiming);

        shuffleButton = view.findViewById(R.id.shuffleButton);
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

        repeatButton = view.findViewById(R.id.repeatButton);

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

        imgView = view.findViewById(R.id.art);
        imgView.setBackgroundResource(R.drawable.playing);
        imgView.setOnTouchListener(new OnSwipeTouchListener(context)
        {
            public void onSwipeDown() {
                onBackButtonPressed();
            }

            public void onSwipeLeft()
            {
//                nextSong();
            }
            public void onSwipeRight()
            {
//                prevSong();
            }
        });
        ShapeAppearanceModel shapeAppearanceModel = new
                ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 30) // Set radius for rounded corners
                .build();
        imgView.setShapeAppearanceModel(shapeAppearanceModel);




        prevButton = view.findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prevSong();
            }
        });
        nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong();
            }
        });
        drawpausebutton = ContextCompat.getDrawable(context, R.drawable.pausebutton);
        drawplaybutton = ContextCompat.getDrawable(context, R.drawable.playbutton);

        pauseButton = view.findViewById(R.id.playpauseButton);
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
                        showNotification(R.drawable.playbutton);
                        pauseButton.setBackgroundResource(R.drawable.playbutton);
                        MusicList.playPauseButton.setBackgroundResource(R.drawable.playbutton);
                        MusicList.audioManager.abandonAudioFocus(MusicList.audioFocusListener);
                        MusicList.isFocused=0;


                    } else {
                        getAudioFocus();
                        MusicList.mediaPlayer.seekTo(MusicList.mediaPlayer.getCurrentPosition());
                        MusicList.mediaPlayer.start();
                        pauseButton.setBackgroundResource(R.drawable.pausebutton);
                        MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
                        showNotification(R.drawable.pausebutton);

                    }
                }
                else
                {
                    Toast.makeText(context, "Nothing to Play", Toast.LENGTH_SHORT).show();
                }

            }
        });

//        stopButton=view.findViewById(R.id.stopButton);
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

        seekbar = view.findViewById(R.id.seek);
        seekbar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
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
            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
        }
        return view;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        MusicList.activeWindow=1;

        final BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

        if (dialog != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 1); // Set your desired maximum width
                layoutParams.width = Math.min(maxWidth, getResources().getDisplayMetrics().widthPixels);
            } else {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }

            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(layoutParams);
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                final BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);

            }
        }


    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.activity_main, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setMaxWidth(Resources.getSystem().getDisplayMetrics().widthPixels);
        return bottomSheetDialog;
    }


    public void showNotification(int playPauseButton)
    {       Intent intent,prevIntent,playIntent,nextIntent;
        PendingIntent contentIntent,prevPendingIntent,playPendingIntent,nextPendingIntent;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
            intent = new Intent(context, MusicList.class);
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
            retriever.setDataSource(MusicList.NowPlayingList.get(MusicList.pos).getFilePath());
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
        Notification notification=new NotificationCompat.Builder(context,Channel_ID_1)
                .setSmallIcon(R.drawable.musicbutton)
                .setLargeIcon(picture)
                .setContentTitle(MusicList.NowPlayingList.get(MusicList.pos).getTitle())
                .setContentText(MusicList.NowPlayingList.get(MusicList.pos).getArtist())
                .addAction(R.drawable.prevbutton,"Previous",prevPendingIntent)
                .addAction(playPauseButton,"PLAY",playPendingIntent)
                .addAction(R.drawable.nextbutton,"NEXT",nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())   //.setMediaSession(mediaSession.getSessionToken())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setSilent(true)
                .build();
        NotificationManager notificationManager=(NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }

    public void prevSong() {
        if (MusicList.pos > 0 && MusicList.mediaPlayer.getCurrentPosition() < 5000) {
            --MusicList.pos;
        }
        else if(MusicList.pos==0 && MusicList.mediaPlayer.getCurrentPosition() < 5000)
        {
            MusicList.pos=MusicList.NowPlayingList.size()-1;
        }
        AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);
        pauseButton.setBackgroundResource(R.drawable.pausebutton);
        path = clickedAudio.getFilePath();
        MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
        MusicList.songName.setText(clickedAudio.getTitle());
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(MusicList.NowPlayingList.get(MusicList.pos).getFilePath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.thumb.setImageBitmap(albumArtBitmap);
                imgView.setImageBitmap(albumArtBitmap);
            } else {
                MusicList.thumb.setImageResource(R.drawable.musicbutton);
                imgView.setBackgroundResource(R.drawable.playing);
                MusicList.seekList.setBackgroundColor(parseColor("#1E1B1B"));
                MusicList.miniPlayer.setBackgroundColor(parseColor("#1E1B1B"));

            }
            retriever.release();
        }
        catch (Exception ae)
        {
            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
        }
        showNotification(R.drawable.pausebutton);
        playAudio(path);
    }
    public void getAudioFocus()
    {
        if (MusicList.isFocused == 0) {
            MusicList.isFocused=1;
            MusicList.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            MusicList.audioFocusListener = new MyAudioFocusChangeListener(context);
            int result = MusicList.audioManager.requestAudioFocus(
                    MusicList.audioFocusListener,
                    AudioManager.STREAM_MUSIC, // Audio stream type
                    AudioManager.AUDIOFOCUS_GAIN // Request permanent audio focus
            );

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Toast.makeText(context, "Audio Focus Not Available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void nextSong() {
        if (MusicList.pos < MusicList.NowPlayingList.size()-1) {
            ++MusicList.pos;
        }
        else if(MusicList.pos==MusicList.NowPlayingList.size()-1)
        {
            MusicList.pos=0;
        }
        AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
        path = clickedAudio.getFilePath();
        pauseButton.setBackgroundResource(R.drawable.pausebutton);
        MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
        MusicList.songName.setText(clickedAudio.getTitle());
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(MusicList.NowPlayingList.get(MusicList.pos).getFilePath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.thumb.setImageBitmap(albumArtBitmap);
                imgView.setImageBitmap(albumArtBitmap);
            } else {
                MusicList.thumb.setImageResource(R.drawable.musicbutton);
                imgView.setBackgroundResource(R.drawable.playing);
                MusicList.seekList.setBackgroundColor(parseColor("#1E1B1B"));
                MusicList.miniPlayer.setBackgroundColor(parseColor("#1E1B1B"));

            }
            retriever.release();
        }
        catch (Exception ae)
        {
            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
        }
        showNotification(R.drawable.pausebutton);
        playAudio(path);
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
            getAudioFocus();
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
                                relativeLayout.setBackgroundColor(getDominantColor(albumArtBitmap));
                                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
                                MusicList.thumb.setImageBitmap(albumArtBitmap);
                            } else {
                                relativeLayout.setBackgroundColor(Color.BLACK);
                                MusicList.thumb.setImageResource(R.drawable.playing);
                                MusicList.miniPlayer.setBackgroundColor(parseColor("#1E1B1B"));
                                MusicList.seekList.setBackgroundColor(parseColor("#1E1B1B"));
                            }
                            retriever.release();
                            MusicList.songName.setText(clickedAudio.getTitle());
                        }
                        catch (Exception ae)
                        {
                            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
                        }
                        clickedAudio.getFilePath();
                        playAudio(clickedAudio.getFilePath());
                    }
                    showNotification(R.drawable.pausebutton);
                }
            });


            AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);

            nowPlayingText = view.findViewById(R.id.nowPlayingText);
            nowPlayingText.setSelected(true);
            nowPlayingText.setText(clickedAudio.getTitle());
            LinearLayout linearLayout=view.findViewById(R.id.l2);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NowPlayingPopup nowPlayingPopup = new NowPlayingPopup(context);
                    nowPlayingPopup.show(getChildFragmentManager(), "NowPlayingPopup");
                }
            });

            artistName=view.findViewById(R.id.artistName);
            artistName.setText(clickedAudio.getArtist());

            ftiming.setText(milliSecondsToTimer(MusicList.mediaPlayer.getDuration()));

            UpdateSeek();
//            fetching album art directly from file path
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                relativeLayout.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
                imgView.setImageBitmap(albumArtBitmap);
            } else {
                imgView.setImageResource(R.drawable.playing);
                relativeLayout.setBackgroundColor(Color.BLACK);
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
                                relativeLayout.setBackgroundColor(getDominantColor(albumArtBitmap));
                                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
                                MusicList.thumb.setImageBitmap(albumArtBitmap);
                            } else {
                                MusicList.thumb.setImageResource(R.drawable.playing);
                                relativeLayout.setBackgroundColor(Color.BLACK);
                                MusicList.miniPlayer.setBackgroundColor(parseColor("#1E1B1B"));
                                MusicList.seekList.setBackgroundColor(parseColor("#1E1B1B"));

                            }
                            retriever.release();
                            MusicList.songName.setText(clickedAudio.getTitle());
                        }
                        catch (Exception ae)
                        {
                            Toast.makeText(context, ae.toString(), Toast.LENGTH_SHORT).show();
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

            nowPlayingText = view.findViewById(R.id.nowPlayingText);
            nowPlayingText.setSelected(true);
            nowPlayingText.setText(clickedAudio.getTitle());

            artistName=view.findViewById(R.id.artistName);
            artistName.setText(clickedAudio.getArtist());


            ftiming.setText(milliSecondsToTimer(MusicList.mediaPlayer.getDuration()));

            UpdateSeek();
//            fetching album art directly from file path
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                relativeLayout.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.miniPlayer.setBackgroundColor(getDominantColor(albumArtBitmap));
                MusicList.seekList.setBackgroundColor(getDominantColor(albumArtBitmap));
                imgView.setImageBitmap(albumArtBitmap);
            } else {
                imgView.setImageResource(R.drawable.playing);
                relativeLayout.setBackgroundColor(Color.BLACK);
                MusicList.miniPlayer.setBackgroundColor(parseColor("#1E1B1B"));
                MusicList.seekList.setBackgroundColor(parseColor("#1E1B1B"));
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

//                Toast.makeText(MusicList.context, clickedAudio.getFilePath(), Toast.LENGTH_SHORT).show();


        }
        catch (Exception ae)
        {
            ae.printStackTrace();
            String a= String.valueOf(ae);
            Toast.makeText(context, a, Toast.LENGTH_SHORT).show();
        }

    }
    private void onBackButtonPressed() {
        // Handle the back button press as needed
        // For example, dismiss the dialog or perform some action
        dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        MusicList.activeWindow=0;
        super.onCancel(dialog);
    }
}


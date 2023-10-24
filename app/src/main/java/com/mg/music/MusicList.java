package com.mg.music;

import static com.mg.music.MyApplication.ACTION_NEXT;
import static com.mg.music.MyApplication.ACTION_PLAY;
import static com.mg.music.MyApplication.ACTION_PREV;
import static com.mg.music.MyApplication.Channel_ID_1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MusicList extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    String srch="";
    public static List<AudioFile> audioFiles=new ArrayList<>();

    public List<AudioFile> filteredAudioFiles ;
    public static List<AudioFile> NowPlayingList= new ArrayList<>();
    public AudioAdapter audioAdapter;
    public static final int REQUEST_CODE = 1;
    int permissionAllowed =0;
    public SearchView searchSong;
    public Spinner spinner;
    public TextView totalSong;
    public ImageButton sortArrow;
    public int orderSort=0;
    public int sortPos=1;
    public static final String[] paths = {"Date Added","Name", "Artist Name","Album Name", "Modified Date","Duration"};
    public static ShapeableImageView thumb;
    public static TextView songName;
    public static ImageButton playPauseButton;
    public static int pos=0;
    public static int repeat=1;
    public static int shuffle=0;
    public static MediaPlayer mediaPlayer;
    public static boolean hasPlayed=false;
    boolean isLoaded=false;
    public static ArrayList<AudioFile> cacheAudioFiles =new ArrayList<>();
    public static SeekBar seekBar;
    public MusicService musicService;
    public static MediaSessionCompat mediaSession;
    public static int isMediaActive=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
        setContentView(R.layout.activity_music_list);
        audioFiles.clear();
        MyApplication myapp = (MyApplication) getApplication();
        mediaPlayer = myapp.getMediaPlayer();
        mediaSession=new MediaSessionCompat(this,"PlayerAudio");
        Intent intent =new Intent(MusicList.this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);

        // Check if the permission is granted
        thumb=findViewById(R.id.thumb);
        thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            NowPlay();
            }
        });
        ShapeAppearanceModel shapeAppearanceModel = new
                ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 25) // Set radius for rounded corners
                .build();
        thumb.setShapeAppearanceModel(shapeAppearanceModel);
        songName=findViewById(R.id.currentSongName);
        songName.setSelected(true);
        songName.setOnTouchListener(new OnSwipeTouchListener(MusicList.this){

            public void onSwipeLeft() {
                nextSong();
                showNotification(R.drawable.pausebutton);
            }

            public void onSwipeRight() {
               prevSong();
                showNotification(R.drawable.pausebutton);

            }

            @Override
            public void onSwipeUp() {
                NowPlay();

            }

            public void onSingleTap() {
                NowPlay();
            }


        });

        playPauseButton=findViewById(R.id.playPauseListPage);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(!hasPlayed)
            {
                    Toast.makeText(myapp, "Nothing to Play", Toast.LENGTH_SHORT).show();
                    return;
            }
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                playPauseButton.setBackgroundResource(R.drawable.playbutton);
                showNotification(R.drawable.playbutton);

            }
            else
            {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                mediaPlayer.start();
                playPauseButton.setBackgroundResource(R.drawable.pausebutton);
                showNotification(R.drawable.pausebutton);

            }
            }
        });


        totalSong=findViewById(R.id.totalSong);

        spinner=findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item,paths);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        sortMusic(1);
                        break;
                    case 1:
                        sortMusic(2);
                        break;
                    case 2:
                        sortMusic(3);
                        break;
                    case 3:
                        sortMusic(4);
                        break;
                    case 4:
                        sortMusic(5);
                        break;
                    case 5:
                        sortMusic(6);
                        break;
                    default:
                        Toast.makeText(MusicList.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        audioAdapter=new AudioAdapter(audioFiles);
        recyclerView.setAdapter(audioAdapter);

        sortArrow=findViewById(R.id.orderSort);
        sortArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setArrow(orderSort);
            }
        });

        //loadAudioFiles();
        //now audio files are loaded from the onRequestPermission method using loadAudioFiles() method

        searchSong=findViewById(R.id.searchsong);
        searchSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSong.setIconified(false);
            }
        });
        searchSong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAudioFiles(newText);
                srch=newText;
                audioAdapter.notifyDataSetChanged();
                return true;
            }
        });
        searchSong.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if(!searchSong.hasFocus())
                {
                    searchSong.setIconified(true);
                }
            }
        });

        seekBar=findViewById(R.id.seekBar);
        seekBar.getThumb().mutate().setAlpha(0);
        seekBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        MyAudioFocusChangeListener audioFocusListener = new MyAudioFocusChangeListener(this);

        int result = audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC, // Audio stream type
                AudioManager.AUDIOFOCUS_GAIN // Request permanent audio focus
        );

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Toast.makeText(myapp, "Audio Focus Not Available", Toast.LENGTH_SHORT).show();
        }

        audioAdapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//              AudioFile clickedAudio= audioFiles.get(position);
//              Toast.makeText(MusicList.this, clickedAudio.getFilePath(), Toast.LENGTH_SHORT).show();

//                intent.putExtra("path",position);
                pos=position;   //sending position to static variable to use in mainActivity
                NowPlayingList.clear();;
                NowPlayingList.addAll(audioFiles);
                AudioFile clickedAudio=NowPlayingList.get(position);
                try{
                showNotification(R.drawable.pausebutton);
                }
                catch (Exception ae)
                {
                    Toast.makeText(myapp, ae.toString() ,Toast.LENGTH_SHORT).show();
                }
                songName.setText(clickedAudio.getTitle());
                shuffle=0;
                playPauseButton.setBackgroundResource(R.drawable.pausebutton);

                hasPlayed=true;

                playAudio(clickedAudio.getFilePath());

            }
        });
    }

//    @Override
//    protected void onPause() {
//
//        super.onPause();
//        unbindService(this);
//    }

    private void prevSong() {
        if (hasPlayed) {
            if (MusicList.pos > 0 && MusicList.mediaPlayer.getCurrentPosition() < 5000) {
                --MusicList.pos;
            } else if (MusicList.pos == 0 && MusicList.mediaPlayer.getCurrentPosition() < 5000) {
                MusicList.pos = MusicList.NowPlayingList.size() - 1;
            }
            AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
            songName.setText(clickedAudio.getTitle());
            playAudio(clickedAudio.getFilePath());
            playPauseButton.setBackgroundResource(R.drawable.pausebutton);
            if(!mediaPlayer.isPlaying())
            {
                showNotification(R.drawable.pausebutton);
            }
            else
            {
                showNotification(R.drawable.playbutton);
            }
        }
    }

    private void nextSong() {
        if(hasPlayed)
        {
            if (MusicList.pos < MusicList.NowPlayingList.size()-1) {
                ++MusicList.pos;
            }
            else if(MusicList.pos==MusicList.NowPlayingList.size()-1)
            {
                MusicList.pos=0;
            }
            AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
            songName.setText(clickedAudio.getTitle());
            playAudio(clickedAudio.getFilePath());
            playPauseButton.setBackgroundResource(R.drawable.pausebutton);
            if(!mediaPlayer.isPlaying())
            {
                showNotification(R.drawable.pausebutton);
            }
            else
            {
                showNotification(R.drawable.playbutton);
            }
        }
    }
    public static Intent intent;
    public void NowPlay() {
        if(!hasPlayed)
        {
            Toast.makeText(this, "Nothing to Play", Toast.LENGTH_SHORT).show();
            return;
        }
        intent=new Intent(MusicList.this,MainActivity.class);
        startActivity(intent);
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
                        clickedAudio.getFilePath();
                        playAudio(clickedAudio.getFilePath());
                    }
                    showNotification(R.drawable.pausebutton);
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
            UpdateSeek();
            MusicList.mediaPlayer.start();
//            AudioFile clickedAudio= MusicList.NowPlayingList.get(MusicList.pos);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                thumb.setImageBitmap(albumArtBitmap);
            } else {
                thumb.setImageResource(R.drawable.musicbutton);
            }
            retriever.release();

        }
        catch (Exception ae)
        {
            ae.printStackTrace();
            Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
        }

    }
    public void UpdateSeek()
    {
        if(MusicList.mediaPlayer!=null)
        {
            int currentPosition=MusicList.mediaPlayer.getCurrentPosition();
            int totalDuration=MusicList.mediaPlayer.getDuration();
            seekBar.setMax(totalDuration);
            seekBar.setProgress(currentPosition);
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UpdateSeek();
                }
            },100);
        }
    }

    public void setArrow(int arrow)
    {
        if(arrow==1)
        {

            orderSort=0;
            sortArrow.setBackgroundResource(R.drawable.downarrow);
            sortMusic(sortPos);

        }
        else
        {
            orderSort=1;
            sortArrow.setBackgroundResource(R.drawable.uparrow);
            sortMusic(sortPos);
        }

    }
    public void checkPermission()
    {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
        };

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        // Load audio files if all permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED ) {
            permissionAllowed=1;
        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )
        {
            permissionAllowed=1;
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
                long Duration=cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String DateAdded=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));

                audioFiles.add(new AudioFile(title,artist,filePath,albumId,albumArtUri,album,ModifiedDate,Duration,DateAdded));
            }while(cursor.moveToNext());
            cursor.close();
            totalSong.setText(audioFiles.size()-1 +" Songs");
            sortMusic(1);
//            audioAdapter.notifyDataSetChanged();
        }
    }
    public void sortMusic(int pos)
    {
        sortPos=pos;
        if (orderSort == 1) {


            switch (pos) {

                case 1:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getDateAdded().compareTo(audioFile2.getDateAdded());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getTitle().compareTo(audioFile2.getTitle());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getArtist().compareTo(audioFile2.getArtist());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getAlbum().compareTo(audioFile2.getAlbum());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 5:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile1.getModifiedDate().compareTo(audioFile2.getModifiedDate());
                        }
                    });

                    audioAdapter.notifyDataSetChanged();
                    break;
                case 6:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return Long.compare(audioFile1.getDuration(), audioFile2.getDuration());
                        }
                    });

                    audioAdapter.notifyDataSetChanged();
                    break;
                default:
                    Toast.makeText(this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else
        {
            switch (pos) {
                case 1:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getDateAdded().compareTo(audioFile1.getDateAdded());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getTitle().compareTo(audioFile1.getTitle());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getArtist().compareTo(audioFile1.getArtist());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getAlbum().compareTo(audioFile1.getAlbum());
                        }
                    });
                    audioAdapter.notifyDataSetChanged();
                    break;
                case 5:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return audioFile2.getModifiedDate().compareTo(audioFile1.getModifiedDate());
                        }
                    });

                    audioAdapter.notifyDataSetChanged();
                    break;
                case 6:
                    Collections.sort(audioFiles, new Comparator<AudioFile>() {
                        @Override
                        public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                            return Long.compare(audioFile2.getDuration(), audioFile1.getDuration());
                        }
                    });

                    audioAdapter.notifyDataSetChanged();
                    break;

                default:
                    Toast.makeText(this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.S) {
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    permissionAllowed = 1;
                } else {
                    Toast.makeText(this, "Please Grant Permission", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    permissionAllowed = 0;
                }
            }
            else{
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                permissionAllowed =1;
            }
            else {
                Toast.makeText(this, "Please Grant Permission", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                permissionAllowed = 0;
            }
            }
            if(permissionAllowed ==1) {
                if (!isLoaded) {
                    loadAudioFiles();
                    isLoaded = true;
                }
                NowPlayingList.addAll(audioFiles);
                filteredAudioFiles = new ArrayList<>(audioFiles);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        if(hasPlayed) {
            if(mediaPlayer.isPlaying())
            {
                playPauseButton.setBackgroundResource(R.drawable.pausebutton);
            }
            else
            {
                playPauseButton.setBackgroundResource(R.drawable.playbutton);
            }
            AudioFile clickedAudio = MusicList.NowPlayingList.get(MusicList.pos);
            MusicList.songName.setText(clickedAudio.getTitle());
            try {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(clickedAudio.getFilePath());
                byte[] albumArt = retriever.getEmbeddedPicture();
                if (albumArt != null) {
                    Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                    MusicList.thumb.setImageBitmap(albumArtBitmap);
                } else {
                    MusicList.thumb.setImageResource(R.drawable.musicbutton);
                }
                retriever.release();
                MusicList.songName.setText(clickedAudio.getTitle());
            } catch (Exception ae) {
                Toast.makeText(MusicList.this, ae.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void nextClicked() {
    nextSong();
    showNotification(R.drawable.pausebutton);

    }

    @Override
    public void prevClicked() {
    prevSong();
        showNotification(R.drawable.pausebutton);
    }

    @Override
    public void playClicked() {
    if(!mediaPlayer.isPlaying())
    {
        showNotification(R.drawable.pausebutton);
        mediaPlayer.start();
        playPauseButton.setBackgroundResource(R.drawable.pausebutton);

    }
    else
    {
        showNotification(R.drawable.playbutton);
        mediaPlayer.pause();
        playPauseButton.setBackgroundResource(R.drawable.playbutton);

    }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder=(MusicService.MyBinder) iBinder;
        musicService=binder.getService();
        musicService.setCallBack(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    musicService=null;
    }

    public void showNotification(int playPauseButton)
    {   Intent intent,prevIntent,playIntent,nextIntent;
        PendingIntent contentIntent,prevPendingIntent,playPendingIntent,nextPendingIntent;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
            intent = new Intent(this, MusicList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
            prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);
            playIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
            playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
            nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
            nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            intent = new Intent(this, MusicList.class);
            contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
            prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
            prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
            playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
            nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
            Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_ID_1)
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
        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);

    }
}
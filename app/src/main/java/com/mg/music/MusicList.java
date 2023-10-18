package com.mg.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MusicList extends AppCompatActivity {
    String srch="";

    public List<AudioFile> audioFiles=new ArrayList<>();

    public List<AudioFile> filteredAudioFiles ;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_music_list);
        // Check if the permission is granted

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

        audioAdapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                AudioFile clickedAudio= audioFiles.get(position);
//                Toast.makeText(MusicList.this, clickedAudio.getFilePath(), Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MusicList.this,MainActivity.class);
                intent.putExtra("path",position);
                intent.putExtra("find",srch);
                intent.putExtra("sortOrder",orderSort);
                intent.putExtra("sortPosition",sortPos);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (!searchSong.isIconified()) {
            searchSong.setIconified(true);
        } else {
            super.onBackPressed();
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
            permissionAllowed =1;

        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )
        {
            permissionAllowed =1;
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
            for (int i = 1; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
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
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                permissionAllowed =1;
            }
            if(permissionAllowed ==1)
            {
                loadAudioFiles();
                filteredAudioFiles=new ArrayList<>(audioFiles);
            }

        }
    }

}

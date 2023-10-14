package com.mg.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MusicList extends AppCompatActivity {

    public ArrayList<AudioFile> audioFiles=new ArrayList<>();
    public AudioAdapter audioAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        audioAdapter=new AudioAdapter(audioFiles);
        recyclerView.setAdapter(audioAdapter);
        loadAudioFiles();
        audioAdapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                AudioFile clickedAudio= audioFiles.get(position);
//                Toast.makeText(MusicList.this, clickedAudio.getFilePath(), Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MusicList.this,MainActivity.class);
                intent.putExtra("path",Integer.toString(position));

                startActivity(intent);
            }
        });
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
                @SuppressLint("Range") String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range") String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                @SuppressLint("Range") String filePath= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                audioFiles.add(new AudioFile(title,artist,filePath));
            }while(cursor.moveToNext());
            cursor.close();
            Collections.sort(audioFiles, new Comparator<AudioFile>() {
                @Override
                public int compare(AudioFile audioFile1, AudioFile audioFile2) {
                    return audioFile1.getTitle().compareTo(audioFile2.getTitle());
                }
            });
            audioAdapter.notifyDataSetChanged();
        }
    }
}

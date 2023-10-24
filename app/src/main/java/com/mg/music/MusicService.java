package com.mg.music;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class MusicService extends Service {
    private IBinder mBinder=new MyBinder();
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREVIOUS";
    public static final String ACTION_PLAY="PLAY";

    ActionPlaying actionPlaying;

            @Override
            public IBinder onBind (Intent intent){
            return mBinder;

         }
        public class MyBinder extends Binder {
            MusicService getService(){
                return MusicService.this;
            }
        }


    @Override
        public int onStartCommand (Intent intent,int flags, int startId) {
        if (intent != null) {
            String actionName = intent.getStringExtra("myactionname");
            switch (actionName) {
                case ACTION_PLAY:
                    if (actionPlaying != null) {
                        actionPlaying.playClicked();
                        if (MusicList.isMediaActive == 1) {
                            if (MusicList.mediaPlayer.isPlaying()) {
                                MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
                                MainActivity.pauseButton.setBackgroundResource(R.drawable.pausebutton);
                            } else {
                                MusicList.playPauseButton.setBackgroundResource(R.drawable.playbutton);
                                MainActivity.pauseButton.setBackgroundResource(R.drawable.playbutton);

                            }
                        } else {
                            if (MusicList.mediaPlayer.isPlaying()) {
                                MusicList.playPauseButton.setBackgroundResource(R.drawable.pausebutton);
                            } else {
                                MusicList.playPauseButton.setBackgroundResource(R.drawable.playbutton);

                            }
                        }
                    }
                    break;
                case ACTION_NEXT:
                    if (actionPlaying != null) {
                        actionPlaying.nextClicked();
                        UpdateNowPlaying();

                    }
                    break;
                case ACTION_PREV:
                    if (actionPlaying != null) {
                        actionPlaying.prevClicked();
                        UpdateNowPlaying();
                    }
                    break;
            }

        }
        return START_STICKY;
    }
        public void setCallBack(ActionPlaying actionPlaying){
                this.actionPlaying=actionPlaying;
        }
        public void UpdateNowPlaying()
        {
            if(MusicList.isMediaActive==1)
            {
                MainActivity.nowPlayingText.setText(MusicList.NowPlayingList.get(MusicList.pos).getTitle());
                MainActivity.artistName.setText(MusicList.NowPlayingList.get(MusicList.pos).getArtist());
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(MusicList.NowPlayingList.get(MusicList.pos).getFilePath());
                    byte[] albumArt = retriever.getEmbeddedPicture();
                    if (albumArt != null) {
                        Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                        MainActivity.imgView.setImageBitmap(albumArtBitmap);
                    } else {
                        MainActivity.imgView.setImageResource(R.drawable.playing);
                    }
                    retriever.release();
                }
                catch (Exception ae)
                {
                    Toast.makeText(this, ae.toString(), Toast.LENGTH_SHORT).show();
                }
                String timerString="";
                int hours=(int)(MusicList.NowPlayingList.get(MusicList.pos).getDuration()/(1000*3600));
                int minutes=(int)(MusicList.NowPlayingList.get(MusicList.pos).getDuration()%(1000*3600))/(1000*60);
                int seconds=(int)(MusicList.NowPlayingList.get(MusicList.pos).getDuration()%(1000*60))/(1000);
                if(hours>0)
                {
                    timerString=String.format("%02d:%02d:%02d",hours,minutes,seconds);
                }
                else
                {
                    timerString=String.format("%02d:%02d",minutes,seconds);
                }
                MainActivity.ftiming.setText(timerString);

            }
        }


}

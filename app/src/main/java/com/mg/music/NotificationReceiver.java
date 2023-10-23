package com.mg.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREVIOUS";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_CONTENT="CONTENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context,MusicService.class);
if(intent.getAction()!=null)
{           intent1.putExtra("myactionname",intent.getAction());
            context.startService(intent1);
}
    }
}

package com.mg.music;


import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;

    public CustomUncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // Handle the uncaught exception here
        // You can log the exception, display an error message, or navigate to the first activity
        // For example, to navigate to the first activity, you can use an Intent:

        Intent intent = new Intent(context, MusicList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Terminate the current process to ensure a clean restart
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}

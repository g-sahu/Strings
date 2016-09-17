package com.mediaplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mediaplayer.activities.HomeActivity;

public class MediaplayerReceiver extends BroadcastReceiver {
    public MediaplayerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MediaplayerReceiver", "Broadcast received");
        //Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        intent.setClass(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

package com.mediaplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mediaplayer.activities.HomeActivity;

public class MediaPlayerReceiver extends BroadcastReceiver {
    public MediaPlayerReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MediaPlayerReceiver", "Broadcast received");
        intent.setClass(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

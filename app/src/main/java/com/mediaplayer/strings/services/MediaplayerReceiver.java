package com.mediaplayer.strings.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mediaplayer.strings.activities.HomeActivity;

public class MediaPlayerReceiver extends BroadcastReceiver {
    public MediaPlayerReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MediaPlayerReceiver", "Broadcast received");
        intent.setClass(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

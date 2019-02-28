package com.mediaplayer.strings.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import static android.support.v4.content.LocalBroadcastManager.*;
import static android.support.v4.content.LocalBroadcastManager.getInstance;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.init;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.ACTION_INIT_COMPLETE;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.FLAG_LIBRARY_CHANGED;

public class MediaManagerService extends IntentService {
    private BroadcastReceiver broadcastReceiver;

    public MediaManagerService() {
        super("MediaManagerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastReceiver = new MediaPlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INIT_COMPLETE);
        getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("MediaManagerService", "Inside MediaManagerService");

        //Initialising/Updating Mediaplayer library
        boolean isChanged = init(this);

        //Sending broadcast indicating that MediaManagerService has finished initiliasing/updating the library
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_INIT_COMPLETE);
        broadcastIntent.putExtra(FLAG_LIBRARY_CHANGED, isChanged);
        getInstance(this).sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}

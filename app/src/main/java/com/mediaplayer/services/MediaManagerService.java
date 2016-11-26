package com.mediaplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;

public class MediaManagerService extends IntentService {

    public MediaManagerService() {
        super("MediaManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("MediaManagerService", "Inside MediaManagerService");

        //Initialising/Updating Mediaplayer library
        boolean isChanged = MediaLibraryManager.init(this);

        //Sending broadcast indicating that MediaManagerService has finished initiliasing/updating the library
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.mediaplayer.SERVICE_BROADCAST");
        broadcastIntent.putExtra(MediaPlayerConstants.FLAG_LIBRARY_CHANGED, isChanged);

        sendBroadcast(broadcastIntent);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}
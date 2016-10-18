package com.mediaplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MessageConstants;

public class MediaManagerService extends IntentService {

    public MediaManagerService() {
        super("MediaManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("MediaManagerService", "Inside MediaManagerService");
        MediaLibraryManager.init(this);

        Intent broadcastIntent = new Intent();
        // TODO: 25-Sep-16 Change this intent in AndroidManifest.xml
        broadcastIntent.setAction("com.tutorialspoint.CUSTOM_INTENT");
        sendBroadcast(broadcastIntent);
    }
}


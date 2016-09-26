package com.mediaplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.mediaplayer.activities.HomeActivity;
import com.mediaplayer.dao.MediaplayerDBHelper;
import com.mediaplayer.utilities.MediaLibraryManager;

public class MediaManagerService extends IntentService {
    public MediaManagerService() {
        super("MediaManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("MediaManagerService", "Inside MediaManagerService");
        MediaLibraryManager.init(this);

        Intent intent1 = new Intent();
        // TODO: 25-Sep-16 Change this intent in AndroidManifest.xml
        intent1.setAction("com.tutorialspoint.CUSTOM_INTENT");
        sendBroadcast(intent1);
    }
}


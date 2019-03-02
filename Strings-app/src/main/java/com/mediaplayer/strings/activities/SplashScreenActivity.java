package com.mediaplayer.strings.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.mediaplayer.strings.services.MediaManagerService;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.mediaplayer.strings.R.layout.activity_splash_screen;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SplashScreenActivity";
    private Intent intent;

    //Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = { WRITE_EXTERNAL_STORAGE };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_splash_screen);
        Log.d(LOG_TAG, "SplashScreenActivity created");

        //Creating intent for MediaManagerService
        intent = new Intent(this, MediaManagerService.class);

        if(hasPermissions()) {
            Log.d(LOG_TAG, "Starting MediaManagerService...");
            startService(intent);
            Log.d(LOG_TAG, "Exiting SplashScreenActivity");
        } else {
            //Request read/write storage permission from the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private boolean hasPermissions() {
        int permission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        return permission == PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if(grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Starting MediaManagerService...");
                    startService(intent);
                } else {
                    finish();
                }
            }
        }
    }
}

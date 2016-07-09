package com.mediaplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mediaplayer.R;
import com.mediaplayer.adapters.CustomAdapter;
import com.mediaplayer.beans.Track;
import com.mediaplayer.utilities.MediaLibraryManager;

import java.util.ArrayList;

public class PlaylistActivity extends Activity {
    private Intent intent;
    private static ArrayList<Track> trackInfoList;
    private Track requestedTrack;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        checkPermissions();

        trackInfoList = new MediaLibraryManager().getTrackInfo(getResources());
        ListView listView = (ListView) findViewById(R.id.listView);
        ListAdapter listAdapter = new CustomAdapter(this, trackInfoList);
        listView.setAdapter(listAdapter);
        intent = new Intent(this, MediaPlayerActivity.class);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                requestedTrack = trackInfoList.get(position);
                intent.putExtra("requestedTrack", requestedTrack);
                startActivity(intent);
            }
        });
    }

    public void checkPermissions() {
        try {
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // Request for permission from the user
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the playList arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
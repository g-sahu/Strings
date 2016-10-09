package com.mediaplayer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mediaplayer.R;
import com.mediaplayer.activities.MediaPlayerActivity;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDBHelper;
import com.mediaplayer.utilities.MediaLibraryManager;

import java.util.ArrayList;

public class SongsFragment extends Fragment {
    private Context context;
    public static ListView trackListView;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        //checkPermissions();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        final ArrayList<Track> trackInfoList = MediaLibraryManager.getTrackInfoList();

        trackListView = (ListView) view.findViewById(R.id.listView);
        ListAdapter songsListAdapter = new SongsListAdapter(context, trackInfoList);
        trackListView.setAdapter(songsListAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
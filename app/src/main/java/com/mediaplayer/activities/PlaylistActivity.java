package com.mediaplayer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediaplayer.R;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.fragments.SongsFragment;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        try {
            TextView textView = (TextView) findViewById(R.id.emptyPlaylistMessage);

            int playlistID = getIntent().getIntExtra(MediaPlayerConstants.KEY_PLAYLIST_ID, 0);
            MediaplayerDAO dao = new MediaplayerDAO(this);
            ArrayList<Track> trackList = dao.getTracksForPlaylist(playlistID);
            MediaLibraryManager.setSelectedPlaylist(trackList);

            if(trackList.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
            } else {
                listView = (ListView) findViewById(R.id.listView);
                ListAdapter playlistAdapter = new SongsListAdapter(this, trackList);
                listView.setAdapter(playlistAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callMediaplayerActivity(View view) {
        int position = listView.getPositionForView(view);
        Track selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_USER, position);
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        startActivity(intent);
    }
}

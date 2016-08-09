package com.mediaplayer.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediaplayer.R;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDBHelper;
import com.mediaplayer.utilities.SQLConstants;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    private ArrayList<Track> playlist = new ArrayList<Track>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        try {
            MediaplayerDBHelper mDbHelper = new MediaplayerDBHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String args[] = {"1"};
            Cursor playlistsCursor = db.rawQuery(SQLConstants.SQL_SELECT_PLAYLIST_DETAIL, args);
            playlistsCursor.moveToFirst();

            while (!playlistsCursor.isAfterLast()) {
                Track track = new Track();
                track.setTrackTitle(playlistsCursor.getString(1));
                track.setTrackDuration(playlistsCursor.getInt(4));
                track.setAlbumName(playlistsCursor.getString(6));
                track.setArtistName(playlistsCursor.getString(7));
                track.setAlbumArt(playlistsCursor.getBlob(8));

                playlist.add(track);
                playlistsCursor.moveToNext();
            }

            playlistsCursor.close();

            if(playlist.isEmpty()) {
                TextView textView = (TextView) findViewById(R.id.emptyPlaylistMessage);
                textView.setVisibility(View.VISIBLE);
            } else {
                ListView listView = (ListView) findViewById(R.id.listView);
                ListAdapter playlistAdapter = new SongsListAdapter(this, playlist);
                listView.setAdapter(playlistAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

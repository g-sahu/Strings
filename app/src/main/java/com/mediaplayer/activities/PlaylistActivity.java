package com.mediaplayer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediaplayer.R;
import com.mediaplayer.adapters.PlaylistsAdapter;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.fragments.PlaylistsFragment;
import com.mediaplayer.fragments.SelectPlaylistDialogFragment;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.SQLConstants;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    private ListView listView;
    private FragmentManager supportFragmentManager;
    Track selectedTrack;
    Context homeContext;
    private static Playlist selectedPlaylist;
    private int playlistID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        try {
            homeContext = HomeActivity.getContext();
            supportFragmentManager = getSupportFragmentManager();

            TextView textView = (TextView) findViewById(R.id.emptyPlaylistMessage);
            Intent intent = getIntent();
            playlistID = intent.getIntExtra(MediaPlayerConstants.KEY_PLAYLIST_ID, 0);
            int playlistIndex = intent.getIntExtra(MediaPlayerConstants.KEY_PLAYLIST_INDEX, 0);
            selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(playlistIndex);
            MediaplayerDAO dao = new MediaplayerDAO(this);

            //Fetching all tracks for the selected playlist from database
            ArrayList<Track> trackList = dao.getTracksForPlaylist(playlistID);

            MediaLibraryManager.setSelectedPlaylist(trackList);
            MediaLibraryManager.sortTracklist(MediaPlayerConstants.KEY_PLAYLIST_OTHER);
            trackList = MediaLibraryManager.getSelectedPlaylist();

            if(trackList.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
            } else {
                listView = (ListView) findViewById(R.id.listView);
                ListAdapter playlistAdapter = new SongsListAdapter(this, trackList);
                listView.setAdapter(playlistAdapter);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void showSongsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_song_options, menu);
        MenuItem optionTwo = menu.findItem(R.id.addToFavourites);
        MenuItem optionThree = menu.findItem(R.id.removeSong);

        int position = listView.getPositionForView(view);
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_OTHER, position);

        //Checking if song is added to defualt playlist 'Favourites'
        if(selectedTrack.isFavSw() == SQLConstants.FAV_SW_YES) {
            optionTwo.setTitle(MediaPlayerConstants.TITLE_REMOVE_FROM_FAVOURITES);
        }

        if(playlistID == SQLConstants.PLAYLIST_ID_FAVOURITES) {
            optionThree.setVisible(false);
        } else {
            optionThree.setTitle(MediaPlayerConstants.TITLE_REMOVE_FROM_PLAYLIST);
        }

        popup.show();
    }

    //Show dialog to select playlists
    public void addToPlaylist(MenuItem menuItem) {
        DialogFragment selectPlaylistDialogFragment = new SelectPlaylistDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        selectPlaylistDialogFragment.setArguments(args);
        selectPlaylistDialogFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ADD_TO_PLAYLIST);
    }

    //Add or remove from favourites menu option
    public void addRemoveFavourites(MenuItem menuItem) {
        if (menuItem.getTitle().equals(MediaPlayerConstants.TITLE_ADD_TO_FAVOURITES)) {
            addToFavourites();
        } else {
            removeFromFavourites();
        }
    }

    //Add to favourites menu option
    private void addToFavourites() {
        ArrayList<Playlist> selectedPlaylists = new ArrayList<Playlist>();
        selectedPlaylists.add(MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES));
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.addToPlaylists(selectedPlaylists, selectedTrack);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.removeFromPlaylist(MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES), selectedTrack);

        //Removing track from selected playlist if it is default playlist 'Favourites'
        if(selectedPlaylist.getPlaylistID() == SQLConstants.PLAYLIST_ID_FAVOURITES) {
            MediaLibraryManager.removeTrack(MediaPlayerConstants.KEY_PLAYLIST_OTHER, selectedTrack.getCurrentTrackIndex());
        }

        //Updating list view adapter
        updatePlaylistsAdapter();
        updateSongsListAdapter();
    }

    //Remove from playlist menu option
    public void removeSong(MenuItem menuItem) {
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.removeFromPlaylist(selectedPlaylist, selectedTrack);

        //Removing track from selectedPlaylist
        MediaLibraryManager.removeTrack(MediaPlayerConstants.KEY_PLAYLIST_OTHER, selectedTrack.getCurrentTrackIndex());

        //Sorting selectedPlaylist
        MediaLibraryManager.sortTracklist(MediaPlayerConstants.KEY_PLAYLIST_OTHER);

        //Updating list view adapter
        updatePlaylistsAdapter();
        updateSongsListAdapter();
    }

    private void updateSongsListAdapter() {
        SongsListAdapter adapter = new SongsListAdapter(this, MediaLibraryManager.getSelectedPlaylist());
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(this, MediaLibraryManager.getPlaylistInfoList());
        ListView listView = PlaylistsFragment.listView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void callMediaplayerActivity(View view) {
        int position = listView.getPositionForView(view);
        Track selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_OTHER, position);
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, MediaPlayerConstants.KEY_PLAYLIST_OTHER);
        intent.putExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN, "PLAYLIST_ACTIVITY");
        intent.setAction(MediaPlayerConstants.PLAY);

        startActivity(intent);
    }
}

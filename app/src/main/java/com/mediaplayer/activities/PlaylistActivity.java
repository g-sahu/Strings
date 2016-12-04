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
import com.mediaplayer.dao.MediaPlayerDAO;
import com.mediaplayer.fragments.PlaylistsFragment;
import com.mediaplayer.fragments.SelectPlaylistDialogFragment;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.SQLConstants;
import com.mediaplayer.utilities.Utilities;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    private ListView listView;
    private FragmentManager supportFragmentManager;
    private Track selectedTrack;
    private Context homeContext;
    private static Playlist selectedPlaylist;
    private int playlistID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Intent intent;
        int playlistIndex;
        TextView playlistName, playlistInfo, emptyPlaylistMessage;
        String playlistTitle, infoText, text;

        try {
            homeContext = HomeActivity.getContext();
            supportFragmentManager = getSupportFragmentManager();

            playlistName = (TextView) findViewById(R.id.playlistName);
            playlistInfo = (TextView) findViewById(R.id.playlistDetails);
            emptyPlaylistMessage = (TextView) findViewById(R.id.emptyPlaylistMessage);

            intent = getIntent();
            playlistID = intent.getIntExtra(MediaPlayerConstants.KEY_PLAYLIST_ID, 0);
            playlistIndex = intent.getIntExtra(MediaPlayerConstants.KEY_PLAYLIST_INDEX, 0);
            selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(playlistIndex);
            playlistTitle = selectedPlaylist.getPlaylistName();
            infoText = getPlaylistDetails();

            MediaPlayerDAO dao = new MediaPlayerDAO(this);

            //Fetching all tracks for the selected playlist from database
            ArrayList<Track> trackList = dao.getTracksForPlaylist(playlistID);

            MediaLibraryManager.setSelectedPlaylist(trackList);
            MediaLibraryManager.sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_OTHER);
            trackList = MediaLibraryManager.getSelectedPlaylist();
            playlistName.setText(playlistTitle);
            playlistInfo.setText(infoText);

            if(trackList.isEmpty()) {
                emptyPlaylistMessage.setVisibility(View.VISIBLE);
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
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.TAG_PLAYLIST_OTHER, position);

        //Checking if song is added to defualt playlist 'Favourites'
        if (selectedTrack != null && selectedTrack.isFavSw() == SQLConstants.FAV_SW_YES) {
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
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
        dao.addToPlaylists(selectedPlaylists, selectedTrack);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
        dao.removeFromPlaylist(MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES), selectedTrack);

        //Sorting the trackList for the selected playlist
        MediaLibraryManager.sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_OTHER);

        //Removing track from selected playlist if it is default playlist 'Favourites'
        if(selectedPlaylist.getPlaylistID() == SQLConstants.PLAYLIST_ID_FAVOURITES) {
            MediaLibraryManager.removeTrack(MediaPlayerConstants.TAG_PLAYLIST_OTHER, selectedTrack.getCurrentTrackIndex());
        }

        //Updating list view adapter
        updatePlaylistsAdapter();
        updateSongsListAdapter();
    }

    //Remove from playlist menu option
    public void removeSong(MenuItem menuItem) {
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
        dao.removeFromPlaylist(selectedPlaylist, selectedTrack);

        //Removing track from selectedPlaylist
        MediaLibraryManager.removeTrack(MediaPlayerConstants.TAG_PLAYLIST_OTHER, selectedTrack.getCurrentTrackIndex());

        //Sorting selectedPlaylist
        MediaLibraryManager.sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_OTHER);

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
        TextView playlistInfo, emptyPlaylistMessage;

        if(MediaLibraryManager.isUserPlaylistEmpty()) {
            emptyPlaylistMessage = (TextView) findViewById(R.id.emptyPlaylistMessage);
            emptyPlaylistMessage.setVisibility(View.VISIBLE);
        }

        playlistInfo = (TextView) findViewById(R.id.playlistDetails);
        playlistInfo.setText(getPlaylistDetails());

        PlaylistsAdapter adapter = new PlaylistsAdapter(homeContext, MediaLibraryManager.getPlaylistInfoList());
        ListView listView = PlaylistsFragment.listView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void callMediaplayerActivity(View view) {
        int position = listView.getPositionForView(view);
        Track selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.TAG_PLAYLIST_OTHER, position);
        Intent intent = new Intent(this, MediaPlayerActivity.class);

        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, MediaPlayerConstants.TAG_PLAYLIST_OTHER);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_TITLE, selectedPlaylist.getPlaylistName());
        intent.putExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN, MediaPlayerConstants.TAG_PLAYLIST_ACTIVITY);
        intent.setAction(MediaPlayerConstants.PLAY);

        startActivity(intent);
    }

    private String getPlaylistDetails() {
        String text, infoText;
        int playlistSize;

        playlistSize = selectedPlaylist.getPlaylistSize();
        text = (playlistSize == SQLConstants.ONE) ? " song,\t" : " songs,\t";
        infoText = playlistSize + text + Utilities.milliSecondsToTimer(selectedPlaylist.getPlaylistDuration());

        return infoText;
    }
}

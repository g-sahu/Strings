package com.mediaplayer.strings.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediaplayer.strings.R;
import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.adapters.SongsListAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.dao.MediaPlayerDAO;
import com.mediaplayer.strings.fragments.PlaylistsFragment;
import com.mediaplayer.strings.fragments.SelectPlaylistDialogFragment;
import com.mediaplayer.strings.utilities.MediaLibraryManager;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;
import com.mediaplayer.strings.utilities.SQLConstants;
import com.mediaplayer.strings.utilities.Utilities;

import java.util.ArrayList;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class PlaylistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
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
        String playlistTitle, infoText;
        MediaPlayerDAO dao = null;

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

            dao = new MediaPlayerDAO(this);

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
                RecyclerView.Adapter playlistAdapter = new SongsListAdapter(this, trackList);
                recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                recyclerView.setAdapter(playlistAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    public void showSongsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_song_options, menu);
        MenuItem optionTwo = menu.findItem(R.id.addToFavourites);
        MenuItem optionThree = menu.findItem(R.id.removeSong);
        int position = recyclerView.getChildLayoutPosition(view);
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
        MediaPlayerDAO dao = null;

        try {
            selectedPlaylists.add(MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES));
            dao = new MediaPlayerDAO(this);
            dao.addToPlaylists(selectedPlaylists, selectedTrack);

            //Updating list view adapter
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        MediaPlayerDAO dao = null;

        try {
            dao = new MediaPlayerDAO(this);
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
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    //Remove from playlist menu option
    public void removeSong(MenuItem menuItem) {
        MediaPlayerDAO dao = null;

        try {
            dao = new MediaPlayerDAO(this);
            dao.removeFromPlaylist(selectedPlaylist, selectedTrack);

            //Removing track from selectedPlaylist
            MediaLibraryManager.removeTrack(MediaPlayerConstants.TAG_PLAYLIST_OTHER, selectedTrack.getCurrentTrackIndex());

            //Sorting selectedPlaylist
            MediaLibraryManager.sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_OTHER);

            //Updating list view adapter
            updatePlaylistsAdapter();
            updateSongsListAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
    }

    private void updateSongsListAdapter() {
        SongsListAdapter adapter = new SongsListAdapter(this, MediaLibraryManager.getSelectedPlaylist());
        recyclerView.setAdapter(adapter);
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
        RecyclerView listView = PlaylistsFragment.recyclerView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void callMediaplayerActivity(View view) {
        int position = recyclerView.getChildLayoutPosition(view);
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

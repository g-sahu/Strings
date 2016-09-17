package com.mediaplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.mediaplayer.R;
import com.mediaplayer.adapters.HomePagerAdapter;
import com.mediaplayer.adapters.PlaylistsAdapter;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.fragments.CreatePlaylistDialogFragment;
import com.mediaplayer.fragments.PlaylistsFragment;
import com.mediaplayer.fragments.SelectPlaylistDialogFragment;
import com.mediaplayer.fragments.SelectTrackDialogFragment;
import com.mediaplayer.fragments.SongsFragment;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.SQLConstants;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private int position;
    private static Track selectedTrack;
    private static Playlist selectedPlaylist;
    private static Playlist favouritesPlaylist;
    private FragmentManager supportFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        supportFragmentManager = getSupportFragmentManager();
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        FragmentPagerAdapter adapterViewPager = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        favouritesPlaylist = MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES);
    }

    public static Track getSelectedTrack() {
        return selectedTrack;
    }

    public static Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    //Show Songs pop-up menu options
    public void showSongsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_song_options, menu);
        MenuItem menuItem = menu.findItem(R.id.addToFavourites);

        ListView listView = SongsFragment.trackListView;
        position = listView.getPositionForView(view);
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_DEFAULT, position);

        //Checking if song is added to defualt playlist 'Favourites'
        if (selectedTrack.isFavouriteSw() == SQLConstants.FAV_SW_YES) {
            menuItem.setTitle(MediaPlayerConstants.TITLE_REMOVE_FROM_FAVOURITES);
        }

        popup.show();
    }

    //Show dialog to select playlists
    public void addToPlaylist(MenuItem menuItem) {
        DialogFragment newFragment = new SelectPlaylistDialogFragment();
        newFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ADD_TO_PLAYLIST);

        //Updating list view adapter
        updatePlaylistsAdapter();
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
        selectedPlaylists.add(favouritesPlaylist);
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.addToPlaylists(selectedPlaylists, selectedTrack);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.removeFromFavourites(favouritesPlaylist, selectedTrack);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Remove from library menu option
    public void removeFromLibrary(MenuItem menuItem) {
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.removeFromLibrary(selectedTrack);

        //Updating list view adapter
        updateSongsListAdapter();
    }

    //Show playlists pop-up menu options
    public void showPlaylistsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_playlist_options, menu);
        MenuItem renamePlaylist = menu.findItem(R.id.renamePlaylist);
        MenuItem deletePlaylist = menu.findItem(R.id.deletePlaylist);

        ListView listView = PlaylistsFragment.listView;
        position = listView.getPositionForView(view);
        selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(position);

        if(selectedPlaylist.getPlaylistID() == SQLConstants.PLAYLIST_ID_FAVOURITES) {
            renamePlaylist.setEnabled(false);
            deletePlaylist.setEnabled(false);
        }

        popup.show();
    }

    public void addTracksToPlaylist(MenuItem menuItem) {
        DialogFragment newFragment = new SelectTrackDialogFragment();
        newFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ADD_TRACKS);

        //Updating list view adapter
        updateSongsListAdapter();
    }

    //Rename playlist menu option
    public void renamePlaylist(MenuItem menuItem) {
        DialogFragment newFragment = new CreatePlaylistDialogFragment();
        Bundle args = new Bundle();

        args.putString(MediaPlayerConstants.KEY_PLAYLIST_TITLE, selectedPlaylist.getPlaylistName());
        args.putInt(MediaPlayerConstants.KEY_PLAYLIST_INDEX, selectedPlaylist.getPlaylistIndex());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), MediaPlayerConstants.TAG_RENAME_PLAYLIST);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Delete playlist menu option
    public void deletePlaylist(MenuItem menuItem) {
        MediaplayerDAO dao = new MediaplayerDAO(this);
        dao.deletePlaylist(selectedPlaylist);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    public void callMediaplayerActivity(View view) {
        ListView listView = SongsFragment.trackListView;
        position = listView.getPositionForView(view);
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_DEFAULT,  position);

        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        startActivity(intent);
    }

    public void callPlaylistActivity(View view) {
        ListView listView = PlaylistsFragment.listView;
        position = listView.getPositionForView(view);
        selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(position);
        int playlistID = selectedPlaylist.getPlaylistID();

        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_ID, playlistID);
        startActivity(intent);
    }

    private void updateSongsListAdapter() {
        SongsListAdapter adapter = new SongsListAdapter(this, MediaLibraryManager.getTrackInfoList());
        ListView listView = SongsFragment.trackListView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(this, MediaLibraryManager.getPlaylistInfoList());
        ListView listView = PlaylistsFragment.listView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
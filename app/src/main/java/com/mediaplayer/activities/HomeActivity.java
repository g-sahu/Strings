package com.mediaplayer.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.mediaplayer.R;
import com.mediaplayer.adapters.HomePagerAdapter;
import com.mediaplayer.adapters.PlaylistsAdapter;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaPlayerDAO;
import com.mediaplayer.fragments.AboutUsDialogFragment;
import com.mediaplayer.fragments.CreatePlaylistDialogFragment;
import com.mediaplayer.fragments.PlaylistsFragment;
import com.mediaplayer.fragments.SelectPlaylistDialogFragment;
import com.mediaplayer.fragments.SelectTrackDialogFragment;
import com.mediaplayer.fragments.SongsFragment;
import com.mediaplayer.services.MediaPlayerService;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.MessageConstants;
import com.mediaplayer.utilities.SQLConstants;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private int position;
    private static Track selectedTrack;
    private static Playlist selectedPlaylist;
    private static Playlist favouritesPlaylist;
    private FragmentManager supportFragmentManager;
    private static Context context;
    private static String LOG_TAG = "HomeActivity";
    private boolean isBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(LOG_TAG, "HomeActivity created");
        context = this;

        Intent intent = getIntent();

        if(intent.getBooleanExtra(MediaPlayerConstants.FLAG_LIBRARY_CHANGED, false)) {
            Toast toast = Toast.makeText(this, MessageConstants.LIBRARY_UPDATED, Toast.LENGTH_SHORT);
            toast.show();
        }

        supportFragmentManager = getSupportFragmentManager();
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        FragmentPagerAdapter adapterViewPager = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        //Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        favouritesPlaylist = MediaLibraryManager.getPlaylistByIndex(SQLConstants.PLAYLIST_INDEX_FAVOURITES);
    }

    public static Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public static Context getContext() {
        return context;
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
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_LIBRARY, position);

        //Checking if song is added to defualt playlist 'Favourites'
        if(selectedTrack != null && selectedTrack.isFavSw() == SQLConstants.FAV_SW_YES) {
            menuItem.setTitle(MediaPlayerConstants.TITLE_REMOVE_FROM_FAVOURITES);
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
        selectedPlaylists.add(favouritesPlaylist);
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
        dao.addToPlaylists(selectedPlaylists, selectedTrack);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
        dao.removeFromPlaylist(favouritesPlaylist, selectedTrack);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    //Remove from library menu option
    public void removeSong(MenuItem menuItem) {
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
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
        DialogFragment selectTrackDialogFragment = new SelectTrackDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, selectedPlaylist);
        selectTrackDialogFragment.setArguments(args);
        selectTrackDialogFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ADD_TRACKS);
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
        MediaPlayerDAO dao = new MediaPlayerDAO(this);
        dao.deletePlaylist(selectedPlaylist);

        //Updating list view adapter
        updatePlaylistsAdapter();
    }

    public void callMediaplayerActivity(View view) {
        ListView listView = SongsFragment.trackListView;
        position = listView.getPositionForView(view);
        selectedTrack = MediaLibraryManager.getTrackByIndex(MediaPlayerConstants.KEY_PLAYLIST_LIBRARY,  position);

        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, MediaPlayerConstants.KEY_PLAYLIST_LIBRARY);
        intent.setAction(MediaPlayerConstants.PLAY);
        intent.putExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN, MediaPlayerConstants.TAG_SONGS_LIST_VIEW);
        startActivity(intent);
    }

    public void callPlaylistActivity(View view) {
        ListView listView = PlaylistsFragment.listView;
        position = listView.getPositionForView(view);
        selectedPlaylist = MediaLibraryManager.getPlaylistByIndex(position);
        int playlistID = selectedPlaylist.getPlaylistID();

        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_ID, playlistID);
        intent.putExtra(MediaPlayerConstants.KEY_PLAYLIST_INDEX, position);
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

    public void showAppPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_app_options, menu);
        popup.show();
    }

    public void rateApp(MenuItem item) {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    public void shareApp(MenuItem item) {
        String link = "http://play.google.com/store/apps/details?id=" + getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, link);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void aboutUs(MenuItem item) {
        DialogFragment aboutUsDialogFragment = new AboutUsDialogFragment();
        aboutUsDialogFragment.show(supportFragmentManager, MediaPlayerConstants.TAG_ABOUT_US);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "Back button pressed");
        isBackPressed = true;
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(!isBackPressed) {
            MediaPlayerActivity.stopProgressBar();

            Intent intent = new Intent(this, MediaPlayerService.class);
            stopService(intent);
        }

        Log.d(LOG_TAG, "HomeActivity destroyed");
    }
}
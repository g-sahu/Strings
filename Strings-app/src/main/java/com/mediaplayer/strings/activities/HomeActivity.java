package com.mediaplayer.strings.activities;

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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.mediaplayer.strings.R;
import com.mediaplayer.strings.adapters.HomePagerAdapter;
import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.dao.MediaPlayerDAO;
import com.mediaplayer.strings.fragments.AboutUsDialogFragment;
import com.mediaplayer.strings.fragments.CreatePlaylistDialogFragment;
import com.mediaplayer.strings.fragments.SelectPlaylistDialogFragment;
import com.mediaplayer.strings.fragments.SelectTrackDialogFragment;
import com.mediaplayer.strings.services.MediaPlayerService;

import java.util.ArrayList;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.net.Uri.parse;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;
import static com.mediaplayer.strings.R.id;
import static com.mediaplayer.strings.R.id.addToFavourites;
import static com.mediaplayer.strings.R.id.tab_layout;
import static com.mediaplayer.strings.R.id.view_pager;
import static com.mediaplayer.strings.R.layout.activity_home;
import static com.mediaplayer.strings.dao.MediaPlayerDAO.UpdateTracksTask;
import static com.mediaplayer.strings.fragments.PlaylistsFragment.recyclerView;
import static com.mediaplayer.strings.fragments.SongsFragment.trackListView;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getPlaylistByIndex;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getPlaylistInfoList;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getTrackByIndex;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.*;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_404;
import static com.mediaplayer.strings.utilities.MessageConstants.LIBRARY_UPDATED;
import static com.mediaplayer.strings.utilities.SQLConstants.FAV_SW_YES;
import static com.mediaplayer.strings.utilities.SQLConstants.PLAYLIST_ID_FAVOURITES;
import static com.mediaplayer.strings.utilities.SQLConstants.PLAYLIST_INDEX_FAVOURITES;

public class HomeActivity extends AppCompatActivity {
    private final static String LOG_TAG = "HomeActivity";
    private static Track selectedTrack;
    private static Playlist selectedPlaylist, favouritesPlaylist;
    private FragmentManager supportFragmentManager;
    private static Context context;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_home);
        Log.d(LOG_TAG, "HomeActivity created");

        context = this;
        setVolumeControlStream(STREAM_MUSIC);
        Intent intent = getIntent();

        if(intent.getBooleanExtra(FLAG_LIBRARY_CHANGED, false)) {
            Toast toast = makeText(this, LIBRARY_UPDATED, LENGTH_LONG);
            toast.show();
        }

        supportFragmentManager = getSupportFragmentManager();
        ViewPager viewPager = findViewById(view_pager);
        FragmentPagerAdapter adapterViewPager = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        //Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        favouritesPlaylist = getPlaylistByIndex(PLAYLIST_INDEX_FAVOURITES);
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
        MenuItem menuItem = menu.findItem(addToFavourites);

        RecyclerView recyclerView = trackListView;
        View parent = (View) view.getParent();
        position = recyclerView.getChildLayoutPosition(parent);
        selectedTrack = getTrackByIndex(TAG_PLAYLIST_LIBRARY, position);

        //Checking if song is added to default playlist 'Favourites'
        if(selectedTrack != null && selectedTrack.isFavSw() == FAV_SW_YES) {
            menuItem.setTitle(TITLE_REMOVE_FROM_FAVOURITES);
        }

        popup.show();
    }

    //Show dialog to select playlists
    public void addToPlaylist(MenuItem menuItem) {
        DialogFragment selectPlaylistDialogFragment = new SelectPlaylistDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable(KEY_SELECTED_TRACK, selectedTrack);
        selectPlaylistDialogFragment.setArguments(args);
        selectPlaylistDialogFragment.show(supportFragmentManager, TAG_ADD_TO_PLAYLIST);
    }

    //Add or remove from favourites menu option
    public void addRemoveFavourites(MenuItem menuItem) {
        if (menuItem.getTitle().equals(TITLE_ADD_TO_FAVOURITES)) {
            addToFavourites();
        } else {
            removeFromFavourites();
        }
    }

    //Add to favourites menu option
    private void addToFavourites() {
        ArrayList<Playlist> selectedPlaylists = new ArrayList<>();

        try (MediaPlayerDAO dao = new MediaPlayerDAO(this)) {
            selectedPlaylists.add(favouritesPlaylist);
            dao.addToPlaylists(selectedPlaylists, selectedTrack);
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }
    }

    //Remove from favourites menu option
    private void removeFromFavourites() {
        try (MediaPlayerDAO dao = new MediaPlayerDAO(this)) {
            dao.removeFromPlaylist(favouritesPlaylist, selectedTrack);
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }
    }

    //Remove from library menu option
    public void removeSong(MenuItem menuItem) {
        MediaPlayerDAO dao = null;

        try {
            dao = new MediaPlayerDAO(this);
            UpdateTracksTask task = new UpdateTracksTask(this);
            task.execute(selectedTrack);
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                //dao.closeConnection();
            }
        }
    }

    //Show playlists pop-up menu options
    public void showPlaylistsPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_playlist_options, menu);
        MenuItem renamePlaylist = menu.findItem(id.renamePlaylist);
        MenuItem deletePlaylist = menu.findItem(id.deletePlaylist);

        RecyclerView listView = recyclerView;
        View parent = (View) view.getParent();
        position = listView.getChildLayoutPosition(parent);
        selectedPlaylist = getPlaylistByIndex(position);

        if(selectedPlaylist.getPlaylistID() == PLAYLIST_ID_FAVOURITES) {
            renamePlaylist.setEnabled(false);
            deletePlaylist.setEnabled(false);
        }

        popup.show();
    }

    public void addTracksToPlaylist(MenuItem menuItem) {
        DialogFragment selectTrackDialogFragment = new SelectTrackDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_SELECTED_PLAYLIST, selectedPlaylist);
        selectTrackDialogFragment.setArguments(args);
        selectTrackDialogFragment.show(supportFragmentManager, TAG_ADD_TRACKS);
    }

    //Rename playlist menu option
    public void renamePlaylist(MenuItem menuItem) {
        DialogFragment newFragment = new CreatePlaylistDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_PLAYLIST_TITLE, selectedPlaylist.getPlaylistName());
        args.putInt(KEY_PLAYLIST_INDEX, selectedPlaylist.getPlaylistIndex());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), TAG_RENAME_PLAYLIST);
        updatePlaylistsAdapter();
    }

    //Delete playlist menu option
    public void deletePlaylist(MenuItem menuItem) {
        try (MediaPlayerDAO dao = new MediaPlayerDAO(this)) {
            dao.deletePlaylist(selectedPlaylist);
            updatePlaylistsAdapter();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }
    }

    public void callMediaplayerActivity(View view) {
        RecyclerView recyclerView = trackListView;
        position = recyclerView.getChildLayoutPosition(view);
        selectedTrack = getTrackByIndex(TAG_PLAYLIST_LIBRARY,  position);

        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.setAction(PLAY);
        intent.putExtra(KEY_SELECTED_TRACK, selectedTrack);
        intent.putExtra(KEY_SELECTED_PLAYLIST, TAG_PLAYLIST_LIBRARY);
        intent.putExtra(KEY_PLAYLIST_TITLE, TITLE_LIBRARY);
        intent.putExtra(KEY_TRACK_ORIGIN, TAG_SONGS_LIST_VIEW);
        startActivity(intent);
    }

    public void callPlaylistActivity(View view) {
        RecyclerView listView = recyclerView;
        position = listView.getChildLayoutPosition(view);
        selectedPlaylist = getPlaylistByIndex(position);
        int playlistID = selectedPlaylist.getPlaylistID();

        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra(KEY_PLAYLIST_ID, playlistID);
        intent.putExtra(KEY_PLAYLIST_INDEX, position);
        startActivity(intent);
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(this, getPlaylistInfoList());
        RecyclerView listView = recyclerView;
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
        Uri uri = parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(myAppLinkToMarket);
        } catch(ActivityNotFoundException e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
            makeText(this, ERROR_404, LENGTH_LONG).show();
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
        aboutUsDialogFragment.show(supportFragmentManager, TAG_ABOUT_US);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "Back button pressed");
        moveTaskToBack(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MediaPlayerService.class);
        stopService(intent);
        Log.d(LOG_TAG, "HomeActivity destroyed");
    }
}

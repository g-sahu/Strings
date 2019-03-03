package com.mediaplayer.strings.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.dao.MediaPlayerDAO;

import java.util.ArrayList;

import static android.support.v7.app.AlertDialog.Builder;
import static com.mediaplayer.strings.fragments.PlaylistsFragment.recyclerView;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getPlaylistInfoList;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.*;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_NO_PLAYLIST;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_NO_PLAYLIST_CREATED;
import static com.mediaplayer.strings.utilities.SQLConstants.PLAYLIST_ID_FAVOURITES;
import static com.mediaplayer.strings.utilities.SQLConstants.ZERO;
import static com.mediaplayer.strings.utilities.Utilities.isNotNullOrEmpty;

public class SelectPlaylistDialogFragment extends DialogFragment {
    private ArrayList<Playlist> playlistsInLibrary = getPlaylistInfoList();
    private ArrayList<Playlist> selectedPlaylists;
    private Track selectedTrack;
    private Context context;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        MediaPlayerDAO dao = new MediaPlayerDAO(context);

        try {
            context = getContext();
            Bundle args = getArguments();
            selectedTrack = (Track) args.getSerializable(KEY_SELECTED_TRACK);

            //Checking if the user has created any custom playlist
            if(playlistsInLibrary.size() > 1) {
                ArrayList<Integer> addedToPlaylists = dao.getPlaylistsForTrack(selectedTrack.getTrackID());
                int addedToPlaylistsCount = addedToPlaylists != null ? addedToPlaylists.size() : ZERO;
                final ArrayList<Playlist> playlistsToDisplay = new ArrayList<>();

                //Iterating playlists in library to remove playlists to which track is already added
                for (Playlist playlist : playlistsInLibrary) {
                    if((playlist.getPlaylistID() != PLAYLIST_ID_FAVOURITES) &&
                       (addedToPlaylistsCount == ZERO || !addedToPlaylists.contains(playlist.getPlaylistID()))) {
                        playlistsToDisplay.add(playlist);
                    }
                }

                selectedPlaylists = new ArrayList<>();
                String[] list = new String[playlistsToDisplay.size()];
                int listLength = list.length;
                int c = 0;

                builder.setTitle(TITLE_SELECT_PLAYLIST);

                if(listLength != 0) {
                    for (Playlist playlist : playlistsToDisplay) {
                        list[c++] = playlist.getPlaylistName();
                    }

                    //Setting multi-select list
                    builder.setMultiChoiceItems(list, null, (dialog, which, isChecked) -> {
                        Playlist playlist1 = playlistsToDisplay.get(which);

                        if(isChecked) {
                            selectedPlaylists.add(playlist1);
                        } else if (selectedPlaylists.contains(playlist1)) {
                            selectedPlaylists.remove(playlist1);
                        }
                    });

                    //Setting listener for 'OK' button
                    builder.setPositiveButton(OK, (dialog, id) -> {
                        if(isNotNullOrEmpty(selectedPlaylists)) {
                            dao.addToPlaylists(selectedPlaylists, selectedTrack);
                            updatePlaylistsAdapter();
                            playlistsToDisplay.removeAll(selectedPlaylists);
                        }
                    });

                    builder.setNegativeButton(CANCEL, (dialog, id) -> {});
                } else {
                    builder.setMessage(ERROR_NO_PLAYLIST);
                    builder.setPositiveButton(OK, (dialog, id) -> {});
                }
            } else {
                builder.setTitle(TITLE_ERROR);
                builder.setMessage(ERROR_NO_PLAYLIST_CREATED);
                builder.setPositiveButton(OK, (dialog, id) -> {});
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            dao.closeConnection();
        }

        return builder.create();
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(context, getPlaylistInfoList());
        RecyclerView listView = recyclerView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}

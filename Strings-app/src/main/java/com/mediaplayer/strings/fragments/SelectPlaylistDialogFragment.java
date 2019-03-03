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
import java.util.Iterator;

import static android.support.v7.app.AlertDialog.Builder;
import static com.mediaplayer.strings.fragments.PlaylistsFragment.recyclerView;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getPlaylistInfoList;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.*;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_NO_PLAYLIST;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_NO_PLAYLIST_CREATED;
import static com.mediaplayer.strings.utilities.SQLConstants.PLAYLIST_ID_FAVOURITES;
import static com.mediaplayer.strings.utilities.SQLConstants.ZERO;

public class SelectPlaylistDialogFragment extends DialogFragment {
    private ArrayList<Playlist> playlistsInLibrary = getPlaylistInfoList();
    private ArrayList<Playlist> selectedPlaylists;
    private Track selectedTrack;
    private Context context;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = null;
        MediaPlayerDAO dao = null;
        ArrayList<Integer> addedToPlaylists;
        final ArrayList<Playlist> playlistsToDisplay;
        Iterator<Playlist> playlistsIterator;
        String list[];
        Playlist playlist;
        int playlistID, playlistCount = playlistsInLibrary.size(), addedToPlaylistsCount, c = 0, listLength;

        try {
            context = getContext();
            builder = new Builder(getActivity());
            Bundle args = getArguments();
            selectedTrack = (Track) args.getSerializable(KEY_SELECTED_TRACK);

            //Checking if playlist count > 1 i.e. the user has created any custom playlist
            if(playlistCount > 1) {
                dao = new MediaPlayerDAO(context);

                //Getting the playlists to which the track is already added
                addedToPlaylists = dao.getPlaylistsForTrack(selectedTrack.getTrackID());
                addedToPlaylistsCount = addedToPlaylists != null ? addedToPlaylists.size() : ZERO;

                playlistsToDisplay = new ArrayList<>();
                playlistsIterator = playlistsInLibrary.iterator();

                //Iterating playlists in library to remove playlists to which track is already added
                while(playlistsIterator.hasNext()) {
                    playlist = playlistsIterator.next();
                    playlistID = playlist.getPlaylistID();

                    if((playlistID != PLAYLIST_ID_FAVOURITES) &&
                       (addedToPlaylistsCount == ZERO || !addedToPlaylists.contains(playlistID))) {
                        playlistsToDisplay.add(playlist);
                    }
                }

                selectedPlaylists = new ArrayList<>();
                list = new String[playlistsToDisplay.size()];
                listLength = list.length;

                //Setting dialog box title
                builder.setTitle(TITLE_SELECT_PLAYLIST);

                if(listLength != 0) {
                    playlistsIterator = playlistsToDisplay.iterator();

                    //Adding playlists to multichoice items list in dialog
                    while(playlistsIterator.hasNext()) {
                        playlist = playlistsIterator.next();
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
                        MediaPlayerDAO dao1 = null;

                        if(!selectedPlaylists.isEmpty()) {
                            try {
                                dao1 = new MediaPlayerDAO(context);

                                //Add track to selected playlists
                                dao1.addToPlaylists(selectedPlaylists, selectedTrack);
                            } catch(Exception e) {
                                Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                                //Utilities.reportCrash(e);
                            } finally {
                                if(dao1 != null) {
                                    dao1.closeConnection();
                                }
                            }

                            //Updating playlist adapter
                            updatePlaylistsAdapter();

                            //Removing added playlists from playlistsInLibrary
                            playlistsToDisplay.removeAll(selectedPlaylists);
                        }
                    });

                    //Setting listener for 'Cancel' button
                    builder.setNegativeButton(CANCEL, (dialog, id) -> {
                        //Do nothing
                    });
                } else {
                    builder.setMessage(ERROR_NO_PLAYLIST);
                    builder.setPositiveButton(OK, (dialog, id) -> {
                        //Do nothing
                    });
                }
            } else {
                builder.setTitle(TITLE_ERROR);
                builder.setMessage(ERROR_NO_PLAYLIST_CREATED);
                builder.setPositiveButton(OK, (dialog, id) -> {
                    //Do nothing
                });
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
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

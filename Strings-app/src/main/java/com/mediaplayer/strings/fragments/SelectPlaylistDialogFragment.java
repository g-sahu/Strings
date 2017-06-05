package com.mediaplayer.strings.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.dao.MediaPlayerDAO;
import com.mediaplayer.strings.utilities.MediaLibraryManager;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;
import com.mediaplayer.strings.utilities.MessageConstants;
import com.mediaplayer.strings.utilities.SQLConstants;
import com.mediaplayer.strings.utilities.Utilities;

import java.util.ArrayList;
import java.util.Iterator;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class SelectPlaylistDialogFragment extends DialogFragment {
    private ArrayList<Playlist> playlistsInLibrary = MediaLibraryManager.getPlaylistInfoList();
    private ArrayList<Playlist> selectedPlaylists;
    private Track selectedTrack;
    private Context context;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = null;
        MediaPlayerDAO dao = null;
        ArrayList<Integer> addedToPlaylists;
        final ArrayList<Playlist> playlistsToDisplay;
        Iterator<Playlist> playlistsIterator;
        String list[];
        Playlist playlist;
        int playlistID, playlistCount = playlistsInLibrary.size(), addedToPlaylistsCount, c = 0, listLength;

        try {
            context = getContext();
            builder = new AlertDialog.Builder(getActivity());
            Bundle args = getArguments();
            selectedTrack = (Track) args.getSerializable(MediaPlayerConstants.KEY_SELECTED_TRACK);

            //Checking if playlist count > 1 i.e. the user has created any custom playlist
            if(playlistCount > 1) {
                dao = new MediaPlayerDAO(context);

                //Getting the playlists to which the track is already added
                addedToPlaylists = dao.getPlaylistsForTrack(selectedTrack.getTrackID());

                if(addedToPlaylists != null) {
                    addedToPlaylistsCount = addedToPlaylists.size();
                } else {
                    addedToPlaylistsCount = 0;
                }

                playlistsToDisplay = new ArrayList<Playlist>();
                playlistsIterator = playlistsInLibrary.iterator();

                //Iterating playlists in library to remove playlists to which track is already added
                while(playlistsIterator.hasNext()) {
                    playlist = playlistsIterator.next();
                    playlistID = playlist.getPlaylistID();

                    if((playlistID != SQLConstants.PLAYLIST_ID_FAVOURITES) &&
                            (addedToPlaylistsCount == SQLConstants.ZERO || !addedToPlaylists.contains(playlistID))) {
                        playlistsToDisplay.add(playlist);
                    }
                }

                selectedPlaylists = new ArrayList<Playlist>();
                list = new String[playlistsToDisplay.size()];
                listLength = list.length;

                //Setting dialog box title
                builder.setTitle(MediaPlayerConstants.TITLE_SELECT_PLAYLIST);

                if(listLength != 0) {
                    playlistsIterator = playlistsToDisplay.iterator();

                    //Adding playlists to multichoice items list in dialog
                    while(playlistsIterator.hasNext()) {
                        playlist = playlistsIterator.next();
                        list[c++] = playlist.getPlaylistName();
                    }

                    //Setting multi-select list
                    builder.setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            Playlist playlist = playlistsToDisplay.get(which);

                            if(isChecked) {
                                selectedPlaylists.add(playlist);
                            } else if (selectedPlaylists.contains(playlist)) {
                                selectedPlaylists.remove(playlist);
                            }
                        }
                    });

                    //Setting listener for 'OK' button
                    builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            MediaPlayerDAO dao = null;

                            if(!selectedPlaylists.isEmpty()) {
                                try {
                                    dao = new MediaPlayerDAO(context);

                                    //Add track to selected playlists
                                    dao.addToPlaylists(selectedPlaylists, selectedTrack);
                                } catch(Exception e) {
                                    Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                                    Utilities.reportCrash(e);
                                } finally {
                                    if(dao != null) {
                                        dao.closeConnection();
                                    }
                                }

                                //Updating playlist adapter
                                updatePlaylistsAdapter();

                                //Removing added playlists from playlistsInLibrary
                                playlistsToDisplay.removeAll(selectedPlaylists);
                            }
                        }
                    });

                    //Setting listener for 'Cancel' button
                    builder.setNegativeButton(MediaPlayerConstants.CANCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    });
                } else {
                    builder.setMessage(MessageConstants.ERROR_NO_PLAYLIST);
                    builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    });
                }
            } else {
                builder.setTitle(MediaPlayerConstants.TITLE_ERROR);
                builder.setMessage(MessageConstants.ERROR_NO_PLAYLIST_CREATED);
                builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                });
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }

        return builder.create();
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(context, MediaLibraryManager.getPlaylistInfoList());
        RecyclerView listView = PlaylistsFragment.recyclerView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}

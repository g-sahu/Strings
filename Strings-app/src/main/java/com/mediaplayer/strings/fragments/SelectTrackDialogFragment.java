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
import com.mediaplayer.strings.activities.HomeActivity;
import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.dao.MediaPlayerDAO;
import com.mediaplayer.strings.utilities.MediaLibraryManager;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;
import com.mediaplayer.strings.utilities.MessageConstants;
import com.mediaplayer.strings.utilities.SQLConstants;

import java.util.ArrayList;
import java.util.Iterator;

public class SelectTrackDialogFragment extends DialogFragment {
    private Context context;
    private ArrayList<Track> selectedTracks;
    private ArrayList<Track> tracksInLibrary = MediaLibraryManager.getTrackInfoList();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<Integer> tracksInPlaylist;
        final ArrayList<Track> tracksToDisplay;
        Iterator<Track> tracksIterator;
        String list[];
        Track track;
        int trackID;
        AlertDialog.Builder builder = null;
        MediaPlayerDAO dao = null;
        int trackInPlaylistSize, c = 0, listLength;

        try {
            context = getContext();
            builder = new AlertDialog.Builder(getActivity());
            Bundle args = getArguments();
            Playlist selectedPlaylist = (Playlist) args.getSerializable(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);

            //Checking if there are any tracks in the library
            if(tracksInLibrary != null && !tracksInLibrary.isEmpty()) {
                dao = new MediaPlayerDAO(context);
                tracksInPlaylist = dao.getTrackIDsForPlaylist(selectedPlaylist.getPlaylistID());

                if(tracksInPlaylist != null && !tracksInPlaylist.isEmpty()) {
                    trackInPlaylistSize = tracksInPlaylist.size();
                } else {
                    trackInPlaylistSize = 0;
                }

                //Creating list of tracks to display in multiselect dialog
                tracksToDisplay = new ArrayList<>();

                //Iterating tracks in library to remove tracks already added to playlist
                tracksIterator = tracksInLibrary.iterator();

                while(tracksIterator.hasNext()) {
                    track = tracksIterator.next();
                    trackID = track.getTrackID();

                    if(trackInPlaylistSize == SQLConstants.ZERO || !tracksInPlaylist.contains(trackID)) {
                        tracksToDisplay.add(track);
                    }
                }

                selectedTracks = new ArrayList<>();
                list = new String[tracksToDisplay.size()];
                listLength = list.length;

                //Setting the title of the dialog window
                builder.setTitle(MediaPlayerConstants.TITLE_SELECT_TRACKS);

                if(listLength != 0) {
                    tracksIterator = tracksToDisplay.iterator();

                    //Adding tracks to multichoice items list in dialog
                    while(tracksIterator.hasNext()) {
                        track = tracksIterator.next();
                        list[c++] = track.getTrackTitle();
                    }

                    builder.setMultiChoiceItems(list, null, (dialog, which, isChecked) -> {
                        Track track1 = tracksToDisplay.get(which);

                        if(isChecked) {
                            selectedTracks.add(track1);
                        } else if (selectedTracks.contains(track1)) {
                            selectedTracks.remove(track1);
                        }
                    });

                    builder.setPositiveButton(MediaPlayerConstants.OK, (dialog, id) -> {
                        MediaPlayerDAO dao1 = null;

                        if(!selectedTracks.isEmpty()) {
                            try {
                                dao1 = new MediaPlayerDAO(getContext());

                                //Add track to selected playlists
                                dao1.addTracks(selectedTracks, HomeActivity.getSelectedPlaylist());
                            } catch(Exception e) {
                                Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
                                //Utilities.reportCrash(e);
                            } finally {
                                if(dao1 != null) {
                                    dao1.closeConnection();
                                }
                            }

                            //Updating list view adapter
                            updatePlaylistsAdapter();

                            //Removing added tracks from tracksInLibrary
                            tracksToDisplay.removeAll(selectedTracks);
                        }
                    });

                    builder.setNegativeButton(MediaPlayerConstants.CANCEL, (dialog, id) -> {
                        //Do nothing
                    });
                } else {
                    builder.setMessage(MessageConstants.ERROR_NO_TRACK);
                    builder.setPositiveButton(MediaPlayerConstants.OK, (dialog, id) -> {
                        //Do nothing
                    });
                }
            } else {
                builder.setTitle(MediaPlayerConstants.TITLE_ERROR);
                builder.setMessage(MessageConstants.ERROR_NO_TRACKS_ADDED);
                builder.setPositiveButton(MediaPlayerConstants.OK, (dialog, id) -> {
                    //Do nothing
                });
            }
        } catch(Exception e) {
            Log.e(MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
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

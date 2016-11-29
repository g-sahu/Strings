package com.mediaplayer.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import com.mediaplayer.activities.HomeActivity;
import com.mediaplayer.adapters.PlaylistsAdapter;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaPlayerDAO;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.MessageConstants;
import com.mediaplayer.utilities.SQLConstants;

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

        context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        Playlist selectedPlaylist = (Playlist) args.getSerializable(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        int size = tracksInLibrary.size(), trackInPlaylistSize, c = 0, listLength;

        //Checking if playlist size > 1 i.e. the user has created any custom playlist
        if(!tracksInLibrary.isEmpty()) {
            MediaPlayerDAO dao = new MediaPlayerDAO(context);
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

            selectedTracks = new ArrayList<Track>();
            list = new String[tracksToDisplay.size()];
            listLength = list.length;

            //Setting the title of the dialog window
            builder.setTitle(MediaPlayerConstants.TITLE_SELECT_TRACKS);

            if(listLength != 0) {
                tracksIterator = tracksToDisplay.iterator();

                while(tracksIterator.hasNext()) {
                    track = tracksIterator.next();
                    list[c++] = track.getTrackTitle();
                }

                builder.setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Track track = tracksToDisplay.get(which);

                        if(isChecked) {
                            selectedTracks.add(track);
                        } else if(selectedTracks.contains(track)) {
                            selectedTracks.remove(track);
                        }
                    }
                });

                builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(!selectedTracks.isEmpty()) {
                            //Add track to selected playlists
                            MediaPlayerDAO dao = new MediaPlayerDAO(getContext());
                            dao.addTracks(selectedTracks, HomeActivity.getSelectedPlaylist());

                            //Updating list view adapter
                            updatePlaylistsAdapter();

                            //Removing added tracks from tracksInLibrary
                            tracksToDisplay.removeAll(selectedTracks);
                        }
                    }
                });

                builder.setNegativeButton(MediaPlayerConstants.CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                });
            } else {
                builder.setMessage(MessageConstants.ERROR_NO_TRACK);
                builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                });
            }
        } else {
            builder.setTitle(MediaPlayerConstants.TITLE_ERROR);
            builder.setMessage(MessageConstants.ERROR_NO_TRACKS_ADDED);
            builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //Do nothing
                }
            });
        }

        return builder.create();
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(context, MediaLibraryManager.getPlaylistInfoList());
        ListView listView = PlaylistsFragment.listView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
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
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.MessageConstants;

import java.util.ArrayList;
import java.util.Iterator;

public class SelectTrackDialogFragment extends DialogFragment {
    private Context context;
    private ArrayList<Track> selectedTracks;
    private ArrayList<Track> trackInfoList = MediaLibraryManager.getTrackInfoList();
    private Playlist selectedPlaylist;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        selectedPlaylist = (Playlist) args.getSerializable(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        ArrayList<Integer> addedTracks;
        int size = trackInfoList.size(), addedTracksSize, c = 0, listLength;
        String list[];
        Track track;
        int trackID;

        //Checking if playlist size > 1 i.e. the user has created any custom playlist
        if(!trackInfoList.isEmpty()) {
            MediaplayerDAO dao = new MediaplayerDAO(context);
            addedTracks = dao.getTrackIDsForPlaylist(selectedPlaylist.getPlaylistID());

            if(addedTracks != null) {
                addedTracksSize = addedTracks.size();
            } else {
                addedTracksSize = 0;
            }

            selectedTracks = new ArrayList<Track>();
            list = new String[size - addedTracksSize];
            listLength = list.length;

            builder.setTitle(MediaPlayerConstants.TITLE_SELECT_TRACKS);

            if(listLength != 0) {
                Iterator<Track> tracksIterator = trackInfoList.iterator();

                while(tracksIterator.hasNext()) {
                    track = tracksIterator.next();
                    trackID = track.getTrackID();

                    if(addedTracksSize == 0 || !addedTracks.contains(trackID)) {
                        list[c++] = track.getTrackTitle();
                    }
                }

                builder.setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Track track = trackInfoList.get(which);

                        if(isChecked) {
                            selectedTracks.add(track);
                        } else if (selectedTracks.contains(track)) {
                            selectedTracks.remove(track);
                        }
                    }
                });

                builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(!selectedTracks.isEmpty()) {
                            //Add track to selected playlists
                            MediaplayerDAO dao = new MediaplayerDAO(getContext());
                            dao.addTracks(selectedTracks, HomeActivity.getSelectedPlaylist());

                            //Updating list view adapter
                            updatePlaylistsAdapter();
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
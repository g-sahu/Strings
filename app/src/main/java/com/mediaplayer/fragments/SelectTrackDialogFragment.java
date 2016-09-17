package com.mediaplayer.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.mediaplayer.activities.HomeActivity;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;

import java.util.ArrayList;
import java.util.Iterator;

public class SelectTrackDialogFragment extends DialogFragment {
    private ArrayList<Track> selectedTracks;
    private ArrayList<Track> trackInfoList = MediaLibraryManager.getTrackInfoList();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int size = trackInfoList.size();
        String list[] = new String[size];
        selectedTracks = new ArrayList<Track>();
        int c = 0;

        Iterator<Track> trackIterator = trackInfoList.iterator();
        while(trackIterator.hasNext()) {
            Track track = trackIterator.next();
            list[c] = track.getTrackTitle();
            c++;
        }

        builder.setTitle(MediaPlayerConstants.TITLE_SELECT_TRACKS);
        builder.setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Track track = trackInfoList.get(which);

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
                    MediaplayerDAO dao = new MediaplayerDAO(getContext());
                    dao.addTracks(selectedTracks, HomeActivity.getSelectedPlaylist());
                }
            }
        });

        builder.setNegativeButton(MediaPlayerConstants.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Do nothing
            }
        });

        return builder.create();
    }
}

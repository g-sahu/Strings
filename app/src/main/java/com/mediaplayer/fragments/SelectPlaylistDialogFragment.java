package com.mediaplayer.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.mediaplayer.activities.HomeActivity;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.MessageConstants;
import com.mediaplayer.utilities.SQLConstants;

import java.util.ArrayList;

public class SelectPlaylistDialogFragment extends DialogFragment {
    private ArrayList<Playlist> selectedPlaylists;
    private ArrayList<Playlist> playlistInfoList = MediaLibraryManager.getPlaylistInfoList();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int size = playlistInfoList.size();
        String list[];
        int c = 0;

        //Checking if playlist size > 1 i.e. the user has created any custom playlist
        if(size > 1) {
            selectedPlaylists = new ArrayList<Playlist>();
            list = new String[size-1];

            //Adding user created playlists from playlistInfoList to multichoice items list in dialog
            for(int i=0; i<size; i++) {
                Playlist playlist = playlistInfoList.get(i);

                //Skipping default playlist 'Favourites' from this list
                if(playlist.getPlaylistID() != SQLConstants.PLAYLIST_ID_FAVOURITES) {
                    list[c++] = playlist.getPlaylistName();
                }
            }

            builder.setTitle(MediaPlayerConstants.TITLE_SELECT_PLAYLIST);
            builder.setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    Playlist playlist = playlistInfoList.get(which + 1);    //+1 for skipping default playlist 'Favourites'

                    if(isChecked) {
                        selectedPlaylists.add(playlist);
                    } else if(selectedPlaylists.contains(playlist)) {
                        selectedPlaylists.remove(playlist);
                    }
                }
            });

            builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    if(!selectedPlaylists.isEmpty()) {
                        //Add track to selected playlists
                        MediaplayerDAO dao = new MediaplayerDAO(getContext());
                        dao.addToPlaylists(selectedPlaylists, HomeActivity.getSelectedTrack());
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
            //// TODO: 23-Aug-16 Update this dialog's design. Add a button to create playlist
            builder.setTitle(MediaPlayerConstants.TITLE_ERROR);
            builder.setMessage(MessageConstants.NO_PLAYLIST_CREATED);
            builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //Do nothing
                }
            });
        }

        return builder.create();
    }
}

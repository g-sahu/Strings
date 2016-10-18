package com.mediaplayer.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import com.mediaplayer.adapters.PlaylistsAdapter;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDAO;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.MessageConstants;
import com.mediaplayer.utilities.SQLConstants;

import java.util.ArrayList;

public class SelectPlaylistDialogFragment extends DialogFragment {
    private Context context;
    private ArrayList<Playlist> selectedPlaylists;
    private ArrayList<Playlist> playlistInfoList = MediaLibraryManager.getPlaylistInfoList();
    private Track selectedTrack;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        selectedTrack = (Track) args.getSerializable(MediaPlayerConstants.KEY_SELECTED_TRACK);
        ArrayList<Integer> addedPlaylists;
        int size = playlistInfoList.size(), addedPlaylistsSize, c = 0, listLength;
        String list[];
        Playlist playlist;
        int playlistID;

        //Checking if playlist size > 1 i.e. the user has created any custom playlist
        if(size > 1) {
            MediaplayerDAO dao = new MediaplayerDAO(context);
            addedPlaylists = dao.getPlaylistsForTrack(selectedTrack.getTrackID());

            if(addedPlaylists != null) {
                addedPlaylistsSize = addedPlaylists.size();
            } else {
                addedPlaylistsSize = 0;
            }

            selectedPlaylists = new ArrayList<Playlist>();
            list = new String[size - addedPlaylistsSize - 1];
            listLength = list.length;

            builder.setTitle(MediaPlayerConstants.TITLE_SELECT_PLAYLIST);

            if(listLength != 0) {
                //Adding user created playlists from 'playlistInfoList' to multichoice items list in dialog
                for(int i = 0; i < size; i++) {
                    playlist = playlistInfoList.get(i);
                    playlistID = playlist.getPlaylistID();

                    //Skipping default playlist 'Favourites' and already added playlist from this list
                    if(playlistID != SQLConstants.PLAYLIST_ID_FAVOURITES &&
                            (addedPlaylistsSize == 0 || !addedPlaylists.contains(playlistID))) {
                        list[c++] = playlist.getPlaylistName();
                    }
                }

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
                            MediaplayerDAO dao = new MediaplayerDAO(context);
                            dao.addToPlaylists(selectedPlaylists, selectedTrack);

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
                builder.setMessage(MessageConstants.ERROR_NO_PLAYLIST);
                builder.setPositiveButton(MediaPlayerConstants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                });
            }
        } else {
            //TODO: 23-Aug-16 Update this dialog's design. Add a button to create playlist
            builder.setTitle(MediaPlayerConstants.TITLE_ERROR);
            builder.setMessage(MessageConstants.ERROR_NO_PLAYLIST_CREATED);
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

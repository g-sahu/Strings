package com.mediaplayer.strings.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.dao.MediaPlayerDAO;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.support.v7.app.AlertDialog.Builder;
import static com.mediaplayer.strings.R.id.playlistTitle;
import static com.mediaplayer.strings.R.layout.dialog_create_playlist;
import static com.mediaplayer.strings.fragments.PlaylistsFragment.recyclerView;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.*;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.*;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_PLAYLIST_TITLE;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_PLAYLIST_TITLE_BLANK;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_PLAYLIST_TITLE_FAVOURITES;
import static com.mediaplayer.strings.utilities.SQLConstants.PLAYLIST_TITLE_FAVOURITES;
import static com.mediaplayer.strings.utilities.SQLConstants.ZERO;

public class CreatePlaylistDialogFragment extends DialogFragment {
    private Context context;
    private MediaPlayerDAO dao;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog playlistDialog = null;

        try {
            context = getContext();
            Builder builder = new Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(dialog_create_playlist, null);
            String tag = getTag();

            if(tag.equals(TAG_CREATE_PLAYLIST)) {
                //Setting values for dialog window
                builder.setTitle(TITLE_CREATE_PLAYLIST);
                builder.setView(dialogView);
                builder.setPositiveButton(CREATE, null);
                playlistDialog = builder.create();

                playlistDialog.setOnShowListener(dialog -> {
                    final AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(BUTTON_POSITIVE);

                    //Setting on-click listener for positive button
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            //Retreiving playlist name entered by the user
                            EditText playlistTitleTextBox = dialogView.findViewById(playlistTitle);
                            String playlistName = playlistTitleTextBox.getText().toString();

                            //Validating playlist title
                            if(isPlaylistTitleValid(playlistTitleTextBox)) {
                                //Setting the values for newly created playlist
                                Playlist playlist = new Playlist();
                                playlist.setPlaylistName(playlistName);
                                playlist.setPlaylistSize(ZERO);
                                playlist.setPlaylistDuration(ZERO);

                                try {
                                    dao = new MediaPlayerDAO(context);

                                    //Creating playlist
                                    dao.createPlaylist(playlist);
                                } catch(Exception e) {
                                    Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                                    //Utilities.reportCrash(e);
                                } finally {
                                    if(dao != null) {
                                        dao.closeConnection();
                                    }
                                }

                                //Sorting the playlists
                                sortPlaylists();

                                //Updating list view adapter
                                updatePlaylistsAdapter();

                                //Dismissing the dialog window
                                alertDialog.dismiss();
                            }
                        }
                    });
                });
            } else if(tag.equals(TAG_RENAME_PLAYLIST)) {
                //Fetching old playlist values
                Bundle args = getArguments();
                final String oldPlaylistTitle = args.getString(KEY_PLAYLIST_TITLE);
                final int oldPlaylistIndex = args.getInt(KEY_PLAYLIST_INDEX);

                //Setting old playlist title in the input text box
                final EditText playlistTitleTextBox = dialogView.findViewById(playlistTitle);
                playlistTitleTextBox.setText(oldPlaylistTitle);

                //Setting values for dialog window
                builder.setTitle(TITLE_RENAME_PLAYLIST);
                builder.setView(dialogView);
                builder.setPositiveButton(RENAME, null);
                playlistDialog = builder.create();

                playlistDialog.setOnShowListener(dialog -> {
                    final AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(BUTTON_POSITIVE);

                    //Setting on-click listener for positive button
                    positiveButton.setOnClickListener(v -> {
                        //Retreiving playlist name entered by the user
                        String newPlaylistTitle = playlistTitleTextBox.getText().toString();

                        //Validating playlist title and checking if it is not the same as the old one
                        if(isPlaylistTitleValid(playlistTitleTextBox) && !newPlaylistTitle.equals(oldPlaylistTitle)) {
                            //Fetching selected playlist from playlistInfoList and updating playlist title
                            Playlist playlist = getPlaylistByIndex(oldPlaylistIndex);
                            playlist.setPlaylistName(newPlaylistTitle);

                            //Updating playlistInfoList with new playlist values
                            updatePlaylistInfoList(oldPlaylistIndex, playlist);

                            //Sort playlistInfoList to update the indices of the playlists
                            sortPlaylists();

                            //Getting upated playlistIndex of the renamed playlist
                            playlist = getPlaylistByTitle(newPlaylistTitle);
                            int newPlaylistIndex = playlist.getPlaylistIndex();
                            playlist.setPlaylistIndex(newPlaylistIndex);

                            try {
                                dao = new MediaPlayerDAO(context);

                                //Updating table 'Playlist' with new values of playlist_title and playlist_index
                                dao.renamePlaylist(playlist);
                            } catch(Exception e) {
                                Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                                //Utilities.reportCrash(e);
                            } finally {
                                if(dao != null) {
                                    dao.closeConnection();
                                }
                            }

                            //Updating list view adapter
                            updatePlaylistsAdapter();

                            //Dismissing the dialog window
                            alertDialog.dismiss();
                        }
                    });
                });
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return playlistDialog;
    }

    private boolean isPlaylistTitleValid(EditText playlistTitleTextBox) {
        String newPlaylistTitle = playlistTitleTextBox.getText().toString();

        //Checking if playlist title is not empty string
        if(newPlaylistTitle.isEmpty()) {
            playlistTitleTextBox.setError(ERROR_PLAYLIST_TITLE_BLANK);
            return false;
        }

        //Checking if playlist title is not the same as default playlist 'Favourites'
        else if(newPlaylistTitle.equalsIgnoreCase(PLAYLIST_TITLE_FAVOURITES)) {
            playlistTitleTextBox.setError(ERROR_PLAYLIST_TITLE_FAVOURITES);
            return false;
        }

        //Checking if playlist title is not the same as an existing playlist
        else if(getPlaylistByTitle(newPlaylistTitle) != null) {
            playlistTitleTextBox.setError(ERROR_PLAYLIST_TITLE);
            return false;
        }
        else {
            return true;
        }
    }

    private void updatePlaylistsAdapter() {
        PlaylistsAdapter adapter = new PlaylistsAdapter(context, getPlaylistInfoList());
        RecyclerView listView = recyclerView;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}

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
import static com.mediaplayer.strings.activities.HomeActivity.getSelectedPlaylist;
import static com.mediaplayer.strings.fragments.PlaylistsFragment.recyclerView;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getPlaylistInfoList;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.getTrackInfoList;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.*;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_NO_TRACK;
import static com.mediaplayer.strings.utilities.MessageConstants.ERROR_NO_TRACKS_ADDED;
import static com.mediaplayer.strings.utilities.SQLConstants.ZERO;
import static com.mediaplayer.strings.utilities.Utilities.isNotNullOrEmpty;

public class SelectTrackDialogFragment extends DialogFragment {
    private Context context;
    private ArrayList<Track> selectedTracks;
    private ArrayList<Track> tracksInLibrary = getTrackInfoList();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        context = getContext();

        try (MediaPlayerDAO dao = new MediaPlayerDAO(context)) {
            Bundle args = getArguments();
            Playlist selectedPlaylist = (Playlist) args.getSerializable(KEY_SELECTED_PLAYLIST);

            //Checking if there are any tracks in the library
            if(isNotNullOrEmpty(tracksInLibrary)) {
                ArrayList<Integer> tracksInPlaylist = dao.getTrackIDsForPlaylist(selectedPlaylist.getPlaylistID());
                int trackInPlaylistSize = isNotNullOrEmpty(tracksInPlaylist) ? tracksInPlaylist.size() : ZERO;
                ArrayList<Track> tracksToDisplay = new ArrayList<>();

                //Iterating tracks in library to remove tracks already added to playlist
                for (Track track: tracksInLibrary) {
                    if(trackInPlaylistSize == ZERO || !tracksInPlaylist.contains(track.getTrackID())) {
                        tracksToDisplay.add(track);
                    }
                }

                selectedTracks = new ArrayList<>();
                String[] list = new String[tracksToDisplay.size()];
                int listLength = list.length;
                int c = 0;

                builder.setTitle(TITLE_SELECT_TRACKS);

                if(listLength != 0) {
                    //Adding tracks to multichoice items list in dialog
                    for (Track track: tracksToDisplay) {
                        list[c++] = track.getTrackTitle();
                    }

                    builder.setMultiChoiceItems(list, null, (dialog, which, isChecked) -> {
                        Track track = tracksToDisplay.get(which);

                        if(isChecked) {
                            selectedTracks.add(track);
                        } else if (selectedTracks.contains(track)) {
                            selectedTracks.remove(track);
                        }
                    });

                    builder.setPositiveButton(OK, (dialog, id) -> {
                        if(isNotNullOrEmpty(selectedTracks)) {
                            try (MediaPlayerDAO dao1 = new MediaPlayerDAO(context)) {
                                dao1.addTracks(selectedTracks, getSelectedPlaylist());
                                updatePlaylistsAdapter();
                                tracksToDisplay.removeAll(selectedTracks);
                            }
                        }
                    });

                    builder.setNegativeButton(CANCEL, (dialog, id) -> {});
                } else {
                    builder.setMessage(ERROR_NO_TRACK);
                    builder.setPositiveButton(OK, (dialog, id) -> {});
                }
            } else {
                builder.setTitle(TITLE_ERROR);
                builder.setMessage(ERROR_NO_TRACKS_ADDED);
                builder.setPositiveButton(OK, (dialog, id) -> {});
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
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

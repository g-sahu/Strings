package com.mediaplayer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mediaplayer.R;
import com.mediaplayer.activities.PlaylistActivity;
import com.mediaplayer.adapters.PlaylistsAdapter;
import com.mediaplayer.beans.Track;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;

public class PlaylistsFragment extends Fragment {
    private View playlistsView;
    private Context context;
    public static ListView listView;

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        playlistsView = inflater.inflate(R.layout.fragment_playlists, container, false);
        listView = (ListView) playlistsView.findViewById(R.id.listView);
        ListAdapter playlistsAdapter = new PlaylistsAdapter(context, MediaLibraryManager.getPlaylistInfoList());
        listView.setAdapter(playlistsAdapter);

        return playlistsView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton createPlaylistButton = (FloatingActionButton) playlistsView.findViewById(R.id.createPlaylistButton);

        // Click Listener for 'Create Playlist' button
        createPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment createPlaylistDialogFragment = new CreatePlaylistDialogFragment();
                createPlaylistDialogFragment.show(getActivity().getSupportFragmentManager(), MediaPlayerConstants.TAG_CREATE_PLAYLIST);
            }
        });
    }
}
package com.mediaplayer.strings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mediaplayer.strings.R;
import com.mediaplayer.strings.adapters.PlaylistsAdapter;
import com.mediaplayer.strings.utilities.MediaLibraryManager;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;

public class PlaylistsFragment extends Fragment {
    private View playlistsView;
    private Context context;
    public static RecyclerView recyclerView;

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
        recyclerView = (RecyclerView) playlistsView.findViewById(R.id.recycler_view);

        RecyclerView.Adapter playlistsAdapter = new PlaylistsAdapter(context, MediaLibraryManager.getPlaylistInfoList());
        recyclerView.setAdapter(playlistsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        return playlistsView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton createPlaylistButton = (FloatingActionButton) playlistsView.findViewById(R.id.createPlaylistButton);

        // Click Listener for 'Create Playlist' button
        createPlaylistButton.setOnClickListener(view -> {
            DialogFragment createPlaylistDialogFragment = new CreatePlaylistDialogFragment();
            createPlaylistDialogFragment.show(getActivity().getSupportFragmentManager(), MediaPlayerConstants.TAG_CREATE_PLAYLIST);
        });
    }
}

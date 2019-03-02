package com.mediaplayer.strings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mediaplayer.strings.R;
import com.mediaplayer.strings.adapters.SongsListAdapter;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.utilities.MediaLibraryManager;

import java.util.ArrayList;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class SongsFragment extends Fragment {
    private Context context;
    public static RecyclerView trackListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        TextView emptyLibraryMessage;
        ArrayList<Track> trackInfoList;
        RecyclerView.Adapter songsListAdapter;

        try {
            view = inflater.inflate(R.layout.fragment_songs, container, false);
            emptyLibraryMessage = view.findViewById(R.id.emptyLibraryMessage);
            trackListView = view.findViewById(R.id.recycler_view);
            trackInfoList = MediaLibraryManager.getTrackInfoList();

            if(trackInfoList == null || trackInfoList.isEmpty()) {
                emptyLibraryMessage.setVisibility(View.VISIBLE);
                trackListView.setVisibility(View.GONE);
            } else {
                songsListAdapter = new SongsListAdapter(context, trackInfoList);
                trackListView.setAdapter(songsListAdapter);
                trackListView.setLayoutManager(new LinearLayoutManager(context));
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return view;
    }
}

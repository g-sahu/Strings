package com.mediaplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediaplayer.R;
import com.mediaplayer.adapters.SongsListAdapter;
import com.mediaplayer.beans.Track;
import com.mediaplayer.utilities.MediaLibraryManager;

import java.util.ArrayList;

public class SongsFragment extends Fragment {
    private Context context;
    public static ListView trackListView;
    private static String LOG_TAG_EXCEPTION = "Exception";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        TextView textView;
        ArrayList<Track> trackInfoList;
        ListAdapter songsListAdapter;

        try {
            view = inflater.inflate(R.layout.fragment_songs, container, false);
            textView = (TextView) view.findViewById(R.id.emptyLibraryMessage);
            trackInfoList = MediaLibraryManager.getTrackInfoList();

            if(trackInfoList == null || trackInfoList.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
            } else {
                trackListView = (ListView) view.findViewById(R.id.listView);
                songsListAdapter = new SongsListAdapter(context, trackInfoList);
                trackListView.setAdapter(songsListAdapter);
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
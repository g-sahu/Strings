package com.mediaplayer.strings.fragments;

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

import com.google.firebase.crash.FirebaseCrash;
import com.mediaplayer.strings.R;
import com.mediaplayer.strings.adapters.SongsListAdapter;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.utilities.MediaLibraryManager;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;

import java.util.ArrayList;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class SongsFragment extends Fragment {
    private Context context;
    public static ListView trackListView;

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
            trackListView = (ListView) view.findViewById(R.id.listView);
            trackInfoList = MediaLibraryManager.getTrackInfoList();

            if(trackInfoList == null || trackInfoList.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                trackListView.setVisibility(View.GONE);
            } else {
                songsListAdapter = new SongsListAdapter(context, trackInfoList);
                trackListView.setAdapter(songsListAdapter);
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());

            FirebaseCrash.log(e.getMessage());
            FirebaseCrash.logcat(Log.ERROR, MediaPlayerConstants.LOG_TAG_EXCEPTION, e.getMessage());
            FirebaseCrash.report(e);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
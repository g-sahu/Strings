package com.mediaplayer.strings.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mediaplayer.strings.beans.Playlist;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.support.v7.widget.RecyclerView.ViewHolder;
import static com.mediaplayer.strings.R.id;
import static com.mediaplayer.strings.R.layout.item_playlist;
import static com.mediaplayer.strings.utilities.SQLConstants.ONE;
import static com.mediaplayer.strings.utilities.Utilities.milliSecondsToTimer;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.Holder> {
    private ArrayList<Playlist> playlistInfoList;
    private static LayoutInflater inflater = null;

    public PlaylistsAdapter(Context context, ArrayList<Playlist> playlistInfoList) {
        this.playlistInfoList = playlistInfoList;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = inflater.inflate(item_playlist, parent, false);
        return new Holder(rowView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Playlist playlist = playlistInfoList.get(position);
        int playlistSize = playlist.getPlaylistSize();
        String text = (playlistSize == ONE) ? " song, " : " songs, ";
        String infoText = playlistSize + text + milliSecondsToTimer(playlist.getPlaylistDuration());

        holder.playlistTitle.setText(playlistInfoList.get(position).getPlaylistName());
        holder.playlistInfo.setText(infoText);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return playlistInfoList.size();
    }

    class Holder extends ViewHolder {
        TextView playlistTitle, playlistInfo;

        Holder(View itemView) {
            super(itemView);
            playlistTitle = itemView.findViewById(id.playlistTitle);
            playlistInfo = itemView.findViewById(id.playlistInfo);
        }
    }
}

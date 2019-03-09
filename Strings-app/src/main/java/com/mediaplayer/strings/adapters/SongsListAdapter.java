package com.mediaplayer.strings.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediaplayer.strings.beans.Track;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.support.v7.widget.RecyclerView.ViewHolder;
import static com.bumptech.glide.Glide.with;
import static com.mediaplayer.strings.R.drawable.img_default_album_art_thumb;
import static com.mediaplayer.strings.R.id;
import static com.mediaplayer.strings.R.id.albumThumbnail;
import static com.mediaplayer.strings.R.layout.item_track;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.Holder> {
    private ArrayList<Track> trackInfoList;
    private static LayoutInflater inflater = null;
    private Context context;

    public SongsListAdapter(Context context, ArrayList<Track> trackInfoList) {
        this.trackInfoList = trackInfoList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override @NonNull
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowView = inflater.inflate(item_track, parent, false);
        return new Holder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Track track = trackInfoList.get(position);
        byte data[] = track.getAlbumArt();

        if(data.length != 0) {
            with(context).load(data).into(holder.albumArt);
        } else {
            with(context).load(img_default_album_art_thumb).into(holder.albumArt);
        }

        holder.trackTitle.setText(track.getTrackTitle());
        holder.artistName.setText(track.getArtistName());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return trackInfoList.size();
    }

    class Holder extends ViewHolder {
        ImageView albumArt;
        TextView trackTitle, artistName;

        Holder(View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(albumThumbnail);
            trackTitle = itemView.findViewById(id.trackTitle);
            artistName = itemView.findViewById(id.artistName);
        }
    }
}

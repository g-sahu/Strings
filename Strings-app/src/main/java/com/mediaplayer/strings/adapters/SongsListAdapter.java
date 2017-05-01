package com.mediaplayer.strings.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mediaplayer.strings.R;
import com.mediaplayer.strings.beans.Track;

import java.util.ArrayList;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.Holder> {
    private ArrayList<Track> trackInfoList;
    private static LayoutInflater inflater = null;
    private Context context;

    public SongsListAdapter(Context context, ArrayList<Track> trackInfoList) {
        this.trackInfoList = trackInfoList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public SongsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = inflater.inflate(R.layout.item_track, parent, false);
        return new SongsListAdapter.Holder(rowView);
    }

    @Override
    public void onBindViewHolder(SongsListAdapter.Holder holder, int position) {
        Track track = trackInfoList.get(position);
        byte data[] = track.getAlbumArt();

        Glide.with(context).load(data).into(holder.albumArt);
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

    class Holder extends RecyclerView.ViewHolder {
        ImageView albumArt;
        TextView trackTitle, artistName;

        Holder(View itemView) {
            super(itemView);
            albumArt = (ImageView) itemView.findViewById(R.id.albumThumbnail);
            trackTitle = (TextView) itemView.findViewById(R.id.trackTitle);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
        }
    }
}
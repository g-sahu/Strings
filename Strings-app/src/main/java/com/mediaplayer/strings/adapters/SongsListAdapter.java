package com.mediaplayer.strings.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediaplayer.strings.R;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.utilities.MediaLibraryManager;

import java.util.ArrayList;

public class SongsListAdapter extends BaseAdapter {
    private ArrayList<Track> trackInfoList;
    private static LayoutInflater inflater = null;

    public SongsListAdapter(Context context, ArrayList<Track> trackInfoList) {
        this.trackInfoList = trackInfoList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        View rowView;
        Bitmap albumArt = null;
        Track track = trackInfoList.get(position);
        System.out.println("getview:" + position + " " + convertView);

        if(convertView == null) {
            holder = new Holder();
            rowView = inflater.inflate(R.layout.item_track, null);

            holder.albumArt = (ImageView) rowView.findViewById(R.id.albumThumbnail);
            holder.trackTitle = (TextView) rowView.findViewById(R.id.trackTitle);
            holder.artistName = (TextView) rowView.findViewById(R.id.artistName);
            holder.moreOptions = (ImageButton) rowView.findViewById(R.id.moreTrackOptionsButton);

            rowView.setTag(holder);
        } else {
            rowView = convertView;
            holder = (Holder) rowView.getTag();
        }

        byte data[] = track.getAlbumArt();

        if(data != null) {
            albumArt = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        holder.albumArt.setImageBitmap(albumArt);
        holder.trackTitle.setText(track.getTrackTitle());
        holder.artistName.setText(track.getArtistName());

        return rowView;
    }

    @Override
    public int getCount() {
        return trackInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return trackInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        ImageView albumArt;
        TextView trackTitle;
        TextView artistName;
        ImageButton moreOptions;
    }
}
package com.mediaplayer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediaplayer.R;
import com.mediaplayer.beans.Track;
import com.mediaplayer.activities.PlaylistActivity;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    ArrayList<Track> trackInfoList;
    private static LayoutInflater inflater = null;

    public CustomAdapter(PlaylistActivity context, ArrayList<Track> trackInfoList) {
        this.trackInfoList = trackInfoList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = inflater.inflate(R.layout.track_list, null);
        byte data[] = trackInfoList.get(position).getAlbumArt();
        Bitmap albumArt = null;

        holder.albumArt = (ImageView) rowView.findViewById(R.id.albumThumbnail);
        holder.trackTitle = (TextView) rowView.findViewById(R.id.trackTitle);
        holder.artistName = (TextView) rowView.findViewById(R.id.artistName);

        if(data != null) {
            albumArt = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        holder.albumArt.setImageBitmap(albumArt);
        holder.trackTitle.setText(trackInfoList.get(position).getTrackTitle());
        holder.artistName.setText(trackInfoList.get(position).getArtistName());

        return rowView;
    }

    @Override
    public int getCount() {
        return trackInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        ImageView albumArt;
        TextView trackTitle;
        TextView artistName;
    }
}
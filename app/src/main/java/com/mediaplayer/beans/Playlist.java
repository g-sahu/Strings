package com.mediaplayer.beans;


import java.util.Comparator;

public class Playlist implements Comparator<Object> {
    private int playlistID;
    private int playlistIndex;
    private String playlistName;
    private int playlistSize;
    private int playlistDuration;

    public int getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(int playlistID) {
        this.playlistID = playlistID;
    }

    public int getPlaylistIndex() {
        return playlistIndex;
    }

    public void setPlaylistIndex(int playlistIndex) {
        this.playlistIndex = playlistIndex;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public int getPlaylistSize() {
        return playlistSize;
    }

    public void setPlaylistSize(int playlistSize) {
        this.playlistSize = playlistSize;
    }

    public int getPlaylistDuration() {
        return playlistDuration;
    }

    public void setPlaylistDuration(int playlistDuration) {
        this.playlistDuration = playlistDuration;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        Playlist playlist1 = (Playlist) lhs;
        Playlist playlist2 = (Playlist) rhs;

        return playlist1.getPlaylistName().compareToIgnoreCase(playlist2.getPlaylistName());
    }
}

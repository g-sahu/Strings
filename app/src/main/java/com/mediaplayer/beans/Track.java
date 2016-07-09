package com.mediaplayer.beans;

import java.io.Serializable;
import java.util.Comparator;

public class Track implements Serializable, Comparator<Object> {
    private byte[] albumArt;
    private String trackTitle;
    private String artistName;
    private String albumName;
    private String trackLength;
    private String trackLocation;

    private int trackIndex;

    public byte[] getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getTrackLocation() {
        return trackLocation;
    }

    public void setTrackLocation(String trackLocation) {
        this.trackLocation = trackLocation;
    }

    public String getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(String trackLength) {
        this.trackLength = trackLength;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        Track track1 = (Track) lhs;
        Track track2 = (Track) rhs;

        return track1.getTrackTitle().compareToIgnoreCase(track2.getTrackTitle());
    }
}

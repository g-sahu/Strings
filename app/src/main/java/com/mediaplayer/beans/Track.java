package com.mediaplayer.beans;

import java.io.Serializable;
import java.util.Comparator;

public class Track implements Serializable, Comparator<Object> {
    private int trackID;
    private String trackTitle;
    private int trackIndex;
    private int currentTrackIndex;
    private String fileName;
    private int trackDuration;
    private long fileSize;
    private String albumName;
    private String artistName;
    private byte[] albumArt;
    private String trackLocation;
    private int favouriteSw;

    public int getTrackID() {
        return trackID;
    }

    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

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

    public int getTrackDuration() {
        return trackDuration;
    }

    public void setTrackDuration(int trackDuration) {
        this.trackDuration = trackDuration;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public int isFavouriteSw() {
        return favouriteSw;
    }

    public void setFavouriteSw(int favouriteSw) {
        this.favouriteSw = favouriteSw;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int currentTrackIndex) {
        this.currentTrackIndex = currentTrackIndex;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        Track track1 = (Track) lhs;
        Track track2 = (Track) rhs;

        return track1.getTrackTitle().compareToIgnoreCase(track2.getTrackTitle());
    }
}

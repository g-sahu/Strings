package com.mediaplayer.utilities;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.mediaplayer.R;
import com.mediaplayer.beans.Track;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class MediaLibraryManager {
    private static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    private static ArrayList<Track> trackInfoList;
    private static int playlistSize;

    public MediaLibraryManager(){}

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public static ArrayList<HashMap<String, String>> getPlayList() {
        File[] fileList = path.listFiles();
        String fileName, filePath;
        ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

        //Iterate through the directory to search for .mp3 files
        for(File file: fileList) {
            //Check if file extension is .mp3
            if(validateExtension(file)) {
                fileName = file.getName().split("[.]")[0];
                filePath = file.getAbsolutePath();

                HashMap<String, String> song = new HashMap<String, String>();
                song.put(MediaPlayerConstants.FILE_NAME, fileName);
                song.put(MediaPlayerConstants.FILE_PATH, filePath);

                songsList.add(song);
            }
        }

        return songsList;
    }

    public ArrayList<Track> getTrackInfo(Resources resources) {
        trackInfoList = new ArrayList<Track>();
        ArrayList<HashMap<String, String>> playList = getPlayList();
        HashMap<String, String> songMap;
        String artistName, songTitle, filePath, albumName, trackLength;
        Bitmap albumArt;
        byte data[];
        Iterator<HashMap<String, String>> playListIterator = playList.iterator();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        while(playListIterator.hasNext()) {
            songMap = playListIterator.next();
            filePath = songMap.get(MediaPlayerConstants.FILE_PATH);
            mmr.setDataSource(filePath);

            songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null
                            ? songMap.get(MediaPlayerConstants.FILE_NAME)
                            : mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) == null
                            ? MediaPlayerConstants.UNKNOWN_ALBUM
                            : mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) == null
                            ? MediaPlayerConstants.UNKNOWN_ARTIST
                            : mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            trackLength = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) == null
                            ? MediaPlayerConstants.TIME_ZERO
                            : mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            data = mmr.getEmbeddedPicture();

            Track track = new Track();
            track.setTrackTitle(songTitle);
            track.setAlbumName(albumName);
            track.setArtistName(artistName);
            track.setTrackLocation(filePath);
            track.setTrackLength(trackLength);

            if (data != null) {
                track.setAlbumArt(data);
            } else {
                albumArt = BitmapFactory.decodeResource(resources, R.drawable.default_album_art);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                albumArt.compress(Bitmap.CompressFormat.PNG, 100, stream);
                track.setAlbumArt(stream.toByteArray());
            }

            trackInfoList.add(track);
        }

        mmr.release();
        playlistSize = trackInfoList.size();

        //Sort the track list
        sortPlaylist(trackInfoList);

        return trackInfoList;
    }

    public void sortPlaylist(ArrayList<Track> playlist) {
        Collections.sort(playlist, new Track());
        Iterator<Track> playlistIterator = playlist.iterator();
        Track track;
        int i = 0;

        while(playlistIterator.hasNext()) {
            track = playlistIterator.next();
            track.setTrackIndex(i);
            i++;
        }
    }

    public static int getPlaylistSize() {
        if(trackInfoList != null) {
            return trackInfoList.size();
        } else {
            return 0;
        }
    }

    public static Track getTrack(int index) {
        return trackInfoList.get(index);
    }

    public static Track getFirstTrack() {
        return trackInfoList.get(0);
    }

    public static Track getLastTrack() {
        return trackInfoList.get(playlistSize - 1);
    }

    public static boolean isFirstTrack(int index) {
        return (index == 0);
    }

    public static boolean isLastTrack(int index) {
        return (index == (playlistSize - 1));
    }

    public static boolean validateExtension(File file) {
        boolean isValidFile = false;
        String fileName = file.getName();
        String extension = "";
        int i = fileName.lastIndexOf('.');

        if (i > 0) {
            extension = fileName.substring(i+1);
        }

        //Code to check file extension
        if(extension.equalsIgnoreCase("mp3")) {
            isValidFile = true;
        }

        return isValidFile;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }
}
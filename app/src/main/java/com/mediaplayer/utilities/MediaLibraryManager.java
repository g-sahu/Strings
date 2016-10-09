package com.mediaplayer.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import com.mediaplayer.R;
import com.mediaplayer.beans.Playlist;
import com.mediaplayer.beans.Track;
import com.mediaplayer.dao.MediaplayerDAO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class MediaLibraryManager {
    private static String LOG_TAG_SQL = "Executing query";
    private static String LOG_TAG_EXCEPTION = "Exception";
    private static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    private static ArrayList<Track> trackInfoList;
    private static ArrayList<Track> selectedPlaylist;
    private static ArrayList<Playlist> playlistInfoList;
    private static int tracklistSize;
    private static int selectedPlaylistSize;

    public MediaLibraryManager(){}

    public static void init(Context context) {
        MediaplayerDAO dao = new MediaplayerDAO(context);

        //Get all filenames from db and store in an ArrayList
        ArrayList<String> fileNamesList = dao.getFileNamesForTracks();

        //Get all mp3 files from storage
        ArrayList<Track> newTracksList = getUpdatedTracks(fileNamesList, context.getResources());

        //Insert new tracks in db
        dao.addTracksToLibrary(newTracksList);

        trackInfoList = dao.getTracks();
        sortTracklist(MediaPlayerConstants.KEY_PLAYLIST_LIBRARY);
        dao.updateTrackIndices();

        playlistInfoList = dao.getPlaylists();
        sortPlaylists();
    }

    /**
     * Method to read all mp3 files from sd card
     * and store the details in ArrayList
     * */
    private static ArrayList<HashMap<String, Object>> getTracks() {
        File[] fileList = path.listFiles();
        String fileName, filePath;
        long fileSize;
        ArrayList<HashMap<String, Object>> tracklist = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> song;

        //Iterate through the directory to search for .mp3 files
        for(File file: fileList) {
            //Check if file extension is .mp3
            if(validateExtension(file)) {
                song = new HashMap<String, Object>();
                fileName = file.getName().split("[.]")[0];
                filePath = file.getAbsolutePath();
                fileSize = file.length();

                song.put(MediaPlayerConstants.FILE_NAME, fileName);
                song.put(MediaPlayerConstants.FILE_PATH, filePath);
                song.put(MediaPlayerConstants.FILE_SIZE, fileSize);

                tracklist.add(song);
            }
        }

        return tracklist;
    }

    /**
     * Method to populate trackInfoList from the list of mp3 files read from file memory
     * @param resources Reference to application's resources
     * @return The list of songs read from memory
     */
    public static ArrayList<Track> populateTrackInfoList(Resources resources) {
        trackInfoList = new ArrayList<Track>();
        ArrayList<HashMap<String, Object>> tracklist = getTracks();
        HashMap<String, Object> songMap;
        String artistName, songTitle, fileName, filePath, albumName, trackLength;
        long fileSize;
        Bitmap albumArt;
        byte data[];
        Iterator<HashMap<String, Object>> tracklistIterator = tracklist.iterator();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        while(tracklistIterator.hasNext()) {
            songMap = tracklistIterator.next();
            filePath = songMap.get(MediaPlayerConstants.FILE_PATH).toString();
            fileName = songMap.get(MediaPlayerConstants.FILE_NAME).toString();
            fileSize = (Long) songMap.get(MediaPlayerConstants.FILE_SIZE);
            mmr.setDataSource(filePath);

            songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null
                            ? fileName
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
            track.setFileName(fileName);
            track.setTrackDuration(Integer.parseInt(trackLength));
            track.setFileSize(fileSize);
            track.setAlbumName(albumName);
            track.setArtistName(artistName);
            track.setTrackLocation(filePath);
            track.setFavSw(SQLConstants.FAV_SW_NO);

            if(data != null) {
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
        tracklistSize = trackInfoList.size();

        //Sort the track list
        sortTracklist(MediaPlayerConstants.KEY_PLAYLIST_LIBRARY);
        return trackInfoList;
    }

    /**
     * Method to sort list of tracks in Media library
     */
    public static void sortTracklist(String playlistType) {
        Iterator<Track> tracklistIterator;
        Track track;
        int i = 0;

        switch(playlistType) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                Collections.sort(trackInfoList, new Track());
                tracklistIterator = trackInfoList.iterator();

                while(tracklistIterator.hasNext()) {
                    track = tracklistIterator.next();
                    track.setTrackIndex(i);
                    i++;
                }

                break;

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                Collections.sort(selectedPlaylist, new Track());
                tracklistIterator = selectedPlaylist.iterator();

                while(tracklistIterator.hasNext()) {
                    track = tracklistIterator.next();
                    track.setCurrentTrackIndex(i);
                    i++;
                }

                break;
        }
    }

    /**
     * Method to sort list of playlists in Media library
     */
    public static void sortPlaylists() {
        //Removing default playlist 'Favourites' from playlistInfoList to prevent it's index from changing
        Playlist fav =  playlistInfoList.remove(SQLConstants.PLAYLIST_INDEX_FAVOURITES);

        Collections.sort(playlistInfoList, new Playlist());
        playlistInfoList.add(SQLConstants.PLAYLIST_INDEX_FAVOURITES, fav);
        Iterator<Playlist> playlistIterator = playlistInfoList.iterator();
        Playlist playlist;
        int i = 0;

        while(playlistIterator.hasNext()) {
            playlist = playlistIterator.next();
            playlist.setPlaylistIndex(i);
            i++;
        }
    }

    public static void addTrack(Track track) {
        trackInfoList.add(track);
    }

    public static ArrayList<Track> getTrackInfoList() {
        return trackInfoList;
    }

    public static ArrayList<Playlist> getPlaylistInfoList() {
        return playlistInfoList;
    }

    public static Playlist getPlaylistByIndex(int index) {
        return playlistInfoList.get(index);
    }

    public static Playlist getPlaylistByTitle(String title) {
        Playlist playlist;
        Iterator<Playlist> playlistIterator = playlistInfoList.iterator();

        while(playlistIterator.hasNext()) {
            playlist = playlistIterator.next();

            if(title.equals(playlist.getPlaylistName())) {
                return playlist;
            }
        }

        return null;
    }

    public static int getTrackInfoListSize() {
        if(trackInfoList != null) {
            return trackInfoList.size();
        } else {
            return SQLConstants.ZERO;
        }
    }

    public static int getPlaylistInfoListSize() {
        if(playlistInfoList != null) {
            return playlistInfoList.size();
        } else {
            return SQLConstants.ZERO;
        }
    }

    public static Track getTrackByIndex(String playlistType, int index) {
        switch (playlistType) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                return trackInfoList.get(index);

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                return selectedPlaylist.get(index);

            default:
                return null;
        }
    }

    public static Track getFirstTrack(String playlistType) {
        switch(playlistType) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                return trackInfoList.get(0);

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                return selectedPlaylist.get(0);

            default:
                return null;
        }
    }

    public static Track getLastTrack(String playlistType) {
        switch(playlistType) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                return trackInfoList.get(tracklistSize - 1);

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                return selectedPlaylist.get(selectedPlaylist.size() - 1);

            default:
                return null;
        }
    }

    public static boolean isFirstTrack(int index) {
        return (index == 0);
    }

    public static boolean isLastTrack(String playlistType, int index) {
        switch(playlistType) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                return (index == (tracklistSize - 1));

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                return (index == (selectedPlaylist.size() - 1));

            default:
                return false;
        }
    }

    public static void removePlaylist(int index) {
        playlistInfoList.remove(index);
    }

    public static void removeTrack(String playlistType, int index) {
        switch(playlistType) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                trackInfoList.remove(index);
                break;

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                selectedPlaylist.remove(index);
                break;
        }
    }

    public static void addPlaylist(Playlist playlist) {
        playlistInfoList.add(playlist);
    }

    public static void updateTrackInfoList(int index, Track track) {
        trackInfoList.set(index, track);
    }

    public static void updatePlaylistInfoList(int index, Playlist playlist) {
        playlistInfoList.set(index, playlist);
    }

    public static ArrayList<Track> getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public static void setSelectedPlaylist(ArrayList<Track> selectedPlaylist) {
        MediaLibraryManager.selectedPlaylist = selectedPlaylist;
    }

    private static boolean validateExtension(File file) {
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

    private static ArrayList<Track> getUpdatedTracks(ArrayList<String> fileNamesList, Resources resources) {
        File[] fileList = path.listFiles();
        String fileName, filePath,artistName, songTitle, albumName, trackLength;
        long fileSize;
        Bitmap albumArt;
        byte data[];
        ArrayList<Track> newTracksList = new ArrayList<Track>();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        int c = -1;

        try {
            //Iterate through the directory to search for .mp3 files
            for (File file : fileList) {
                //Check if file extension is .mp3
                if (validateExtension(file)) {
                    fileName = file.getName().split("[.]")[0];

                    if (!fileNamesList.contains(fileName)) {
                        filePath = file.getAbsolutePath();
                        fileSize = file.length();
                        mmr.setDataSource(filePath);

                        songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null
                                ? fileName
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
                        track.setTrackIndex(c--);
                        track.setFileName(fileName);
                        track.setTrackDuration(Integer.parseInt(trackLength));
                        track.setFileSize(fileSize);
                        track.setAlbumName(albumName);
                        track.setArtistName(artistName);
                        track.setTrackLocation(filePath);
                        track.setFavSw(SQLConstants.FAV_SW_NO);

                        if (data != null) {
                            track.setAlbumArt(data);
                        } else {
                            albumArt = BitmapFactory.decodeResource(resources, R.drawable.default_album_art);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            albumArt.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            track.setAlbumArt(stream.toByteArray());
                        }

                        newTracksList.add(track);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            mmr.release();
        }

        return newTracksList;
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
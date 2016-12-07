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
import com.mediaplayer.dao.MediaPlayerDAO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import static com.mediaplayer.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class MediaLibraryManager {
    private static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    private static ArrayList<Track> trackInfoList;
    private static ArrayList<Track> selectedPlaylist;
    private static ArrayList<Playlist> playlistInfoList;
    private static int tracklistSize;

    public MediaLibraryManager(){}

    public static boolean init(Context context) {
        HashMap<String, ArrayList<Track>> map;
        ArrayList<Track> trackList, newTracksList = null, deletedTracksList = null;
        MediaPlayerDAO dao = null;
        boolean isChanged = false;

        try {
            dao = new MediaPlayerDAO(context);

            //Get all filenames from db and store in an ArrayList
            trackList = dao.getTracksFromLibrary();

            //Get all mp3 files from storage
            map = getUpdatedTracks(trackList, context.getResources());

            //Check if map is not null. If null, it means there has been no change to the songs library
            if (map != null) {
                newTracksList = map.get(MediaPlayerConstants.KEY_NEW_TRACKS_LIST);
                deletedTracksList = map.get(MediaPlayerConstants.KEY_DELETED_TRACKS_LIST);
                isChanged = true;
            }

            //Insert new tracks in db
            if (newTracksList != null && !newTracksList.isEmpty()) {
                dao.addTracksToLibrary(newTracksList);
            }

            //Delete deleted tracks from db
            if (deletedTracksList != null && !deletedTracksList.isEmpty()) {
                dao.deleteTracksFromLibrary(deletedTracksList);
            }

            //Getting list of all tracks from db
            trackInfoList = dao.getTracks();

            if(trackInfoList != null) {
                tracklistSize = trackInfoList.size();

                //Sorting the track list
                sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);

                if (map != null) {
                    //Updating track indices in db to keep in sync with trackInfoList
                    dao.updateTrackIndices();
                }
            }

            //Getting list of all playlist from db and sorting them
            playlistInfoList = dao.getPlaylists();
            sortPlaylists();
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
        return isChanged;
    }

    /**
     * Method to read all mp3 files from sd card
     * and store the details in ArrayList
     * */
    private static ArrayList<HashMap<String, Object>> getTracks() {
        ArrayList<HashMap<String, Object>> tracklist = null;
        HashMap<String, Object> song;
        String fileName, filePath;
        long fileSize;

        //Getting all the files at the given path
        File[] fileList = path.listFiles();

        if(fileList != null && (fileList.length > 0)) {
            Log.d("Files in the directory", String.valueOf(fileList.length));
            tracklist = new ArrayList<HashMap<String, Object>>();

            //Iterate through the directory to search for .mp3 files
            for(File file : fileList) {
                //Check if file extension is .mp3
                if(isExtensionValid(file)) {
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

            Log.d("Music files fetched", String.valueOf(tracklist.size()));
        }

        return tracklist;
    }

    /**
     * Method to populate trackInfoList from the list of mp3 files read from file memory
     * @param resources Reference to application's resources
     * @return The list of songs read from memory
     */
    public static ArrayList<Track> populateTrackInfoList(Resources resources) {
        String artistName, songTitle, fileName, filePath, albumName, trackLength;
        ArrayList<HashMap<String, Object>> tracklist;
        Iterator<HashMap<String, Object>> tracklistIterator;
        HashMap<String, Object> songMap;
        MediaMetadataRetriever mmr = null;
        long fileSize;
        Bitmap albumArt;
        byte data[];
        Track track;
        ByteArrayOutputStream stream = null;

        try {
            //Getting the list of media files from storage
            tracklist = getTracks();

            if(tracklist != null && !tracklist.isEmpty()) {
                trackInfoList = new ArrayList<Track>();
                tracklistIterator = tracklist.iterator();
                mmr = new MediaMetadataRetriever();

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

                    track = new Track();
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
                        stream = new ByteArrayOutputStream();
                        albumArt.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        track.setAlbumArt(stream.toByteArray());
                    }

                    trackInfoList.add(track);
                }

                tracklistSize = trackInfoList.size();

                //Sorting the track list
                sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);
            } else {
                tracklistSize = 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(mmr != null) {
                mmr.release();
            }

            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                }
            }
        }

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
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                Collections.sort(trackInfoList, new Track());
                tracklistIterator = trackInfoList.iterator();

                while(tracklistIterator.hasNext()) {
                    track = tracklistIterator.next();
                    track.setTrackIndex(i);
                    i++;
                }

                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
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
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return trackInfoList.get(index);

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                return selectedPlaylist.get(index);

            default:
                return null;
        }
    }

    public static Track getFirstTrack(String playlistType) {
        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return trackInfoList.get(0);

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                return selectedPlaylist.get(0);

            default:
                return null;
        }
    }

    public static Track getLastTrack(String playlistType) {
        switch(playlistType) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return trackInfoList.get(tracklistSize - 1);

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
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
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                return (index == (tracklistSize - 1));

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
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
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                trackInfoList.remove(index);
                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
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

    private static boolean isExtensionValid(File file) {
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

    private static HashMap<String, ArrayList<Track>> getUpdatedTracks(ArrayList<Track> trackList, Resources resources) {
        String fileName, filePath, artistName, songTitle, albumName, trackLength;
        ByteArrayOutputStream stream;
        ArrayList<String> libraryFileNamesList = null, musicFileNamesList, newFileNamesList, deletedFileNamesList = null;
        ArrayList<Track> newTracksList = null, deletedTracksList = null;
        ArrayList<File> musicFilesList;
        Iterator<Track> trackListIterator;
        Iterator<File> musicFilesListIterator;
        HashMap<String, ArrayList<Track>> map = null;
        File[] fileList;
        File musicFile;
        Track track;
        long fileSize;
        Bitmap albumArt;
        byte data[];
        int c = -1;

        ArrayList<Track> renamedTracksList = new ArrayList<Track>();            // TODO: 13-Oct-16
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        try {
            //Checking if tracks exist in db
            if(trackList != null && !trackList.isEmpty()) {
                trackListIterator = trackList.iterator();
                libraryFileNamesList = new ArrayList<String>();

                //Iterating trackList to get the list of all file names
                while (trackListIterator.hasNext()) {
                    track = trackListIterator.next();
                    libraryFileNamesList.add(track.getFileName());
                }
            }

            //Listing all files in the directory
            fileList = path.listFiles();

            if(fileList != null && fileList.length > 0) {
                Log.d("Files in the directory", String.valueOf(fileList.length));
                musicFileNamesList = new ArrayList<String>();
                musicFilesList = new ArrayList<File>();

                //Iterating the files to search for mp3 files
                for(File file : fileList) {
                    //Check if file extension is .mp3
                    if(isExtensionValid(file)) {
                        fileName = file.getName().split("[.]")[0];
                        musicFileNamesList.add(fileName);
                        musicFilesList.add(file);
                    }
                }

                if(!musicFileNamesList.isEmpty() && !musicFilesList.isEmpty()) {
                    Log.d("Music files fetched", String.valueOf(musicFilesList.size()));

                    if(libraryFileNamesList != null && !libraryFileNamesList.isEmpty()) {
                        //Getting all newly added tracks
                        newFileNamesList = new ArrayList<>(musicFileNamesList);
                        newFileNamesList.removeAll(libraryFileNamesList);
                    } else {
                        newFileNamesList = musicFileNamesList;
                    }

                    if(!newFileNamesList.isEmpty()) {
                        newTracksList = new ArrayList<Track>();
                        musicFilesListIterator = musicFilesList.iterator();

                        //Iterating music files list to get the list of tracks
                        while(musicFilesListIterator.hasNext()) {
                            musicFile = musicFilesListIterator.next();
                            fileName = musicFile.getName().split("[.]")[SQLConstants.ZERO];

                            //Checking if it is a new track
                            if(newFileNamesList.contains(fileName)) {
                                filePath = musicFile.getAbsolutePath();
                                fileSize = musicFile.length();
                                mmr.setDataSource(filePath);

                                //Retrieving track info
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

                                //Creating track
                                track = new Track();
                                track.setTrackTitle(songTitle);
                                track.setTrackIndex(c--);
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
                                    stream = new ByteArrayOutputStream();
                                    albumArt.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    track.setAlbumArt(stream.toByteArray());
                                }

                                //Adding track to the list of new tracks
                                newTracksList.add(track);
                            }
                        }
                    }

                    //Getting all deleted tracks
                    if(libraryFileNamesList != null && !libraryFileNamesList.isEmpty()) {
                        deletedFileNamesList = new ArrayList<>(libraryFileNamesList);
                        deletedFileNamesList.removeAll(musicFileNamesList);
                    }
                } else {
                    deletedTracksList = trackList;
                }
            } else {
                deletedTracksList = trackList;
            }

            //Iterating trackList to create list of deleted tracks
            if(deletedFileNamesList != null && !deletedFileNamesList.isEmpty()) {
                deletedTracksList = new ArrayList<Track>();
                trackListIterator = trackList.iterator();

                while(trackListIterator.hasNext()) {
                    track = trackListIterator.next();

                    if(deletedFileNamesList.contains(track.getFileName())) {
                        deletedTracksList.add(track);
                    }
                }
            }

            if((newTracksList != null && !newTracksList.isEmpty()) ||
                    (deletedTracksList != null && !deletedTracksList.isEmpty())) {
                map = new HashMap<String, ArrayList<Track>>();
                map.put(MediaPlayerConstants.KEY_NEW_TRACKS_LIST, newTracksList);
                map.put(MediaPlayerConstants.KEY_DELETED_TRACKS_LIST, deletedTracksList);

                if(newTracksList != null && !newTracksList.isEmpty()) {
                    Log.d("New tracks", String.valueOf(newTracksList.size()));
                }

                if(deletedTracksList != null && !deletedTracksList.isEmpty()) {
                    Log.d("Deleted tracks", String.valueOf(deletedTracksList.size()));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            mmr.release();
        }

        return map;
    }

    public static boolean isUserPlaylistEmpty() {
        return (selectedPlaylist == null || selectedPlaylist.isEmpty());
    }
}
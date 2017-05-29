package com.mediaplayer.strings.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.dao.MediaPlayerDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class MediaLibraryManager {
    private static ArrayList<Track> trackInfoList;
    private static ArrayList<Track> selectedPlaylist;
    private static ArrayList<Playlist> playlistInfoList;
    private static int tracklistSize;

    public static boolean init(Context context) {
        HashMap<String, ArrayList<?>> map = null;
        ArrayList<Track> newTracksList = null;
        ArrayList<String> fileNamesList, deletedTracksList = null;
        MediaPlayerDAO dao = null;
        boolean isChanged = false;

        try {
            dao = new MediaPlayerDAO(context);

            //Checking if this is not the first time tracks are populated
            if(trackInfoList == null) {
                //Get all filenames from db and store in an ArrayList
                fileNamesList = dao.getFileNamesFromLibrary();

                //Get all mp3 files from storage
                map = getUpdatedTracks(fileNamesList, context);

                //Check if map is not null. If null, it means there has been no change to the songs library
                if (map != null) {
                    newTracksList = (ArrayList<Track>) map.get(MediaPlayerConstants.KEY_NEW_TRACKS_LIST);
                    deletedTracksList = (ArrayList<String>) map.get(MediaPlayerConstants.KEY_DELETED_TRACKS_LIST);
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
            }

            //Getting list of all tracks from db
            trackInfoList = dao.getTracks();
            sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);

            if(trackInfoList != null) {
                tracklistSize = trackInfoList.size();

                if (map != null) {
                    //Updating track indices in db to keep in sync with trackInfoList
                    MediaPlayerDAO.updateTrackIndices();
                }
            }

            //Getting list of all playlist from db and sorting them
            playlistInfoList = dao.getPlaylists();
            sortPlaylists();
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(dao != null) {
                dao.closeConnection();
            }
        }
        return isChanged;
    }

    /**
     * Method to read all music files from MediaStore
     * and store the details in a Cursor
     **/
    private static Cursor[] getAllTracksFromProvider(Context context) {
        Cursor cursors[] = new Cursor[2];
        String[] projection = new String[] {
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.MediaColumns.DATA,
        };

        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " != 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_ALARM + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_NOTIFICATION + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_PODCAST + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_RINGTONE + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.DURATION + " > 60000";

        Uri internalUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        cursors[0] = contentResolver.query(internalUri, projection, selection, null, null);
        cursors[1] = contentResolver.query(externalUri, projection, selection, null, null);

        return cursors;
    }

    private static Cursor[] getFileNamesFromProvider(Context context) {
        Cursor cursors[] = new Cursor[2];
        String[] projection = new String[] {
                MediaStore.Audio.AudioColumns.DISPLAY_NAME
        };

        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " != 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_ALARM + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_NOTIFICATION + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_PODCAST + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.IS_RINGTONE + " == 0 " + SQLConstants.AND +
                MediaStore.Audio.AudioColumns.DURATION + " > 60000";

        Uri internalUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        cursors[0] = contentResolver.query(internalUri, projection, selection, null, null);
        cursors[1] = contentResolver.query(externalUri, projection, selection, null, null);

        return cursors;
    }

    private static Cursor[] getNewTracksFromProvider(Context context, ArrayList<String> fileNamesList) {
        Cursor cursors[] = new Cursor[2];
        String[] projection = new String[] {
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.MediaColumns.DATA,
        };

        Iterator<String> fileNamesIterator = fileNamesList.iterator();
        StringBuilder fileNames = new StringBuilder();

        while(fileNamesIterator.hasNext()) {
            fileNames.append(SQLConstants.DOUBLE_QUOTE).append(fileNamesIterator.next()).append(SQLConstants.DOUBLE_QUOTE);

            if(fileNamesIterator.hasNext()) {
                fileNames.append(SQLConstants.COMMA_SEP);
            }
        }

        String selection = MediaStore.Audio.AudioColumns.DISPLAY_NAME + " IN (" + fileNames + ")";
        Uri internalUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        cursors[0] = contentResolver.query(internalUri, projection, selection, null, null);
        cursors[1] = contentResolver.query(externalUri, projection, selection, null, null);

        return cursors;
    }

    /**
     * Method to create track list from tracks fetched from MediaStore
     * @param context Reference to calling context
     * @return Track list
     */
    public static ArrayList<Track> populateTrackInfoList(Context context) {
        //Fetching metadata of all tracks from MediaStore content provider
        Cursor cursors[] = getAllTracksFromProvider(context);

        //Creating track list from cursors
        trackInfoList = createTrackListFromCursor(cursors);
        tracklistSize = (trackInfoList != null && !trackInfoList.isEmpty()) ? trackInfoList.size() : 0;

        sortTracklist(MediaPlayerConstants.TAG_PLAYLIST_LIBRARY);
        return trackInfoList;
    }

    private static ArrayList<Track> createTrackListFromCursor(Cursor cursors[]) {
        int c;
        byte data[];
        Track track;
        String filePath;
        MediaMetadataRetriever mmr = null;
        ArrayList<Track> trackList = null;

        try {
            for(Cursor tracksCursor : cursors) {
                if(tracksCursor.getCount() > SQLConstants.ZERO) {
                    tracksCursor.moveToFirst();
                    mmr = new MediaMetadataRetriever();
                    trackList = new ArrayList<Track>();

                    while(!tracksCursor.isAfterLast()) {
                        track = new Track();
                        c = SQLConstants.ZERO;

                        track.setTrackTitle(tracksCursor.getString(c++));
                        track.setFileName(tracksCursor.getString(c++));
                        track.setTrackDuration(tracksCursor.getInt(c++));
                        track.setFileSize(tracksCursor.getInt(c++));
                        track.setAlbumName(tracksCursor.getString(c++));
                        track.setArtistName(tracksCursor.getString(c++));
                        filePath = tracksCursor.getString(c);
                        track.setTrackLocation(filePath);

                        mmr.setDataSource(filePath);
                        data = mmr.getEmbeddedPicture();

                        if(data != null && data.length > SQLConstants.ZERO) {
                            track.setAlbumArt(data);
                        } else {
                            track.setAlbumArt(new byte[SQLConstants.ZERO]);
                        }

                        trackList.add(track);
                        tracksCursor.moveToNext();
                    }

                    tracksCursor.close();
                }
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        } finally {
            if(mmr != null) {
                mmr.release();
            }
        }

        return trackList;
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

    private static HashMap<String, ArrayList<?>> getUpdatedTracks(ArrayList<String> libraryFileNamesList, Context context) {
        ArrayList<String> storageFileNamesList = new ArrayList<String>();
        ArrayList<String> newFileNamesList, deletedFileNamesList = null;
        ArrayList<Track> newTracksList = null;
        HashMap<String, ArrayList<?>> map = null;

        try {
            //Getting the cursors of music file names from MediaStore
            Cursor[] cursors = getFileNamesFromProvider(context);

            //Creating a list of music file names from the cursors
            for (Cursor cursor : cursors) {
                try {
                    if (cursor.getCount() > SQLConstants.ZERO) {
                        cursor.moveToFirst();

                        while (!cursor.isAfterLast()) {
                            storageFileNamesList.add(cursor.getString(SQLConstants.ZERO));
                            cursor.moveToNext();
                        }
                    }
                } catch(Exception e) {
                    Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                    //Utilities.reportCrash(e);
                } finally {
                    cursor.close();
                }
            }

            //Creating list of file names of new and deleted tracks
            if (!storageFileNamesList.isEmpty()) {
                if (libraryFileNamesList != null && !libraryFileNamesList.isEmpty()) {
                    //Getting all newly added tracks
                    newFileNamesList = new ArrayList<String>(storageFileNamesList);
                    newFileNamesList.removeAll(libraryFileNamesList);

                    //Getting all deleted tracks
                    deletedFileNamesList = new ArrayList<String>(libraryFileNamesList);
                    deletedFileNamesList.removeAll(storageFileNamesList);
                } else {
                    newFileNamesList = storageFileNamesList;
                }

                //Checking if there are any new tracks
                if(!newFileNamesList.isEmpty()) {
                    //Creating list of new tracks from new file name list
                    cursors = getNewTracksFromProvider(context, newFileNamesList);
                    newTracksList = createTrackListFromCursor(cursors);
                }
            } else {
                deletedFileNamesList = libraryFileNamesList;
            }

            if ((newTracksList != null && !newTracksList.isEmpty()) ||
                    (deletedFileNamesList != null && !deletedFileNamesList.isEmpty())) {
                map = new HashMap<String, ArrayList<?>>();
                map.put(MediaPlayerConstants.KEY_NEW_TRACKS_LIST, newTracksList);
                map.put(MediaPlayerConstants.KEY_DELETED_TRACKS_LIST, deletedFileNamesList);

                if (newTracksList != null && !newTracksList.isEmpty()) {
                    Log.d("New tracks", String.valueOf(newTracksList.size()));
                }

                if (deletedFileNamesList != null && !deletedFileNamesList.isEmpty()) {
                    Log.d("Deleted tracks", String.valueOf(deletedFileNamesList.size()));
                }
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return map;
    }

    public static boolean isUserPlaylistEmpty() {
        return (selectedPlaylist == null || selectedPlaylist.isEmpty());
    }
}
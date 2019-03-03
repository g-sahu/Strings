package com.mediaplayer.strings.dao;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.mediaplayer.strings.adapters.SongsListAdapter;
import com.mediaplayer.strings.beans.Playlist;
import com.mediaplayer.strings.beans.Track;

import java.util.ArrayList;
import java.util.Iterator;

import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.mediaplayer.strings.R.id;
import static com.mediaplayer.strings.fragments.SongsFragment.trackListView;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.*;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_SQL;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.TAG_PLAYLIST_LIBRARY;
import static com.mediaplayer.strings.utilities.MessageConstants.*;
import static com.mediaplayer.strings.utilities.SQLConstants.*;
import static com.mediaplayer.strings.utilities.Utilities.getCurrentDate;
import static com.mediaplayer.strings.utilities.Utilities.isNotNullOrEmpty;
import static java.lang.String.valueOf;

public class MediaPlayerDAO {
    private static SQLiteDatabase db;
    private MediaPlayerDBHelper mDbHelper;
    private Context context;

    public MediaPlayerDAO(Context context) {
        this.context = context;
        mDbHelper = new MediaPlayerDBHelper(context);
        db = mDbHelper.getWritableDatabase();
    }

    //Closes SQLite database connection
    public void closeConnection() {
        if(mDbHelper != null) {
            mDbHelper.close();
        }

        if(db != null) {
            db.close();
        }
    }

    //Add to playlist
    public void addToPlaylists(ArrayList<Playlist> selectedPlaylists, Track selectedTrack) {
        SQLiteStatement insertStmt, updateStmt;
        String toastText;
        int trackID, playlistID, playlistSize, newPlaylistSize, playlistDuration, newPlaylistDuration, increment = ONE;

        try {
            insertStmt = db.compileStatement(SQL_INSERT_PLAYLIST_DETAIL);

            //Retrieving selected track from trackInfoList
            trackID = selectedTrack.getTrackID();

            for(Playlist playlist: selectedPlaylists) {
                //Fetching current values for the selected playlist
                playlistID = playlist.getPlaylistID();
                playlistSize = playlist.getPlaylistSize();
                playlistDuration = playlist.getPlaylistDuration();
                newPlaylistSize = playlistSize + increment;
                newPlaylistDuration = playlistDuration + selectedTrack.getTrackDuration();

                //Making an entry in table 'Playlist_Detail' for the selected playlist
                insertStmt.clearBindings();
                insertStmt.bindLong(1, playlistID);
                insertStmt.bindLong(2, trackID);
                Log.d(LOG_TAG_SQL, insertStmt.toString());
                insertStmt.execute();

                //Updating table 'Playlists' for the selected playlist with new values of 'playlistSize' and 'playlistDuration'
                updateStmt = db.compileStatement(SQL_UPDATE_PLAYLIST);
                updateStmt.bindLong(1, newPlaylistSize);
                updateStmt.bindLong(2, newPlaylistDuration);
                updateStmt.bindString(3, getCurrentDate());
                updateStmt.bindLong(4, playlistID);
                Log.d(LOG_TAG_SQL, updateStmt.toString());
                updateStmt.execute();

                //Setting new values for 'playlistSize' and 'playlistDuration' for selected playlist
                playlist.setPlaylistSize(newPlaylistSize);
                playlist.setPlaylistDuration(newPlaylistDuration);
                getPlaylistInfoList().set(playlist.getPlaylistIndex(), playlist);

                //If selected playlist is default playlist 'Favourites' then updating fav_sw in table 'Tracks' and trackInfoList
                if(playlistID == PLAYLIST_ID_FAVOURITES) {
                    //Updating fav_sw in table Tracks
                    updateStmt = db.compileStatement(SQL_UPDATE_TRACK_FAV_SW);
                    updateStmt.bindLong(1, FAV_SW_YES);
                    updateStmt.bindLong(2, trackID);
                    Log.d(LOG_TAG_SQL, updateStmt.toString());
                    updateStmt.execute();

                    //Setting favSw in trackInfoList
                    selectedTrack.setFavSw(FAV_SW_YES);
                    getTrackInfoList().set(selectedTrack.getTrackIndex(), selectedTrack);
                }
            }

            //Setting success toast message
            toastText = selectedPlaylists.contains(getPlaylistByIndex(PLAYLIST_INDEX_FAVOURITES)) ? ADDED_TO_FAVOURITES : ADDED_TO_PLAYLISTS;
        } catch(SQLiteConstraintException sqle) {
            Log.e(LOG_TAG_EXCEPTION, sqle.getMessage());
            //Utilities.reportCrash(sqle);

            toastText = ERROR_DUPLICATE_TRACK_FAVOURITES;
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);

            //Setting error toast message
            toastText = ERROR;
        }

        //Displaying toast message to user
        Toast toast = makeText(context, toastText, LENGTH_SHORT);
        toast.show();
    }

    public void removeFromPlaylist(Playlist selectedPlaylist, Track selectedTrack) {
        SQLiteStatement updateStmt;
        String toastText;
        int trackID, playlistID, playlistSize, newPlaylistSize, playlistDuration, newPlaylistDuration;

        try {
            //Fetching existing values for selected playlist
            trackID = selectedTrack.getTrackID();
            playlistID = selectedPlaylist.getPlaylistID();
            playlistSize = selectedPlaylist.getPlaylistSize();
            playlistDuration = selectedPlaylist.getPlaylistDuration();

            //Deleting record from table 'Playlist_Detail'
            String args[] = {valueOf(playlistID), valueOf(trackID)};
            db.execSQL(SQL_DELETE_TRACK_FROM_PLAYLIST, args);
            Log.d(LOG_TAG_SQL, SQL_DELETE_TRACK_FROM_PLAYLIST);

            newPlaylistSize = playlistSize - 1;
            newPlaylistDuration = playlistDuration - selectedTrack.getTrackDuration();

            //Updating table 'Playlists' for the selected playlist with new values of 'playlistSize' and 'playlistDuration'
            updateStmt = db.compileStatement(SQL_UPDATE_PLAYLIST);
            updateStmt.bindLong(1, newPlaylistSize);
            updateStmt.bindLong(2, newPlaylistDuration);
            updateStmt.bindString(3, getCurrentDate());
            updateStmt.bindLong(4, playlistID);
            Log.d(LOG_TAG_SQL, updateStmt.toString());
            updateStmt.execute();

            //Setting new values for 'playlistSize' and 'playlistDuration' for selected playlist
            selectedPlaylist.setPlaylistSize(newPlaylistSize);
            selectedPlaylist.setPlaylistDuration(newPlaylistDuration);
            getPlaylistInfoList().set(selectedPlaylist.getPlaylistIndex(), selectedPlaylist);

            //If selected playlist is default playlist 'Favourites' then updating fav_sw in table 'Tracks' and trackInfoList
            if(playlistID == PLAYLIST_ID_FAVOURITES) {
                //Updating fav_sw to 0 in table 'Tracks'
                updateStmt = db.compileStatement(SQL_UPDATE_TRACK_FAV_SW);
                updateStmt.bindLong(1, FAV_SW_NO);
                updateStmt.bindLong(2, trackID);
                Log.d(LOG_TAG_SQL, updateStmt.toString());
                updateStmt.execute();

                //Setting favSw in trackInfoList
                selectedTrack.setFavSw(FAV_SW_NO);
                getTrackInfoList().set(selectedTrack.getTrackIndex(), selectedTrack);

                //Setting success toast message
                toastText = REMOVED_FROM_FAVOURITES;
            } else {
                //Setting success toast message
                toastText = REMOVED_FROM_PLAYLIST;
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);

            //Setting error toast message
            toastText = ERROR;
        }

        //Displaying toast message to user
        Toast toast = makeText(context, toastText, LENGTH_SHORT);
        toast.show();
    }

    public static int updateTrackIndices() {
        int tracksUpdated = 0;

        try (SQLiteStatement updateStmt = db.compileStatement(SQL_UPDATE_TRACK_INDICES);) {
            //Updating the indices of all the tracks
            for(Track track: getTrackInfoList()) {
                updateStmt.bindLong(1, track.getTrackIndex());
                updateStmt.bindString(2, getCurrentDate());
                updateStmt.bindLong(3, track.getTrackID());

                Log.d(LOG_TAG_SQL, updateStmt.toString());
                updateStmt.execute();
                updateStmt.clearBindings();
                tracksUpdated++;
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return tracksUpdated;
    }

    public void createPlaylist(Playlist playlist) {
        SQLiteStatement insertStmt;
        int playlistID;
        String toastText;

        try {
            //Inserting new playlist into table 'Playlists'
            insertStmt = db.compileStatement(SQL_INSERT_PLAYLIST);

            //Setting playlist_index to current max index + 1 initially
            insertStmt.bindLong(1, getPlaylistInfoListSize());
            insertStmt.bindString(2, playlist.getPlaylistName());
            insertStmt.bindLong(3, playlist.getPlaylistSize());
            insertStmt.bindLong(4, playlist.getPlaylistDuration());
            insertStmt.bindString(5, getCurrentDate());
            Log.d(LOG_TAG_SQL, insertStmt.toString());
            playlistID = (int) insertStmt.executeInsert();

            //Setting playlistID of newly created playlist
            playlist.setPlaylistID(playlistID);

            //Adding new playlist to playlistInfoList
            addPlaylist(playlist);

            //Updating indices of all the playlists in table 'Playlists'
            updatePlaylistIndices();

            //Setting success toast message
            toastText = PLAYLIST_CREATED;
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);

            //Setting error toast message
            toastText = ERROR;
        }

        Toast toast = makeText(context, toastText, LENGTH_SHORT);
        toast.show();
    }

    public void renamePlaylist(Playlist selectedPlaylist) {
        SQLiteStatement updateStmt;
        String toastText;

        try {
            //Updating the title of the renamed playlist
            updateStmt = db.compileStatement(SQL_UPDATE_PLAYLIST_TITLE);
            updateStmt.bindString(1, selectedPlaylist.getPlaylistName());
            updateStmt.bindString(2, getCurrentDate());
            updateStmt.bindLong(3, selectedPlaylist.getPlaylistID());
            Log.d(LOG_TAG_SQL, updateStmt.toString());
            updateStmt.execute();

            //Updating indices of all the playlists in table 'Playlists'
            updatePlaylistIndices();

            //Setting success toast message
            toastText = PLAYLIST_RENAMED;
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);

            //Setting error toast message
            toastText = ERROR;
        }

        //Displaying toast message to user
        Toast toast = makeText(context, toastText, LENGTH_SHORT);
        toast.show();
    }

    private void updatePlaylistIndices() {
        SQLiteStatement updateStmt;
        updateStmt = db.compileStatement(SQL_UPDATE_PLAYLIST_INDICES);

        for(Playlist playlist: getPlaylistInfoList()) {
            //Updating the indices of all the playlists
            updateStmt.bindLong(1, playlist.getPlaylistIndex());
            updateStmt.bindString(2, getCurrentDate());
            updateStmt.bindLong(3, playlist.getPlaylistID());
            Log.d(LOG_TAG_SQL, updateStmt.toString());
            updateStmt.execute();
            updateStmt.clearBindings();
        }
    }

    public void deletePlaylist(Playlist playlist) {
        SQLiteStatement deleteStmt;
        String toastText;
        int playlistID = playlist.getPlaylistID();

        try {
            //Deleting playlist from table 'Playlist_Detail'
            deleteStmt = db.compileStatement(SQL_DELETE_PLAYLIST_FROM_PLAYLIST_DETAIL);
            deleteStmt.bindLong(1, playlistID);
            Log.d(LOG_TAG_SQL, deleteStmt.toString());
            deleteStmt.executeUpdateDelete();

            //Deleting playlist from table 'Playlist'
            deleteStmt = db.compileStatement(SQL_DELETE_FROM_PLAYLISTS);
            deleteStmt.bindLong(1, playlistID);
            Log.d(LOG_TAG_SQL, deleteStmt.toString());
            deleteStmt.executeUpdateDelete();

            //Removing playlist from playlistInfoList
            removePlaylist(playlist.getPlaylistIndex());

            //Updating playlist_index for all playlists
            updatePlaylistIndices();

            //Setting success toast message
            toastText = PLAYLIST_DELETED;
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);

            //Setting error toast message
            toastText = ERROR;
        }

        //Displaying toast message to user
        Toast toast = makeText(context, toastText, LENGTH_SHORT);
        toast.show();
    }

    public void addTracks(ArrayList<Track> selectedTracks, Playlist selectedPlaylist) {
        SQLiteStatement insertStmt, updateStmt = null;
        String toastText;
        int trackID, trackDuration, trackIndex, playlistID, playlistIndex, playlistSize, playlistDuration;

        try {
            //Retrieving current values for selected playlist
            playlistID = selectedPlaylist.getPlaylistID();
            playlistIndex = selectedPlaylist.getPlaylistIndex();
            playlistSize = selectedPlaylist.getPlaylistSize();
            playlistDuration = selectedPlaylist.getPlaylistDuration();
            insertStmt = db.compileStatement(SQL_INSERT_PLAYLIST_DETAIL);

            //Checking if selected playlist is default playlist 'Favourites'
            updateStmt = (playlistID == PLAYLIST_ID_FAVOURITES) ? db.compileStatement(SQL_UPDATE_TRACK_FAV_SW) : null;

            for(Track track: selectedTracks) {
                trackID = track.getTrackID();
                trackIndex = track.getTrackIndex();
                trackDuration = track.getTrackDuration();

                //Inserting in table 'Playlist_Detail'
                insertStmt.clearBindings();
                insertStmt.bindLong(1, playlistID);
                insertStmt.bindLong(2, trackID);
                Log.d(LOG_TAG_SQL, insertStmt.toString());
                insertStmt.executeInsert();

                //If selected playlist is 'Favourites', updating 'fav_sw' in table 'Tracks'
                if(playlistID == PLAYLIST_ID_FAVOURITES) {
                    updateStmt.clearBindings();
                    updateStmt.bindLong(1, FAV_SW_YES);
                    updateStmt.bindLong(2, trackID);
                    Log.d(LOG_TAG_SQL, updateStmt.toString());
                    updateStmt.executeUpdateDelete();

                    //Updating trackInfoList with new value if favSw
                    track.setFavSw(FAV_SW_YES);
                    updateTrackInfoList(trackIndex, track);
                }

                //Calculating new values for selected playlist
                playlistSize++;
                playlistDuration = playlistDuration + trackDuration;
            }

            //Updating selected playlist with new values of playlist_size and playlist_duration
            updateStmt = db.compileStatement(SQL_UPDATE_PLAYLIST);
            updateStmt.bindLong(1, playlistSize);
            updateStmt.bindLong(2, playlistDuration);
            updateStmt.bindString(3, getCurrentDate());
            updateStmt.bindLong(4, playlistID);
            Log.d(LOG_TAG_SQL, updateStmt.toString());
            updateStmt.executeUpdateDelete();

            //Updating playlistInfoList with new values of playlistSize and playlistDuration
            selectedPlaylist.setPlaylistSize(playlistSize);
            selectedPlaylist.setPlaylistDuration(playlistDuration);
            updatePlaylistInfoList(playlistIndex, selectedPlaylist);

            //Set success toast message
            toastText = ADDED_TRACKS;
        } catch(SQLiteConstraintException sqle) {
            Log.e(LOG_TAG_SQL, sqle.getMessage());
            //Utilities.reportCrash(sqle);

            toastText = ERROR_DUPLICATE_TRACK_FAVOURITES;
        } catch(Exception e) {
            Log.e(LOG_TAG_SQL, e.getMessage());
            //Utilities.reportCrash(e);

            //Setting error toast message
            toastText = ERROR;
        }

        //Displaying toast message to user
        Toast toast = makeText(context, toastText, LENGTH_SHORT);
        toast.show();
    }

    public void addTracksToLibrary(@NonNull ArrayList<Track> trackList) {
        int c;
        long tracksAdded = 0;

        SQLiteStatement insertStmt = db.compileStatement(SQL_INSERT_TRACK);

        //Inserting tracks in table 'Tracks'
        for(Track track: trackList) {
            c = ONE;

            insertStmt.bindString(c++, track.getTrackTitle());
            insertStmt.bindLong(c++, track.getTrackIndex());
            insertStmt.bindString(c++, track.getFileName());
            insertStmt.bindLong(c++, track.getTrackDuration());
            insertStmt.bindLong(c++, track.getFileSize());
            insertStmt.bindString(c++, track.getAlbumName());
            insertStmt.bindString(c++, track.getArtistName());
            insertStmt.bindBlob(c++, track.getAlbumArt());
            insertStmt.bindString(c++, track.getTrackLocation());
            insertStmt.bindLong(c++, track.isFavSw());
            insertStmt.bindString(c, getCurrentDate());

            Log.d(LOG_TAG_SQL, insertStmt.toString());

            try {
                insertStmt.executeInsert();
                insertStmt.clearBindings();
                ++tracksAdded;
            } catch(SQLException sqle) {
                Log.e(LOG_TAG_EXCEPTION, sqle.getMessage());
                //Utilities.reportCrash(sqle);
            }
        }

        Log.d("Tracks added to library", valueOf(tracksAdded));
    }

    public void deleteTracksFromLibrary(@NonNull ArrayList<String> deletedTracksList) {
        int tracksDeleted;
        Iterator<String> deletedTracksIterator = deletedTracksList.iterator();
        StringBuilder fileNames = new StringBuilder();

        while(deletedTracksIterator.hasNext()) {
            fileNames.append(DOUBLE_QUOTE).append(deletedTracksIterator.next()).append(DOUBLE_QUOTE);

            if(deletedTracksIterator.hasNext()) {
                fileNames.append(COMMA_SEP);
            }
        }

        SQLiteStatement deleteStmt = db.compileStatement(SQL_DELETE_TRACK_FOR_FILENAME + fileNames + ")");
        tracksDeleted = deleteStmt.executeUpdateDelete();

        Log.d("Tracks deleted", valueOf(tracksDeleted));
    }

    public ArrayList<Track> getTracksForPlaylist(int playlistID) {
        ArrayList<Track> trackList = new ArrayList<>();
        String args[] = {valueOf(playlistID)};
        Log.d(LOG_TAG_SQL, SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST);

        try (Cursor playlistsCursor = db.rawQuery(SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST, args)) {
            playlistsCursor.moveToFirst();
            int c;

            while(!playlistsCursor.isAfterLast()) {
                Track track = new Track();
                c = ZERO;

                track.setTrackID(playlistsCursor.getInt(c++));
                track.setTrackTitle(playlistsCursor.getString(c++));
                track.setTrackIndex(playlistsCursor.getInt(c++));
                track.setFileName(playlistsCursor.getString(c++));
                track.setTrackDuration(playlistsCursor.getInt(c++));
                track.setFileSize(playlistsCursor.getInt(c++));
                track.setAlbumName(playlistsCursor.getString(c++));
                track.setArtistName(playlistsCursor.getString(c++));
                track.setAlbumArt(playlistsCursor.getBlob(c++));
                track.setTrackLocation(playlistsCursor.getString(c++));
                track.setFavSw(playlistsCursor.getInt(c));

                trackList.add(track);
                playlistsCursor.moveToNext();
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return trackList;
    }

    public ArrayList<Integer> getTrackIDsForPlaylist(int playlistID) {
        ArrayList<Integer> trackList = null;
        String args[] = {valueOf(playlistID)};
        Log.d(LOG_TAG_SQL, SQL_SELECT_PLAYLISTS_FOR_TRACK);

        try (Cursor cursor = db.rawQuery(SQL_SELECT_TRACK_IDS_FOR_PLAYLIST, args);) {
            if(isNotNullOrEmpty(cursor)) {
                trackList = new ArrayList<>();
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    trackList.add(cursor.getInt(ZERO));
                    cursor.moveToNext();
                }
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return trackList;
    }

    /**
     * Method to get the list of playlists from database
     * @return Sorted list of Playlists
     */
    public ArrayList<Playlist> getPlaylists() {
        ArrayList<Playlist> playlistInfoList = new ArrayList<>();

        Log.d(LOG_TAG_SQL, SQL_SELECT_PLAYLISTS);
        Cursor playlistsCursor = db.rawQuery(SQL_SELECT_PLAYLISTS, null);
        playlistsCursor.moveToFirst();

        while(!playlistsCursor.isAfterLast()) {
            Playlist playlist = new Playlist();
            playlist.setPlaylistID(playlistsCursor.getInt(0));
            playlist.setPlaylistIndex(playlistsCursor.getInt(1));
            playlist.setPlaylistName(playlistsCursor.getString(2));
            playlist.setPlaylistSize(playlistsCursor.getInt(3));
            playlist.setPlaylistDuration(playlistsCursor.getInt(4));

            playlistInfoList.add(playlist);
            playlistsCursor.moveToNext();
        }

        playlistsCursor.close();
        return playlistInfoList;
    }

    //Instantiates a new track list and populates it from table 'Tracks'
    public ArrayList<Track> getTracks() {
        ArrayList<Track> trackInfoList = null;
        Track track;
        int c;

        Log.d(LOG_TAG_SQL, SQL_SELECT_TRACKS);
        Cursor tracksCursor = db.rawQuery(SQL_SELECT_TRACKS, null);

        if(isNotNullOrEmpty(tracksCursor)) {
            tracksCursor.moveToFirst();
            trackInfoList = new ArrayList<>();

            while(!tracksCursor.isAfterLast()) {
                track = new Track();
                c = ZERO;

                track.setTrackID(tracksCursor.getInt(c++));
                track.setTrackTitle(tracksCursor.getString(c++));
                track.setTrackIndex(tracksCursor.getInt(c++));
                track.setFileName(tracksCursor.getString(c++));
                track.setTrackDuration(tracksCursor.getInt(c++));
                track.setFileSize(tracksCursor.getInt(c++));
                track.setAlbumName(tracksCursor.getString(c++));
                track.setArtistName(tracksCursor.getString(c++));
                track.setAlbumArt(tracksCursor.getBlob(c++));
                track.setTrackLocation(tracksCursor.getString(c++));
                track.setFavSw(tracksCursor.getInt(c));

                trackInfoList.add(track);
                tracksCursor.moveToNext();
            }
        }

        tracksCursor.close();
        return trackInfoList;
    }

    public ArrayList<Integer> getPlaylistsForTrack(int trackID) {
        ArrayList<Integer> playlist = null;
        String args[] = {valueOf(trackID)};

        //Fetching existing values for selected playlist
        Log.d(LOG_TAG_SQL, SQL_SELECT_PLAYLISTS_FOR_TRACK);

        try (Cursor cursor = db.rawQuery(SQL_SELECT_PLAYLISTS_FOR_TRACK, args);) {
            if(isNotNullOrEmpty(cursor)) {
                playlist = new ArrayList<>();
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    playlist.add(cursor.getInt(ZERO));
                    cursor.moveToNext();
                }
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        return playlist;
    }

    public ArrayList<String> getFileNamesFromLibrary() {
        ArrayList<String> fileNamesList = null;
        int fileNamesListSize = 0;

        Log.d(LOG_TAG_SQL, SQL_SELECT_FILE_NAMES);

        try (Cursor tracksCursor = db.rawQuery(SQL_SELECT_FILE_NAMES, null);) {
            if(isNotNullOrEmpty(tracksCursor)) {
                fileNamesList = new ArrayList<>();
                tracksCursor.moveToFirst();

                while(!tracksCursor.isAfterLast()) {
                    fileNamesList.add(tracksCursor.getString(ZERO));
                    tracksCursor.moveToNext();
                }

                fileNamesListSize = fileNamesList.size();
            }
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        Log.d("File names fetched", valueOf(fileNamesListSize));
        return fileNamesList;
    }

    public static class UpdateTracksTask extends AsyncTask<Track, Void, Integer> {
        private Activity activity;

        public UpdateTracksTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(Track... selectedTracks) {
            SQLiteStatement updateStmt, deleteStmt;
            String toastText;
            int trackID, playlistID, playlistSize, newPlaylistSize, playlistDuration, newPlaylistDuration, trackDuration, tracksUpdated = 0;
            Playlist playlist;
            Track selectedTrack = selectedTracks[0];
            trackID = selectedTrack.getTrackID();
            trackDuration = selectedTrack.getTrackDuration();
            String args[] = {valueOf(trackID)};
            String currentDate = getCurrentDate();

            //Fetching existing values for selected playlist
            Log.d(LOG_TAG_SQL, SQL_SELECT_PLAYLIST_INDICES_FOR_TRACK);

            try (Cursor cursor = db.rawQuery(SQL_SELECT_PLAYLIST_INDICES_FOR_TRACK, args);) {
                updateStmt = db.compileStatement(SQL_UPDATE_PLAYLIST);
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    playlist = getPlaylistByIndex(cursor.getInt(ZERO));
                    playlistID = playlist.getPlaylistID();
                    playlistSize = playlist.getPlaylistSize();
                    playlistDuration = playlist.getPlaylistDuration();

                    //Calculating new values for selected playlist
                    newPlaylistSize = playlistSize - ONE;
                    newPlaylistDuration = playlistDuration - trackDuration;

                    //Updating table 'Playlists' for the selected playlist with new values of 'playlistSize' and 'playlistDuration'
                    updateStmt.clearBindings();
                    updateStmt.bindLong(1, newPlaylistSize);
                    updateStmt.bindLong(2, newPlaylistDuration);
                    updateStmt.bindString(3, currentDate);
                    updateStmt.bindLong(4, playlistID);
                    Log.d(LOG_TAG_SQL, updateStmt.toString());
                    updateStmt.execute();

                    //Setting new values for 'playlistSize' and 'playlistDuration' for selected playlist
                    playlist.setPlaylistSize(newPlaylistSize);
                    playlist.setPlaylistDuration(newPlaylistDuration);
                    getPlaylistInfoList().set(playlist.getPlaylistIndex(), playlist);

                    cursor.moveToNext();
                }

                //Deleting track from table 'Playlist_Detail'
                deleteStmt = db.compileStatement(SQL_DELETE_TRACK_FROM_PLAYLIST_DETAIL);
                deleteStmt.bindLong(1, trackID);
                Log.d(LOG_TAG_SQL, deleteStmt.toString());
                deleteStmt.execute();

                //Deleting track from table 'Tracks'
                deleteStmt = db.compileStatement(SQL_DELETE_FROM_TRACKS);
                deleteStmt.bindLong(1, trackID);
                Log.d(LOG_TAG_SQL, deleteStmt.toString());
                deleteStmt.execute();

                //Removing selected track from the list of tracks
                removeTrack(TAG_PLAYLIST_LIBRARY, selectedTrack.getTrackIndex());

                //Sorting the list of tracks to update track indices
                sortTracklist(TAG_PLAYLIST_LIBRARY);

                //Updating track indices in db
                tracksUpdated = updateTrackIndices();

                //Setting success toast message
                toastText = REMOVED_FROM_LIBRARY;
            } catch(Exception e) {
                Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                //Utilities.reportCrash(e);

                //Setting error toast message
                toastText = ERROR;
            }

            return tracksUpdated;
        }

        protected void onPostExecute(Integer result) {
            //Updating songs list view adapter
            updateSongsListAdapter();

            Toast toast = makeText(activity, "Tracks updated", LENGTH_SHORT);
            toast.show();
        }

        private void updateSongsListAdapter() {
            ArrayList<Track> trackList = getTrackInfoList();

            if(trackList.isEmpty()) {
                TextView emptyLibraryMessage = activity.findViewById(id.emptyLibraryMessage);
                emptyLibraryMessage.setVisibility(VISIBLE);
            }

            SongsListAdapter adapter = new SongsListAdapter(activity, trackList);
            RecyclerView recyclerView = trackListView;
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}

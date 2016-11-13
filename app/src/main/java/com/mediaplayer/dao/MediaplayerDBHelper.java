package com.mediaplayer.dao;

import android.content.Context;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.mediaplayer.beans.Track;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.SQLConstants;
import com.mediaplayer.utilities.Utilities;

import java.util.ArrayList;
import java.util.Iterator;

public class MediaplayerDBHelper extends SQLiteOpenHelper {
    private Resources resources;
    private static String LOG_TAG_SQL = "Executing query";
    private static String LOG_TAG_EXCEPTION = "Exception";

    public MediaplayerDBHelper(Context context) {
        super(context, MediaplayerContract.DATABASE_NAME, null, MediaplayerContract.DATABASE_VERSION);
        resources = context.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SQLiteStatement insertStmt = null;
        ArrayList<Track> trackList;
        Iterator<Track> trackIterator;
        Track track;
        int c, tracksInserted = 0;

        try {
            Log.d(LOG_TAG_SQL, SQLConstants.SQL_CREATE_TRACKS);
            db.execSQL(SQLConstants.SQL_CREATE_TRACKS);

            Log.d(LOG_TAG_SQL, SQLConstants.SQL_CREATE_PLAYLISTS);
            db.execSQL(SQLConstants.SQL_CREATE_PLAYLISTS);

            Log.d(LOG_TAG_SQL, SQLConstants.SQL_CREATE_PLAYLIST_DETAIL);
            db.execSQL(SQLConstants.SQL_CREATE_PLAYLIST_DETAIL);

            //Creating default playlist 'Favourites'
            insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_PLAYLIST);

            insertStmt.bindLong(1, SQLConstants.PLAYLIST_INDEX_FAVOURITES);
            insertStmt.bindString(2, SQLConstants.PLAYLIST_TITLE_FAVOURITES);
            insertStmt.bindLong(3, SQLConstants.ZERO);
            insertStmt.bindLong(4, SQLConstants.ZERO);
            insertStmt.bindString(5, Utilities.getCurrentDate());

            Log.d(LOG_TAG_SQL, insertStmt.toString());
            insertStmt.execute();

            //Fetching tracks from storage
            trackList = MediaLibraryManager.populateTrackInfoList(resources);

            //Inserting tracks in table 'Tracks'
            insertStmt = db.compileStatement(SQLConstants.SQL_INSERT_TRACK);

            if(trackList != null && !trackList.isEmpty()) {
                trackIterator = trackList.iterator();

                while (trackIterator.hasNext()) {
                    track = trackIterator.next();
                    c = SQLConstants.ONE;

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
                    insertStmt.bindString(c, Utilities.getCurrentDate());

                    Log.d(LOG_TAG_SQL, insertStmt.toString());

                    try {
                        insertStmt.executeInsert();
                        ++tracksInserted;
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                        Log.e(LOG_TAG_EXCEPTION, sqle.getMessage());
                    }
                }

                Log.d("Tracks added to library", String.valueOf(tracksInserted));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        } finally {
            if(insertStmt != null) {
                insertStmt.close();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
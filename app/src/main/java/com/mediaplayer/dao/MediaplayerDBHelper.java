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
        try {
            Log.d(LOG_TAG_SQL, SQLConstants.SQL_CREATE_TRACKS);
            db.execSQL(SQLConstants.SQL_CREATE_TRACKS);

            Log.d(LOG_TAG_SQL, SQLConstants.SQL_CREATE_PLAYLISTS);
            db.execSQL(SQLConstants.SQL_CREATE_PLAYLISTS);

            Log.d(LOG_TAG_SQL, SQLConstants.SQL_CREATE_PLAYLIST_DETAIL);
            db.execSQL(SQLConstants.SQL_CREATE_PLAYLIST_DETAIL);

            //Creating default playlist 'Favourites'
            SQLiteStatement stmt = db.compileStatement(SQLConstants.SQL_INSERT_PLAYLIST);
            stmt.bindLong(1, SQLConstants.PLAYLIST_INDEX_FAVOURITES);
            stmt.bindString(2, SQLConstants.PLAYLIST_TITLE_FAVOURITES);
            stmt.bindLong(3, SQLConstants.ZERO);
            stmt.bindLong(4, SQLConstants.ZERO);
            stmt.bindString(5, Utilities.getCurrentDate());
            Log.d(LOG_TAG_SQL, stmt.toString());
            stmt.execute();

            //Inserting tracks in table 'Tracks'
            stmt = db.compileStatement(SQLConstants.SQL_INSERT_TRACK);
            Iterator<Track> trackIterator = MediaLibraryManager.populateTrackInfoList(resources).iterator();
            Track track;

            while(trackIterator.hasNext()) {
                track = trackIterator.next();

                stmt.bindString(1, track.getTrackTitle());
                stmt.bindLong(2, track.getTrackIndex());
                stmt.bindString(3, track.getFileName());
                stmt.bindLong(4, track.getTrackDuration());
                stmt.bindLong(5, track.getFileSize());
                stmt.bindString(6, track.getAlbumName());
                stmt.bindString(7, track.getArtistName());
                stmt.bindBlob(8, track.getAlbumArt());
                stmt.bindString(9, track.getTrackLocation());
                stmt.bindLong(10, track.isFavouriteSw());
                stmt.bindString(11, Utilities.getCurrentDate());

                Log.d(LOG_TAG_SQL, stmt.toString());

                try {
                    stmt.execute();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    Log.e(LOG_TAG_EXCEPTION, sqle.getMessage());
                }
            }

            //MediaLibraryManager.init(db);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}

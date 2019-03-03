package com.mediaplayer.strings.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import com.mediaplayer.strings.beans.Track;

import java.util.ArrayList;

import static com.mediaplayer.strings.dao.MediaPlayerContract.DATABASE_NAME;
import static com.mediaplayer.strings.dao.MediaPlayerContract.DATABASE_VERSION;
import static com.mediaplayer.strings.utilities.MediaLibraryManager.populateTrackInfoList;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_SQL;
import static com.mediaplayer.strings.utilities.SQLConstants.*;
import static com.mediaplayer.strings.utilities.Utilities.getCurrentDate;
import static com.mediaplayer.strings.utilities.Utilities.isNotNullOrEmpty;
import static java.lang.String.valueOf;

class MediaPlayerDBHelper extends SQLiteOpenHelper {
    private Context context;

    MediaPlayerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
        createFavouritesPlaylist(db);
        populateTracks(db, populateTrackInfoList(context));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    private void createSchema(SQLiteDatabase db) {
        Log.d(LOG_TAG_SQL, SQL_CREATE_TRACKS);
        db.execSQL(SQL_CREATE_TRACKS);

        Log.d(LOG_TAG_SQL, SQL_CREATE_PLAYLISTS);
        db.execSQL(SQL_CREATE_PLAYLISTS);

        Log.d(LOG_TAG_SQL, SQL_CREATE_PLAYLIST_DETAIL);
        db.execSQL(SQL_CREATE_PLAYLIST_DETAIL);
    }

    private void createFavouritesPlaylist(SQLiteDatabase db) {
        try (SQLiteStatement insertStmt = db.compileStatement(SQL_INSERT_PLAYLIST)) {
            insertStmt.bindLong(1, PLAYLIST_INDEX_FAVOURITES);
            insertStmt.bindString(2, PLAYLIST_TITLE_FAVOURITES);
            insertStmt.bindLong(3, ZERO);
            insertStmt.bindLong(4, ZERO);
            insertStmt.bindString(5, getCurrentDate());
            Log.d(LOG_TAG_SQL, insertStmt.toString());
            insertStmt.execute();
        }
    }

    private void populateTracks(SQLiteDatabase db, ArrayList<Track> trackList) {
        int c, tracksInserted = 0;

        if(isNotNullOrEmpty(trackList)) {
            try (SQLiteStatement insertStmt = db.compileStatement(SQL_INSERT_TRACK)) {
                for (Track track : trackList) {
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
                    insertStmt.executeInsert();
                    ++tracksInserted;
                }
            }

            Log.d("Tracks added to library", valueOf(tracksInserted));
        }
    }
}

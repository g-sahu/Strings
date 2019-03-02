package com.mediaplayer.strings.utilities;

import static com.mediaplayer.strings.dao.MediaPlayerContract.PlaylistDetail;
import static com.mediaplayer.strings.dao.MediaPlayerContract.Playlists;
import static com.mediaplayer.strings.dao.MediaPlayerContract.Tracks.*;

public final class SQLConstants {
    private static final String PRIMARY_KEY = " PRIMARY KEY ";
    private static final String FOREIGN_KEY = " FOREIGN KEY ";
    private static final String AUTOINCREMENT = " AUTOINCREMENT ";
    private static final String REFERENCES = " REFERENCES ";
    private static final String NOT_NULL = " NOT NULL ";
    private static final String UNIQUE = " UNIQUE ";
    private static final String TEXT = " TEXT ";
    private static final String INTEGER = " INTEGER ";
    private static final String BLOB = " BLOB ";
    public static final String COMMA_SEP = ", ";
    public static final String PLAYLIST_TITLE_FAVOURITES = "Favourites";
    static final String DD_MM_YYYY = "dd-MM-yyyy";
    static final String AND = " AND ";
    public static final String DOUBLE_QUOTE = "\"";

    public static final int PLAYLIST_ID_FAVOURITES = 1;
    public static final int PLAYLIST_INDEX_FAVOURITES = 0;
    public static final int FAV_SW_YES = 1;
    public static final int FAV_SW_NO = 0;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int HUNDRED = 100;

    // Create tables
    public static final String SQL_CREATE_TRACKS =
            "CREATE TABLE " + TABLE_NAME + " (" +
            TRACK_ID + INTEGER + NOT_NULL + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
            TRACK_TITLE + TEXT + COMMA_SEP +
            TRACK_INDEX + INTEGER + NOT_NULL + COMMA_SEP +
            FILE_NAME + TEXT + NOT_NULL + UNIQUE + COMMA_SEP +
            TRACK_DURATION + INTEGER + NOT_NULL + COMMA_SEP +
            FILE_SIZE + INTEGER + NOT_NULL + COMMA_SEP +
            ALBUM_NAME +	TEXT + COMMA_SEP +
            ARTIST_NAME + TEXT + COMMA_SEP +
            ALBUM_ART + BLOB + COMMA_SEP +
            TRACK_LOCATION + TEXT + COMMA_SEP +
            FAV_SW + INTEGER + NOT_NULL + COMMA_SEP +
            CREATE_DT + TEXT + NOT_NULL + COMMA_SEP +
            UPDATE_DT + TEXT +
            " )";

    public static final String SQL_CREATE_PLAYLISTS =
            "CREATE TABLE " + Playlists.TABLE_NAME + " (" +
            Playlists.PLAYLIST_ID + INTEGER + NOT_NULL + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
            Playlists.PLAYLIST_INDEX + INTEGER + NOT_NULL + COMMA_SEP +
            Playlists.PLAYLIST_TITLE + TEXT + NOT_NULL + UNIQUE + COMMA_SEP +
            Playlists.PLAYLIST_SIZE + INTEGER + NOT_NULL + COMMA_SEP +
            Playlists.PLAYLIST_DURATION + INTEGER + NOT_NULL + COMMA_SEP +
            Playlists.CREATE_DT + TEXT + NOT_NULL + COMMA_SEP +
            Playlists.UPDATE_DT + TEXT +
            " )";

    public static final String SQL_CREATE_PLAYLIST_DETAIL =
            "CREATE TABLE " + PlaylistDetail.TABLE_NAME + " (" +
            PlaylistDetail.PLAYLIST_ID + INTEGER + NOT_NULL + COMMA_SEP +
            PlaylistDetail.TRACK_ID + INTEGER + NOT_NULL + COMMA_SEP +
            PRIMARY_KEY + "(" + Playlists.PLAYLIST_ID + COMMA_SEP +
            TRACK_ID + ")" + COMMA_SEP +
            FOREIGN_KEY + "(" + Playlists.PLAYLIST_ID + ")" +
            REFERENCES + Playlists.TABLE_NAME + "(" +
            Playlists.PLAYLIST_ID + ")" + COMMA_SEP +
            FOREIGN_KEY + "(" + TRACK_ID + ")" +
            REFERENCES + TABLE_NAME + "(" +
            TRACK_ID + ")" +
            " )";

    //Select queries
    public static final String SQL_SELECT_TRACKS =
            "SELECT * FROM " + TABLE_NAME;

    public static final String SQL_SELECT_PLAYLISTS =
            "SELECT * FROM " + Playlists.TABLE_NAME;

    public static final String SQL_SELECT_PLAYLIST_INDICES_FOR_TRACK =
            "SELECT " + Playlists.PLAYLIST_INDEX +
            " FROM " + Playlists.TABLE_NAME +
            " WHERE " + Playlists.PLAYLIST_ID + " IN (" +
            "SELECT " + PlaylistDetail.PLAYLIST_ID + " FROM " +
            PlaylistDetail.TABLE_NAME + " WHERE " +
            PlaylistDetail.TRACK_ID + " = ?)";

    public static final String SQL_SELECT_PLAYLISTS_FOR_TRACK =
            "SELECT " + PlaylistDetail.PLAYLIST_ID +
            " FROM " + PlaylistDetail.TABLE_NAME +
            " WHERE " + PlaylistDetail.TRACK_ID + " = ? " +
            " AND " + PlaylistDetail.PLAYLIST_ID + " != " + PLAYLIST_ID_FAVOURITES;

    public static final String SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST =
            "SELECT * FROM " + TABLE_NAME + " WHERE " +
            TRACK_ID + " IN (SELECT " +
            PlaylistDetail.TRACK_ID +
            " FROM " + PlaylistDetail.TABLE_NAME +
            " WHERE " + PlaylistDetail.PLAYLIST_ID + " = ?)";

    public static final String SQL_SELECT_TRACK_IDS_FOR_PLAYLIST =
            "SELECT " + PlaylistDetail.TRACK_ID +
            " FROM " + PlaylistDetail.TABLE_NAME +
            " WHERE " + PlaylistDetail.PLAYLIST_ID + " = ?";

    public static final String SQL_SELECT_FILE_NAMES =
            "SELECT " + FILE_NAME + " FROM " + TABLE_NAME;

    public static final String SQL_SELECT_TRACK_IDS_FOR_FILE_NAMES =
            "SELECT " + TRACK_ID +
            " FROM " + TABLE_NAME +
            " WHERE " + FILE_NAME;


    //Insert queries
    public static final String SQL_INSERT_TRACK =
            "INSERT INTO " + TABLE_NAME + "(" +
            TRACK_TITLE + COMMA_SEP +
            TRACK_INDEX + COMMA_SEP +
            FILE_NAME + COMMA_SEP +
            TRACK_DURATION + COMMA_SEP +
            FILE_SIZE + COMMA_SEP +
            ALBUM_NAME + COMMA_SEP +
            ARTIST_NAME + COMMA_SEP +
            ALBUM_ART + COMMA_SEP +
            TRACK_LOCATION + COMMA_SEP +
            FAV_SW + COMMA_SEP +
            CREATE_DT +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_PLAYLIST =
            "INSERT INTO " + Playlists.TABLE_NAME + "(" +
            Playlists.PLAYLIST_INDEX + COMMA_SEP +
            Playlists.PLAYLIST_TITLE + COMMA_SEP +
            Playlists.PLAYLIST_SIZE + COMMA_SEP +
            Playlists.PLAYLIST_DURATION + COMMA_SEP +
            Playlists.CREATE_DT +
            ") VALUES (?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_PLAYLIST_DETAIL =
            "INSERT INTO " + PlaylistDetail.TABLE_NAME + "(" +
            PlaylistDetail.PLAYLIST_ID + COMMA_SEP +
            PlaylistDetail.TRACK_ID +
            ") VALUES (?, ?)";

    //Update queries
    public static final String SQL_UPDATE_PLAYLIST =
            "UPDATE " + Playlists.TABLE_NAME + " SET " +
            Playlists.PLAYLIST_SIZE + " = ?" + COMMA_SEP +
            Playlists.PLAYLIST_DURATION + " = ?" + COMMA_SEP +
            Playlists.UPDATE_DT + " = ? " +
            " WHERE " + Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_PLAYLIST_TITLE =
            "UPDATE " + Playlists.TABLE_NAME + " SET " +
            Playlists.PLAYLIST_TITLE + " = ?" + COMMA_SEP +
            Playlists.UPDATE_DT + " = ? " +
            " WHERE " + Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_PLAYLIST_INDICES =
            "UPDATE " + Playlists.TABLE_NAME + " SET " +
            Playlists.PLAYLIST_INDEX + " = ?" + COMMA_SEP +
            Playlists.UPDATE_DT + " = ? " +
            " WHERE " + Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_TRACK_INDICES =
            "UPDATE " + TABLE_NAME + " SET " +
            TRACK_INDEX + " = ?" + COMMA_SEP +
            UPDATE_DT + " = ? " +
            " WHERE " + TRACK_ID + " = ?";

    public static final String SQL_UPDATE_TRACK_FAV_SW =
            "UPDATE " + TABLE_NAME + " SET " +
            FAV_SW + " = ?" +
            " WHERE " + TRACK_ID + " = ?";

    // Delete queries
    public static final String SQL_DELETE_FROM_TRACKS =
            "DELETE FROM " + TABLE_NAME +
            " WHERE " + TRACK_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FOR_FILENAME =
            "DELETE FROM " + TABLE_NAME +
            " WHERE " + FILE_NAME + " IN (";

    public static final String SQL_DELETE_FROM_PLAYLISTS =
            "DELETE FROM " + Playlists.TABLE_NAME +
            " WHERE " + Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FROM_PLAYLIST_DETAIL =
            "DELETE FROM " + PlaylistDetail.TABLE_NAME +
            " WHERE " + PlaylistDetail.TRACK_ID + " = ?";

    public static final String SQL_DELETE_PLAYLIST_FROM_PLAYLIST_DETAIL =
            "DELETE FROM " + PlaylistDetail.TABLE_NAME +
            " WHERE " + PlaylistDetail.PLAYLIST_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FROM_PLAYLIST =
            "DELETE FROM " + PlaylistDetail.TABLE_NAME +
            " WHERE " + PlaylistDetail.PLAYLIST_ID + " = ?" +
            " AND " + PlaylistDetail.TRACK_ID + " = ?";
}

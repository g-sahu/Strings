package com.mediaplayer.strings.utilities;

import com.mediaplayer.strings.dao.MediaPlayerContract;

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
            "CREATE TABLE " + MediaPlayerContract.Tracks.TABLE_NAME + " (" +
                    MediaPlayerContract.Tracks.TRACK_ID + INTEGER + NOT_NULL + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_TITLE + TEXT + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_INDEX + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Tracks.FILE_NAME + TEXT + NOT_NULL + UNIQUE + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_DURATION + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Tracks.FILE_SIZE + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Tracks.ALBUM_NAME +	TEXT + COMMA_SEP +
                    MediaPlayerContract.Tracks.ARTIST_NAME + TEXT + COMMA_SEP +
                    MediaPlayerContract.Tracks.ALBUM_ART + BLOB + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_LOCATION + TEXT + COMMA_SEP +
                    MediaPlayerContract.Tracks.FAV_SW + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Tracks.CREATE_DT + TEXT + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Tracks.UPDATE_DT + TEXT +
                    " )";

    public static final String SQL_CREATE_PLAYLISTS =
            "CREATE TABLE " + MediaPlayerContract.Playlists.TABLE_NAME + " (" +
                    MediaPlayerContract.Playlists.PLAYLIST_ID + INTEGER + NOT_NULL + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_INDEX + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_TITLE + TEXT + NOT_NULL + UNIQUE + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_SIZE + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_DURATION + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Playlists.CREATE_DT + TEXT + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.Playlists.UPDATE_DT + TEXT +
                    " )";

    public static final String SQL_CREATE_PLAYLIST_DETAIL =
            "CREATE TABLE " + MediaPlayerContract.PlaylistDetail.TABLE_NAME + " (" +
                    MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaPlayerContract.PlaylistDetail.TRACK_ID + INTEGER + NOT_NULL + COMMA_SEP +
                    PRIMARY_KEY + "(" + MediaPlayerContract.Playlists.PLAYLIST_ID + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_ID + ")" + COMMA_SEP +
                    FOREIGN_KEY + "(" + MediaPlayerContract.Playlists.PLAYLIST_ID + ")" +
                    REFERENCES + MediaPlayerContract.Playlists.TABLE_NAME + "(" +
                    MediaPlayerContract.Playlists.PLAYLIST_ID + ")" + COMMA_SEP +
                    FOREIGN_KEY + "(" + MediaPlayerContract.Tracks.TRACK_ID + ")" +
                    REFERENCES + MediaPlayerContract.Tracks.TABLE_NAME + "(" +
                    MediaPlayerContract.Tracks.TRACK_ID + ")" +
                    " )";

    //Select queries
    public static final String SQL_SELECT_TRACKS =
            "SELECT * FROM " + MediaPlayerContract.Tracks.TABLE_NAME;

    public static final String SQL_SELECT_PLAYLISTS =
            "SELECT * FROM " + MediaPlayerContract.Playlists.TABLE_NAME;

    public static final String SQL_SELECT_PLAYLIST_INDICES_FOR_TRACK =
            "SELECT " + MediaPlayerContract.Playlists.PLAYLIST_INDEX +
                    " FROM " + MediaPlayerContract.Playlists.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.Playlists.PLAYLIST_ID + " IN (" +
                    "SELECT " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + " FROM " +
                    MediaPlayerContract.PlaylistDetail.TABLE_NAME + " WHERE " +
                    MediaPlayerContract.PlaylistDetail.TRACK_ID + " = ?)";

    public static final String SQL_SELECT_PLAYLISTS_FOR_TRACK =
            "SELECT " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID +
                    " FROM " + MediaPlayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.PlaylistDetail.TRACK_ID + " = ? " +
                    " AND " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + " != " + PLAYLIST_ID_FAVOURITES;

    public static final String SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST =
            "SELECT * FROM " + MediaPlayerContract.Tracks.TABLE_NAME + " WHERE " +
                    MediaPlayerContract.Tracks.TRACK_ID + " IN (SELECT " +
                    MediaPlayerContract.PlaylistDetail.TRACK_ID +
                    " FROM " + MediaPlayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + " = ?)";

    public static final String SQL_SELECT_TRACK_IDS_FOR_PLAYLIST =
            "SELECT " + MediaPlayerContract.PlaylistDetail.TRACK_ID +
                    " FROM " + MediaPlayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + " = ?";

    public static final String SQL_SELECT_FILE_NAMES =
            "SELECT " + MediaPlayerContract.Tracks.FILE_NAME + " FROM " + MediaPlayerContract.Tracks.TABLE_NAME;

    public static final String SQL_SELECT_TRACK_IDS_FOR_FILE_NAMES =
            "SELECT " + MediaPlayerContract.Tracks.TRACK_ID +
                    " FROM " + MediaPlayerContract.Tracks.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.Tracks.FILE_NAME;


    //Insert queries
    public static final String SQL_INSERT_TRACK =
            "INSERT INTO " + MediaPlayerContract.Tracks.TABLE_NAME + "(" +
                    MediaPlayerContract.Tracks.TRACK_TITLE + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_INDEX + COMMA_SEP +
                    MediaPlayerContract.Tracks.FILE_NAME + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_DURATION + COMMA_SEP +
                    MediaPlayerContract.Tracks.FILE_SIZE + COMMA_SEP +
                    MediaPlayerContract.Tracks.ALBUM_NAME + COMMA_SEP +
                    MediaPlayerContract.Tracks.ARTIST_NAME + COMMA_SEP +
                    MediaPlayerContract.Tracks.ALBUM_ART + COMMA_SEP +
                    MediaPlayerContract.Tracks.TRACK_LOCATION + COMMA_SEP +
                    MediaPlayerContract.Tracks.FAV_SW + COMMA_SEP +
                    MediaPlayerContract.Tracks.CREATE_DT +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_PLAYLIST =
            "INSERT INTO " + MediaPlayerContract.Playlists.TABLE_NAME + "(" +
                    MediaPlayerContract.Playlists.PLAYLIST_INDEX + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_TITLE + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_SIZE + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_DURATION + COMMA_SEP +
                    MediaPlayerContract.Playlists.CREATE_DT +
                    ") VALUES (?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_PLAYLIST_DETAIL =
            "INSERT INTO " + MediaPlayerContract.PlaylistDetail.TABLE_NAME + "(" +
                    MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + COMMA_SEP +
                    MediaPlayerContract.PlaylistDetail.TRACK_ID +
                    ") VALUES (?, ?)";

    //Update queries
    public static final String SQL_UPDATE_PLAYLIST =
            "UPDATE " + MediaPlayerContract.Playlists.TABLE_NAME + " SET " +
                    MediaPlayerContract.Playlists.PLAYLIST_SIZE + " = ?" + COMMA_SEP +
                    MediaPlayerContract.Playlists.PLAYLIST_DURATION + " = ?" + COMMA_SEP +
                    MediaPlayerContract.Playlists.UPDATE_DT + " = ? " +
                    " WHERE " + MediaPlayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_PLAYLIST_TITLE =
            "UPDATE " + MediaPlayerContract.Playlists.TABLE_NAME + " SET " +
                    MediaPlayerContract.Playlists.PLAYLIST_TITLE + " = ?" + COMMA_SEP +
                    MediaPlayerContract.Playlists.UPDATE_DT + " = ? " +
                    " WHERE " + MediaPlayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_PLAYLIST_INDICES =
            "UPDATE " + MediaPlayerContract.Playlists.TABLE_NAME + " SET " +
                    MediaPlayerContract.Playlists.PLAYLIST_INDEX + " = ?" + COMMA_SEP +
                    MediaPlayerContract.Playlists.UPDATE_DT + " = ? " +
                    " WHERE " + MediaPlayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_TRACK_INDICES =
            "UPDATE " + MediaPlayerContract.Tracks.TABLE_NAME + " SET " +
                    MediaPlayerContract.Tracks.TRACK_INDEX + " = ?" + COMMA_SEP +
                    MediaPlayerContract.Tracks.UPDATE_DT + " = ? " +
                    " WHERE " + MediaPlayerContract.Tracks.TRACK_ID + " = ?";

    public static final String SQL_UPDATE_TRACK_FAV_SW =
            "UPDATE " + MediaPlayerContract.Tracks.TABLE_NAME + " SET " +
                    MediaPlayerContract.Tracks.FAV_SW + " = ?" +
                    " WHERE " + MediaPlayerContract.Tracks.TRACK_ID + " = ?";

    // Delete queries
    public static final String SQL_DELETE_FROM_TRACKS =
            "DELETE FROM " + MediaPlayerContract.Tracks.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.Tracks.TRACK_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FOR_FILENAME =
            "DELETE FROM " + MediaPlayerContract.Tracks.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.Tracks.FILE_NAME + " IN (";

    public static final String SQL_DELETE_FROM_PLAYLISTS =
            "DELETE FROM " + MediaPlayerContract.Playlists.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FROM_PLAYLIST_DETAIL =
            "DELETE FROM " + MediaPlayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.PlaylistDetail.TRACK_ID + " = ?";

    public static final String SQL_DELETE_PLAYLIST_FROM_PLAYLIST_DETAIL =
            "DELETE FROM " + MediaPlayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FROM_PLAYLIST =
            "DELETE FROM " + MediaPlayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaPlayerContract.PlaylistDetail.PLAYLIST_ID + " = ?" +
                    " AND " + MediaPlayerContract.PlaylistDetail.TRACK_ID + " = ?";
}

package com.mediaplayer.utilities;

import com.mediaplayer.dao.MediaplayerContract;

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
    private static final String COMMA_SEP = ", ";
    public static final String PLAYLIST_TITLE_FAVOURITES = "Favourites";
    public static final String DD_MM_YYYY = "dd-MM-yyyy";

    public static final int PLAYLIST_ID_FAVOURITES = 1;
    public static final int PLAYLIST_INDEX_FAVOURITES = 0;
    public static final int FAV_SW_YES = 1;
    public static final int FAV_SW_NO = 0;
    public static final int ZERO = 0;

    // Create tables
    public static final String SQL_CREATE_TRACKS =
            "CREATE TABLE " + MediaplayerContract.Tracks.TABLE_NAME + " (" +
                    MediaplayerContract.Tracks.TRACK_ID + INTEGER + NOT_NULL + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_TITLE + TEXT + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_INDEX + INTEGER + NOT_NULL + UNIQUE + COMMA_SEP +
                    MediaplayerContract.Tracks.FILE_NAME + TEXT + NOT_NULL + UNIQUE + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_DURATION + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Tracks.FILE_SIZE + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Tracks.ALBUM_NAME +	TEXT + COMMA_SEP +
                    MediaplayerContract.Tracks.ARTIST_NAME + TEXT + COMMA_SEP +
                    MediaplayerContract.Tracks.ALBUM_ART + BLOB + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_LOCATION + TEXT + COMMA_SEP +
                    MediaplayerContract.Tracks.FAV_SW + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Tracks.CREATE_DT + TEXT + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Tracks.UPDATE_DT + TEXT +
                    " )";

    public static final String SQL_CREATE_PLAYLISTS =
            "CREATE TABLE " + MediaplayerContract.Playlists.TABLE_NAME + " (" +
                    MediaplayerContract.Playlists.PLAYLIST_ID + INTEGER + NOT_NULL + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_INDEX + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_TITLE + TEXT + NOT_NULL + UNIQUE + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_SIZE + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_DURATION + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Playlists.CREATE_DT + TEXT + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.Playlists.UPDATE_DT + TEXT +
                    " )";

    public static final String SQL_CREATE_PLAYLIST_DETAIL =
            "CREATE TABLE " + MediaplayerContract.PlaylistDetail.TABLE_NAME + " (" +
                    MediaplayerContract.PlaylistDetail.PLAYLIST_ID + INTEGER + NOT_NULL + COMMA_SEP +
                    MediaplayerContract.PlaylistDetail.TRACK_ID + INTEGER + NOT_NULL + COMMA_SEP +
                    PRIMARY_KEY + "(" + MediaplayerContract.Playlists.PLAYLIST_ID + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_ID + ")" + COMMA_SEP +
                    FOREIGN_KEY + "(" + MediaplayerContract.Playlists.PLAYLIST_ID + ")" +
                    REFERENCES + MediaplayerContract.Playlists.TABLE_NAME + "(" +
                    MediaplayerContract.Playlists.PLAYLIST_ID + ")" + COMMA_SEP +
                    FOREIGN_KEY + "(" + MediaplayerContract.Tracks.TRACK_ID + ")" +
                    REFERENCES + MediaplayerContract.Tracks.TABLE_NAME + "(" +
                    MediaplayerContract.Tracks.TRACK_ID + ")" +
                    " )";

    //Select queries
    public static final String SQL_SELECT_TRACKS =
            "SELECT * FROM " + MediaplayerContract.Tracks.TABLE_NAME;

    public static final String SQL_SELECT_PLAYLISTS =
            "SELECT * FROM " + MediaplayerContract.Playlists.TABLE_NAME;

    public static final String SQL_SELECT_ALL_PLAYLISTS_FOR_TRACK =
            "SELECT " + MediaplayerContract.Playlists.PLAYLIST_INDEX +
                    " FROM " + MediaplayerContract.Playlists.TABLE_NAME +
                    " WHERE " + MediaplayerContract.Playlists.PLAYLIST_ID + " IN (" +
                    "SELECT " + MediaplayerContract.PlaylistDetail.PLAYLIST_ID + " FROM " +
                    MediaplayerContract.PlaylistDetail.TABLE_NAME + " WHERE " +
                    MediaplayerContract.PlaylistDetail.TRACK_ID + " = ?)";

    public static final String SQL_SELECT_ALL_TRACKS_FOR_PLAYLIST =
            "SELECT * FROM " + MediaplayerContract.Tracks.TABLE_NAME + " WHERE " +
                    MediaplayerContract.Tracks.TRACK_ID + " IN (SELECT " +
                    MediaplayerContract.PlaylistDetail.TRACK_ID +
                    " FROM " + MediaplayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaplayerContract.PlaylistDetail.PLAYLIST_ID + " = ?)";

    //Insert queries
    public static final String SQL_INSERT_TRACK =
            "INSERT INTO " + MediaplayerContract.Tracks.TABLE_NAME + "(" +
                    MediaplayerContract.Tracks.TRACK_TITLE + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_INDEX + COMMA_SEP +
                    MediaplayerContract.Tracks.FILE_NAME + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_DURATION + COMMA_SEP +
                    MediaplayerContract.Tracks.FILE_SIZE + COMMA_SEP +
                    MediaplayerContract.Tracks.ALBUM_NAME + COMMA_SEP +
                    MediaplayerContract.Tracks.ARTIST_NAME + COMMA_SEP +
                    MediaplayerContract.Tracks.ALBUM_ART + COMMA_SEP +
                    MediaplayerContract.Tracks.TRACK_LOCATION + COMMA_SEP +
                    MediaplayerContract.Tracks.FAV_SW + COMMA_SEP +
                    MediaplayerContract.Tracks.CREATE_DT +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_PLAYLIST =
            "INSERT INTO " + MediaplayerContract.Playlists.TABLE_NAME + "(" +
                    MediaplayerContract.Playlists.PLAYLIST_INDEX + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_TITLE + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_SIZE + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_DURATION + COMMA_SEP +
                    MediaplayerContract.Playlists.CREATE_DT +
                    ") VALUES (?, ?, ?, ?, ?)";

    public static final String SQL_INSERT_PLAYLIST_DETAIL =
            "INSERT INTO " + MediaplayerContract.PlaylistDetail.TABLE_NAME + "(" +
                    MediaplayerContract.PlaylistDetail.PLAYLIST_ID + COMMA_SEP +
                    MediaplayerContract.PlaylistDetail.TRACK_ID +
                    ") VALUES (?, ?)";

    //Update queries
    public static final String SQL_UPDATE_PLAYLIST =
            "UPDATE " + MediaplayerContract.Playlists.TABLE_NAME + " SET " +
                    MediaplayerContract.Playlists.PLAYLIST_SIZE + " = ?" + COMMA_SEP +
                    MediaplayerContract.Playlists.PLAYLIST_DURATION + " = ?" + COMMA_SEP +
                    MediaplayerContract.Playlists.UPDATE_DT + " = ? " +
                    " WHERE " + MediaplayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_PLAYLIST_TITLE =
            "UPDATE " + MediaplayerContract.Playlists.TABLE_NAME + " SET " +
                    MediaplayerContract.Playlists.PLAYLIST_TITLE + " = ?" + COMMA_SEP +
                    MediaplayerContract.Playlists.UPDATE_DT + " = ? " +
                    " WHERE " + MediaplayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_PLAYLIST_INDICES =
            "UPDATE " + MediaplayerContract.Playlists.TABLE_NAME + " SET " +
                    MediaplayerContract.Playlists.PLAYLIST_INDEX + " = ?" + COMMA_SEP +
                    MediaplayerContract.Playlists.UPDATE_DT + " = ? " +
                    " WHERE " + MediaplayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_UPDATE_TRACK_INDICES =
            "UPDATE " + MediaplayerContract.Tracks.TABLE_NAME + " SET " +
                    MediaplayerContract.Tracks.TRACK_INDEX + " = ?" + COMMA_SEP +
                    MediaplayerContract.Tracks.UPDATE_DT + " = ? " +
                    " WHERE " + MediaplayerContract.Tracks.TRACK_ID + " = ?";

    public static final String SQL_UPDATE_TRACK_FAV_SW =
            "UPDATE " + MediaplayerContract.Tracks.TABLE_NAME + " SET " +
                    MediaplayerContract.Tracks.FAV_SW + " = ?" +
                    " WHERE " + MediaplayerContract.Tracks.TRACK_ID + " = ?";

    // Delete queries
    public static final String SQL_DELETE_FROM_TRACKS =
            "DELETE FROM " + MediaplayerContract.Tracks.TABLE_NAME +
                    " WHERE " + MediaplayerContract.Tracks.TRACK_ID + " = ?";

    public static final String SQL_DELETE_FROM_PLAYLISTS =
            "DELETE FROM " + MediaplayerContract.Playlists.TABLE_NAME +
                    " WHERE " + MediaplayerContract.Playlists.PLAYLIST_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FROM_PLAYLIST_DETAIL =
            "DELETE FROM " + MediaplayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaplayerContract.PlaylistDetail.TRACK_ID + " = ?";

    public static final String SQL_DELETE_PLAYLIST_FROM_PLAYLIST_DETAIL =
            "DELETE FROM " + MediaplayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaplayerContract.PlaylistDetail.PLAYLIST_ID + " = ?";

    public static final String SQL_DELETE_TRACK_FROM_FAVOURITES =
            "DELETE FROM " + MediaplayerContract.PlaylistDetail.TABLE_NAME +
                    " WHERE " + MediaplayerContract.PlaylistDetail.PLAYLIST_ID + " = ?" +
                    " AND " + MediaplayerContract.PlaylistDetail.TRACK_ID + " = ?";

    //Drop table queries
    public static final String SQL_DELETE_TRACKS = "DROP TABLE IF EXISTS " + MediaplayerContract.Tracks.TABLE_NAME;
    public static final String SQL_DELETE_PLAYLISTS = "DROP TABLE IF EXISTS " + MediaplayerContract.Playlists.TABLE_NAME;
    public static final String SQL_DELETE_PLAYLIST_DETAIL = "DROP TABLE IF EXISTS " + MediaplayerContract.PlaylistDetail.TABLE_NAME;
}

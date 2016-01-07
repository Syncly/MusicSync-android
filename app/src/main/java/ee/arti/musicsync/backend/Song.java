package ee.arti.musicsync.backend;

import android.content.ContentValues;
import android.database.Cursor;

public class Song extends Playlist {

    private String url;
    private String playlistId;

    public static final String[] columns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_TYPE,
            DatabaseHelper.COLUMN_URL,
            DatabaseHelper.COLUMN_PLAYLIST_ID,
            DatabaseHelper.COLUMN_STATUS_TYPE,
            DatabaseHelper.COLUMN_STATUS_PROGRESS
    };

    public Song(String id, String name, String type, String url, String playlistId, String statusType, String statusProgress) {
        super(id, name, type, statusType, statusProgress);
        this.setType(url);
        this.setPlaylistId(playlistId);
    }

    public Song(Cursor cursor) {
        this.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
        this.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
        this.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE)));
        this.setUrl(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_URL)));
        this.setPlaylistId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYLIST_ID)));
        this.setStatusType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS_TYPE)));
        this.setStatusProgress(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS_PROGRESS)));
    }

    public ContentValues getValues() {
        ContentValues content = new ContentValues();
        content.put(DatabaseHelper.COLUMN_ID, getId());
        content.put(DatabaseHelper.COLUMN_NAME, getName());
        content.put(DatabaseHelper.COLUMN_TYPE, getType());
        content.put(DatabaseHelper.COLUMN_URL, getUrl());
        content.put(DatabaseHelper.COLUMN_PLAYLIST_ID, getPlaylistId());
        content.put(DatabaseHelper.COLUMN_STATUS_TYPE, getStatusType());
        content.put(DatabaseHelper.COLUMN_STATUS_PROGRESS, getStatusProgress());
        return content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }
}

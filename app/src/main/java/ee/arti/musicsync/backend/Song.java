package ee.arti.musicsync.backend;

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
        this.setId(cursor.getString(cursor.getColumnIndex(columns[0])));
        this.setName(cursor.getString(cursor.getColumnIndex(columns[1])));
        this.setType(cursor.getString(cursor.getColumnIndex(columns[2])));
        this.setUrl(cursor.getString(cursor.getColumnIndex(columns[3])));
        this.setPlaylistId(cursor.getString(cursor.getColumnIndex(columns[4])));
        this.setStatusType(cursor.getString(cursor.getColumnIndex(columns[5])));
        this.setStatusProgress(cursor.getString(cursor.getColumnIndex(columns[6])));
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

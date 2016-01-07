package ee.arti.musicsync.backend;

import android.content.ContentValues;
import android.database.Cursor;

public class Playlist {

    private String id;
    private String name;
    private String type;
    private String statusType;
    private String statusProgress;

    public static final String[] columns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_TYPE,
            DatabaseHelper.COLUMN_STATUS_TYPE,
            DatabaseHelper.COLUMN_STATUS_PROGRESS
    };

    public Playlist() {

    }

    public Playlist(String id, String name, String type, String statusType, String statusProgress) {
        this.setId(id);
        this.setName(name);
        this.setType(type);
        this.setStatusType(statusType);
        this.setStatusProgress(statusProgress);
    }

    /**
     * Create a Playlist object from cursor
     *
     * @param cursor
     */
    public Playlist(Cursor cursor) {
        this.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
        this.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
        this.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE)));
        this.setStatusType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS_TYPE)));
        this.setStatusProgress(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS_PROGRESS)));
    }

    public ContentValues getValues() {
        ContentValues content = new ContentValues();
        content.put(DatabaseHelper.COLUMN_ID, getId());
        content.put(DatabaseHelper.COLUMN_NAME, getName());
        content.put(DatabaseHelper.COLUMN_TYPE, getType());
        content.put(DatabaseHelper.COLUMN_STATUS_TYPE, getStatusType());
        content.put(DatabaseHelper.COLUMN_STATUS_PROGRESS, getStatusProgress());
        return content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public String getStatusProgress() {
        return statusProgress;
    }

    public void setStatusProgress(String statusProgress) {
        this.statusProgress = statusProgress;
    }
}

package ee.arti.musicsync.backend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_PLAYLISTS = "playlists";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PLAYLIST_ID = "playlist";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_STATUS_TYPE = "status_type";
    public static final String COLUMN_STATUS_PROGRESS = "status_progress";

    public static final String DATABASE_NAME = "MusicSync.db";
    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_CREATE =
            "create table "+TABLE_PLAYLISTS
                +" ("
                    +COLUMN_ID+" text primary key, "
                    +COLUMN_NAME+" text not null, "
                    +COLUMN_TYPE+" text not null, "
                    +COLUMN_STATUS_TYPE+" text not null, "
                    +COLUMN_STATUS_PROGRESS+ " text"
                +"); "+
            "create table "+TABLE_SONGS
                +" ("
                    +COLUMN_ID+" text primary key, "
                    +COLUMN_NAME+" text not null, "
                    +COLUMN_TYPE+" text not null, "
                    +COLUMN_URL+" text not null, "
                    +COLUMN_PLAYLIST_ID+" text not null, "
                    +COLUMN_STATUS_TYPE+" text not null, "
                    +COLUMN_STATUS_PROGRESS+ " text, "
                    +"FOREIGN KEY("+COLUMN_PLAYLIST_ID+") REFERENCES "+TABLE_PLAYLISTS+"("+COLUMN_ID+")"
                +"); ";

    private static DatabaseHelper instance;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseHelper(context);

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version "+oldVersion+" to "+newVersion+", DROP TABLES");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
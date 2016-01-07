package ee.arti.musicsync.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String TAG = "Database";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public Database(Context context) {
        dbHelper = DatabaseHelper.getHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        // do nothing, android will clean up the connection on app close
    }

    public ArrayList<Playlist> getAllPlaylists () {
        ArrayList<Playlist> playlists = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLISTS, Playlist.columns,
                              null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Playlist playlist = new Playlist(cursor);
            playlists.add(playlist);
            cursor.moveToNext();
        }
        cursor.close();
        return playlists;
     }

    public ArrayList<Song> getAllSongsInPlaylist (Playlist playlist) {
        return getAllSongsInPlaylist(playlist.getId());
    }

    public ArrayList<Song> getAllSongsInPlaylist (String id) {
        ArrayList<Song> songs = new ArrayList<>();
        String[] args = {id};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, Song.columns,
                DatabaseHelper.COLUMN_PLAYLIST_ID+" = ?", args, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Song song = new Song(cursor);
            songs.add(song);
            cursor.moveToNext();
        }
        cursor.close();

        return songs;
    }

    public Playlist getPlaylist(String id) {
        String[] args = {id};
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLISTS, Playlist.columns,
                DatabaseHelper.COLUMN_ID+" = ?", args, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            return new Playlist(cursor);
        } else {
            return null;
        }
    }

    public Song getSong(String id) {
        String[] args = {id};
        Cursor cursor = db.query(DatabaseHelper.TABLE_SONGS, Song.columns,
                DatabaseHelper.COLUMN_ID+" = ?", args, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            return new Song(cursor);
        } else {
            return null;
        }
    }

    public void addPlaylist(Playlist playlist) {
        long rowID = db.insert(DatabaseHelper.TABLE_PLAYLISTS, null, playlist.getValues());
        Log.d(TAG, "Inserted playlist " + playlist.getName() + " with rowID " + rowID);
    }

    public void addSong(Song song) {
        long rowID = db.insert(DatabaseHelper.TABLE_SONGS, null, song.getValues());
        Log.d(TAG, "Inserted song "+song.getName()+" with rowID "+rowID);
    }

    public void deletePlaylist(Playlist playlist) {
        deletePlaylist(playlist.getId());
    }

    public void deletePlaylist(String id) {
        db.delete(DatabaseHelper.TABLE_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID+" = "+id, null);
        db.delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteSong(Song song) {
        deleteSong(song.getId());
    }

    public void deleteSong(String id) {
        db.delete(DatabaseHelper.TABLE_SONGS, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public Playlist updatePlaylistStatusProgress(Playlist playlist) {
        return updatePlaylistStatusProgress(playlist.getId());
    }

    public Playlist updatePlaylistStatusProgress(String id) {
        ContentValues values = new ContentValues();
        ArrayList<Song> songs = getAllSongsInPlaylist(id);

        values.put(DatabaseHelper.COLUMN_STATUS_PROGRESS, "SongCount: "+songs.size());

        db.update(DatabaseHelper.TABLE_PLAYLISTS,values, DatabaseHelper.COLUMN_ID+" = ?", new String[] {id});
        return getPlaylist(id);
    }
}

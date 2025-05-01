package com.example.myyoutubeaudioapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "playlist.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    private static final String TABLE_SONGS = "songs";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_FILE_PATH = "file_path";

    // Create table SQL query
    private static final String CREATE_TABLE_SONGS = 
        "CREATE TABLE " + TABLE_SONGS + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_TITLE + " TEXT,"
        + COLUMN_FILE_PATH + " TEXT"
        + ")";

    public PlaylistDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_SONGS);
        } catch (Exception e) {
            Log.e("PlaylistDatabaseHelper", "Error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For this version, we'll simply drop and recreate
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
            onCreate(db);
        } catch (Exception e) {
            Log.e("PlaylistDatabaseHelper", "Error upgrading database", e);
        }
    }

    // Add a new song to the database
    public long addSong(Song song) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, song.getTitle());
            values.put(COLUMN_FILE_PATH, song.getFilePath());

            long id = db.insert(TABLE_SONGS, null, values);
            return id;
        } catch (Exception e) {
            Log.e("PlaylistDatabaseHelper", "Error adding song", e);
            return -1;
        }
    }

    // Get all songs from the database
    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SONGS;

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Song song = new Song(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH))
                    );
                    songs.add(song);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("PlaylistDatabaseHelper", "Error getting songs", e);
        }

        return songs;
    }

    // Delete a song from the database
    public boolean deleteSong(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            return db.delete(TABLE_SONGS, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}) > 0;
        } catch (Exception e) {
            Log.e("PlaylistDatabaseHelper", "Error deleting song", e);
            return false;
        }
    }

    // Get a single song by ID
    public Song getSong(int id) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_SONGS, null, 
                COLUMN_ID + "=?", new String[]{String.valueOf(id)}, 
                null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                Song song = new Song(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH))
                );
                cursor.close();
                return song;
            }
        } catch (Exception e) {
            Log.e("PlaylistDatabaseHelper", "Error getting song by id", e);
        }
        return null;
    }
}

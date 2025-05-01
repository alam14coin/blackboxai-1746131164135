package com.example.myyoutubeaudioapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SongAdapter.SongActionListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private TextInputEditText urlEditText;
    private MaterialButton downloadButton;
    private RecyclerView playlistRecyclerView;
    private SongAdapter songAdapter;
    private PlaylistDatabaseHelper dbHelper;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        urlEditText = findViewById(R.id.urlEditText);
        downloadButton = findViewById(R.id.downloadButton);
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);

        // Initialize database helper
        dbHelper = new PlaylistDatabaseHelper(this);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up MediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            if (currentlyPlayingPosition != -1) {
                songAdapter.updateSongPlayingState(currentlyPlayingPosition, false);
                currentlyPlayingPosition = -1;
            }
        });

        // Set up download button
        downloadButton.setOnClickListener(v -> {
            String url = urlEditText.getText().toString().trim();
            if (!url.isEmpty()) {
                checkPermissionsAndStartDownload(url);
            } else {
                Snackbar.make(v, "Please enter a YouTube URL", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Check for permissions on startup
        checkAndRequestPermissions();
    }

    private void setupRecyclerView() {
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Song> songs = dbHelper.getAllSongs();
        songAdapter = new SongAdapter(songs, this);
        playlistRecyclerView.setAdapter(songAdapter);
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
        }
    }

    private void checkPermissionsAndStartDownload(String url) {
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
            startDownload(url);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                "Storage permission is required to download audio",
                Snackbar.LENGTH_LONG).show();
            checkAndRequestPermissions();
        }
    }

    private void startDownload(String url) {
        Data inputData = new Data.Builder()
            .putString("url", url)
            .build();

        OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class)
            .setInputData(inputData)
            .build();

        WorkManager.getInstance(this).enqueue(downloadWorkRequest);

        // Show progress
        Snackbar.make(findViewById(android.R.id.content),
            "Download started...", Snackbar.LENGTH_LONG).show();

        // Observe work status
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(downloadWorkRequest.getId())
            .observe(this, workInfo -> {
                if (workInfo != null) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        // Clear the URL input
                        urlEditText.setText("");
                        
                        // Refresh the playlist
                        songAdapter.updateSongs(dbHelper.getAllSongs());
                        
                        Snackbar.make(findViewById(android.R.id.content),
                            "Download completed successfully", 
                            Snackbar.LENGTH_SHORT).show();
                    } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                        String error = workInfo.getOutputData()
                            .getString("error");
                        Snackbar.make(findViewById(android.R.id.content),
                            "Download failed: " + error,
                            Snackbar.LENGTH_LONG).show();
                    }
                }
            });
    }

    @Override
    public void onPlayClicked(Song song, int position) {
        try {
            if (currentlyPlayingPosition != -1) {
                // Stop currently playing song
                songAdapter.updateSongPlayingState(currentlyPlayingPosition, false);
            }

            if (currentlyPlayingPosition != position) {
                // Start playing new song
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                currentlyPlayingPosition = position;
                songAdapter.updateSongPlayingState(position, true);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error playing audio", e);
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPauseClicked(Song song, int position) {
        if (mediaPlayer.isPlaying() && currentlyPlayingPosition == position) {
            mediaPlayer.pause();
            songAdapter.updateSongPlayingState(position, false);
            currentlyPlayingPosition = -1;
        }
    }

    @Override
    public void onDeleteClicked(Song song, int position) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Song")
            .setMessage("Are you sure you want to delete this song?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Stop playback if this song is playing
                if (currentlyPlayingPosition == position) {
                    mediaPlayer.stop();
                    currentlyPlayingPosition = -1;
                }

                // Delete the file
                File file = new File(song.getFilePath());
                if (file.exists()) {
                    file.delete();
                }

                // Delete from database
                dbHelper.deleteSong(song.getId());

                // Update RecyclerView
                songAdapter.updateSongs(dbHelper.getAllSongs());

                Snackbar.make(findViewById(android.R.id.content),
                    "Song deleted successfully",
                    Snackbar.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, 
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(android.R.id.content),
                    "Permission granted! You can now download audio.",
                    Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                    "Permission denied. Cannot download audio without storage access.",
                    Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

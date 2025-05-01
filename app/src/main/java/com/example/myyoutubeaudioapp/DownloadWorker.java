package com.example.myyoutubeaudioapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class DownloadWorker extends Worker {
    private static final String TAG = "DownloadWorker";
    private static final String YOUTUBE_URL_PATTERN = 
        "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$";
    private final Context context;
    private final PlaylistDatabaseHelper dbHelper;

    public DownloadWorker(
        @NonNull Context context,
        @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.dbHelper = new PlaylistDatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        String youtubeUrl = getInputData().getString("url");
        
        if (youtubeUrl == null || !isValidYoutubeUrl(youtubeUrl)) {
            return Result.failure(new Data.Builder()
                .putString("error", "Invalid YouTube URL")
                .build());
        }

        try {
            // Create MyMusic directory if it doesn't exist
            File musicDir = new File(context.getExternalFilesDir(null), "MyMusic");
            if (!musicDir.exists()) {
                if (!musicDir.mkdirs()) {
                    return Result.failure(new Data.Builder()
                        .putString("error", "Failed to create MyMusic directory")
                        .build());
                }
            }

            // Generate a unique filename based on timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
            String fileName = "audio_" + timestamp + ".mp3";
            File outputFile = new File(musicDir, fileName);

            // For demonstration purposes, we'll simulate downloading and converting
            // In a real app, you would use a proper YouTube download library
            simulateDownloadAndConversion(outputFile);

            // Add the song to the database
            Song song = new Song(fileName, outputFile.getAbsolutePath());
            long songId = dbHelper.addSong(song);

            if (songId == -1) {
                return Result.failure(new Data.Builder()
                    .putString("error", "Failed to save song to database")
                    .build());
            }

            // Return success with the song details
            Data outputData = new Data.Builder()
                .putString("title", fileName)
                .putString("filePath", outputFile.getAbsolutePath())
                .putLong("songId", songId)
                .build();

            return Result.success(outputData);

        } catch (Exception e) {
            Log.e(TAG, "Error during download", e);
            return Result.failure(new Data.Builder()
                .putString("error", "Download failed: " + e.getMessage())
                .build());
        }
    }

    private boolean isValidYoutubeUrl(String url) {
        return Pattern.compile(YOUTUBE_URL_PATTERN)
            .matcher(url)
            .matches();
    }

    // This is a simulation method. In a real app, you would implement actual
    // YouTube download and audio conversion logic here
    private void simulateDownloadAndConversion(File outputFile) throws IOException {
        // Simulate some work being done
        try {
            Thread.sleep(2000); // Simulate 2 seconds of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }

        // Create an empty file to simulate the download
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // Write some dummy data
            fos.write("Simulated MP3 file content".getBytes());
        }
    }

    // Note: In a real implementation, you would:
    // 1. Use a proper YouTube download library
    // 2. Handle various video qualities and formats
    // 3. Implement proper audio extraction
    // 4. Show download progress
    // 5. Handle various edge cases and errors
}

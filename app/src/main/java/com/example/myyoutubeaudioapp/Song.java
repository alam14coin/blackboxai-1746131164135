package com.example.myyoutubeaudioapp;

public class Song {
    private int id;
    private String title;
    private String filePath;
    private boolean isPlaying;

    public Song(int id, String title, String filePath) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.isPlaying = false;
    }

    // Constructor without id for new songs
    public Song(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
        this.isPlaying = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}

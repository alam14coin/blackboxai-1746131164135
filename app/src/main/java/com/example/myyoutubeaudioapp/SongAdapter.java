package com.example.myyoutubeaudioapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    
    private List<Song> songs;
    private SongActionListener listener;

    public interface SongActionListener {
        void onPlayClicked(Song song, int position);
        void onPauseClicked(Song song, int position);
        void onDeleteClicked(Song song, int position);
    }

    public SongAdapter(List<Song> songs, SongActionListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song, position);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // Method to update the song list
    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    // Method to update a single song's playing state
    public void updateSongPlayingState(int position, boolean isPlaying) {
        if (position >= 0 && position < songs.size()) {
            songs.get(position).setPlaying(isPlaying);
            notifyItemChanged(position);
        }
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private ImageButton playButton;
        private ImageButton pauseButton;
        private ImageButton deleteButton;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.songTitleTextView);
            playButton = itemView.findViewById(R.id.playButton);
            pauseButton = itemView.findViewById(R.id.pauseButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(final Song song, final int position) {
            titleTextView.setText(song.getTitle());

            // Update play/pause button visibility based on playing state
            if (song.isPlaying()) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            } else {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }

            // Set click listeners
            playButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayClicked(song, position);
                }
            });

            pauseButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPauseClicked(song, position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClicked(song, position);
                }
            });
        }
    }
}

package com.example.musicapppromax.Fragment;

import static android.content.Context.MODE_PRIVATE;
import static com.example.musicapppromax.Activity.MainActivity.ARTIST_TO_FRAG;
import static com.example.musicapppromax.Activity.MainActivity.PATH_TO_FRAG;
import static com.example.musicapppromax.Activity.MainActivity.SHOW_MINI_PLAYER;
import static com.example.musicapppromax.Activity.MainActivity.SONG_NAME_TO_FRAG;
import static com.example.musicapppromax.Activity.MainActivity.musicFiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.musicapppromax.R;
import com.example.musicapppromax.Service.PlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageButton btnNext;
    ImageView albumArt;
    TextView txtSingerName, txtSongName;
    FloatingActionButton btnPlayPause;
    View view;
    PlayerService playerService;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlayingFragmentBottom() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_now_playing_bottom,
                container, false);
        txtSingerName = view.findViewById(R.id.txtSingerName);
        txtSongName = view.findViewById(R.id.txtSongName);
        btnNext = view.findViewById(R.id.next_bottom);
        albumArt = view.findViewById(R.id.bottom_album_art);
        btnPlayPause = view.findViewById(R.id.play_pause_miniPlayer);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Next", Toast.LENGTH_SHORT).show();
                if (playerService != null) {
                    playerService.btnNextClicked();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity()
                                .getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
                                .edit();
                        editor.putString(MUSIC_FILE, playerService.musicFiles.
                                get(playerService.position).getPath());
                        editor.putString(ARTIST_NAME, playerService.musicFiles.
                                get(playerService.position).getArtist());
                        editor.putString(SONG_NAME, playerService.musicFiles.
                                get(playerService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity()
                                .getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artist = preferences.getString(ARTIST_NAME, null);
                        String song_name = preferences.getString(SONG_NAME, null);
                        if (path != null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artist;
                            SONG_NAME_TO_FRAG = song_name;
                        } else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if (SHOW_MINI_PLAYER) {
                            if (PATH_TO_FRAG != null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if (art != null) {
                                    Glide.with(getContext()).load(art)
                                            .into(albumArt);
                                } else {
                                    Glide.with(getContext()).load(R.drawable.background_new_year_horenito)
                                            .into(albumArt);
                                }
                                //txtSongName.setText(SONG_NAME_TO_FRAG);
                                //txtSingerName.setText(ARTIST_TO_FRAG);
                            }
                        }
                    }
                }
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "PlayPause", Toast.LENGTH_SHORT).show();
                if (playerService != null) {
                    playerService.btnPlayPauseClicked();
                    if (playerService.isPlaying()) {
                        btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
                    } else {
                        btnPlayPause.setBackgroundResource(R.drawable.ic_play);
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER ) {
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null) {
                    Glide.with(getContext()).load(art)
                            .into(albumArt);
                } else {
                    Glide.with(getContext()).load(R.drawable.background_new_year_horenito)
                            .into(albumArt);
                }
                //txtSongName.setText(SONG_NAME_TO_FRAG);
                //txtSingerName.setText(ARTIST_TO_FRAG);
                Intent intent = new Intent(getContext(), PlayerService.class);
                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null && playerService != null) {
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        PlayerService.MyBinder binder = (PlayerService.MyBinder) service;
        playerService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        playerService = null;
    }
}

package com.example.musicapppromax.Activity;


import static com.example.musicapppromax.Activity.MainActivity.musicFiles;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapppromax.Adapter.ArtistDetailsAdapter;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.R;

import java.util.ArrayList;

public class ArtistDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView artistPhoto;
    String txtArtistName;
    ArrayList<MusicFiles> artistSongs = new ArrayList<>();
    ArtistDetailsAdapter artistDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);
        recyclerView = findViewById(R.id.recyclerView);
        artistPhoto = findViewById(R.id.artistPhoto);
        txtArtistName = getIntent().getStringExtra("artistName");
        int j = 0;
        for (int i = 0; i < musicFiles.size(); i++) {
            if (txtArtistName.equals(musicFiles.get(i).getArtist())) {
                artistSongs.add(j, musicFiles.get(i));
                j++;
            }
        }
        byte[] image = getAlbumArt(artistSongs.get(0).getPath());
        if (image != null) {
            Glide.with(this)
                    .load(image)
                    .into(artistPhoto);
        } else {
            Glide.with(this)
                    .load(R.drawable.background_new_year_horenito)
                    .into(artistPhoto);
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
    protected void onResume() {
        super.onResume();
        if (!(artistSongs.size() < 1)) {
            artistDetailsAdapter = new ArtistDetailsAdapter(artistSongs, this);
            recyclerView.setAdapter(artistDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }
}
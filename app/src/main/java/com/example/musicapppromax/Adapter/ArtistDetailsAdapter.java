package com.example.musicapppromax.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapppromax.Activity.PlayerActivity;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.R;

import java.util.ArrayList;

public class ArtistDetailsAdapter extends RecyclerView.Adapter<ArtistDetailsAdapter.MyHolder> {

    public static ArrayList<MusicFiles> artistFlies;
    private Context mContext;
    View view;

    public ArtistDetailsAdapter(ArrayList<MusicFiles> artistFlies, Context mContext) {
        this.artistFlies = artistFlies;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext)
                .inflate(R.layout.music_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistDetailsAdapter.MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtSongName.setText(artistFlies.get(position).getTitle());
        holder.txtArtistName.setText(artistFlies.get(position).getArtist());
        byte[] image = getAlbumArt(artistFlies.get(position).getPath());
        if (image != null) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.imgArtist);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.background_new_year_horenito)
                    .into(holder.imgArtist);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "artistDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artistFlies.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView imgArtist;
        TextView txtSongName, txtArtistName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imgArtist = itemView.findViewById(R.id.img_music);
            txtSongName = itemView.findViewById(R.id.txtSongName);
            txtArtistName = itemView.findViewById(R.id.txtSingerName);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

}

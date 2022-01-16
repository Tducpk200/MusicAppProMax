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
import com.example.musicapppromax.Activity.ArtistDetails;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.R;

import java.util.ArrayList;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.Holder> {
    private ArrayList<MusicFiles> artistFiles;
    private Context mContext;
    View view;

    public ArtistAdapter( Context mContext, ArrayList<MusicFiles> artistFiles) {
        this.artistFiles = artistFiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).
                inflate(R.layout.artist_item, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAdapter.Holder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtArtistName.setText(artistFiles.get(position).getArtist());
        byte[] image = getAlbumArt(artistFiles.get(position).getPath());
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
                Intent intent = new Intent(mContext, ArtistDetails.class);
                intent.putExtra("artistName", artistFiles.get(position).getArtist());
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return artistFiles.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView imgArtist;
        TextView txtArtistName;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imgArtist = itemView.findViewById(R.id.imgArtist);
            txtArtistName = itemView.findViewById(R.id.txtArtistName);
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

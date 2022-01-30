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
import com.example.musicapppromax.Activity.AlbumDetails;
import com.example.musicapppromax.Activity.PlayerActivity;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.R;

import java.util.ArrayList;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder> {
    public  static ArrayList<MusicFiles> albumFiles;
    private final Context mContext;
    View view;

    public AlbumDetailsAdapter(ArrayList<MusicFiles> albumFiles, Context mContext) {
        AlbumDetailsAdapter.albumFiles = albumFiles;
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
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtAlbumName.setText(albumFiles.get(position).getTitle());
        holder.txtSingerName.setText(albumFiles.get(position).getArtist());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.imgAlbum);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.background_new_year_horenito)
                    .into(holder.imgAlbum);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView imgAlbum;
        TextView txtAlbumName, txtSingerName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum = itemView.findViewById(R.id.img_music);
            txtAlbumName = itemView.findViewById(R.id.txtSongName);
            txtSingerName = itemView.findViewById(R.id.txtSingerName);
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

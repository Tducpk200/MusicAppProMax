package com.example.musicapppromax.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    public long playlistID;
    @ColumnInfo( name = "playlist_name")
    public String name;
}

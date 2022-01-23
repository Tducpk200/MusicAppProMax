package com.example.musicapppromax.DB;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaylistDAO {
    @Insert(onConflict = REPLACE)
    void insertPlaylist(Playlist playlist);

    @Insert(onConflict =  IGNORE)
    void insertOrReplacePlaylist(Playlist... playlists);

    @Update(onConflict = REPLACE)
    void updatePlaylist(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);

    @Query("DELETE FROM Playlist")
    void deleteAll();

    @Query("SELECT * FROM Playlist")
    public List<Playlist> findAllPlaylist();
}

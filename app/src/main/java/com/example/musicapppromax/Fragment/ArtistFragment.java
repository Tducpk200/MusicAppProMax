package com.example.musicapppromax.Fragment;

import static com.example.musicapppromax.Activity.MainActivity.artists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapppromax.Adapter.ArtistAdapter;
import com.example.musicapppromax.R;


public class ArtistFragment extends Fragment {

    RecyclerView recyclerView;
    ArtistAdapter artistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if (!(artists.size() < 1)) {
            artistAdapter = new ArtistAdapter(getContext(), artists);
            recyclerView.setAdapter(artistAdapter);
            recyclerView.setLayoutManager(
                    new GridLayoutManager(getContext(), 2));
        }
        return view;
    }
}
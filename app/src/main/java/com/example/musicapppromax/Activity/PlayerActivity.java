package com.example.musicapppromax.Activity;

import static com.example.musicapppromax.Activity.MainActivity.musicFiles;
import static com.example.musicapppromax.Activity.MainActivity.repeatBoolean;
import static com.example.musicapppromax.Activity.MainActivity.shuffleBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musicapppromax.Adapter.MusicAdapter;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    TextView txtSongName, txtSingerName, duration_Total, duration_Player;
    ImageView cover_art, btnNext, btnBack, btnShuffle, btnRepeat,btnPrev;
    FloatingActionButton btnPlayPause;
    SeekBar seekBar;
    int position = -1;
    static  ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();
        mediaPlayer.setOnCompletionListener(this);
        txtSongName.setText(listSongs.get(position).getTitle());
        txtSingerName.setText(listSongs.get(position).getArtist());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(mediaPlayer != null & fromUser){
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_Total.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean){
                    shuffleBoolean = false;
                    btnShuffle.setBackgroundResource(R.drawable.ic_shuffle);
                }
                else {
                    shuffleBoolean = true;
                    btnShuffle.setBackgroundResource(R.drawable.ic_shuffle_blue);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean){
                    repeatBoolean = false;
                    btnRepeat.setBackgroundResource(R.drawable.ic_repeat);
                }
                else {
                    repeatBoolean = true;
                    btnRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
                }
            }
        });
    }

    private  String formattedTime(int mCurrentPosition){
        String totalOut = "";
        String totalNew ="";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String mimites = String.valueOf(mCurrentPosition  / 60);
        totalOut = mimites + ":" + seconds;
        totalNew = mimites + ";" + "0" + seconds;
        if(seconds.length() == 1){
            return  totalNew;
        }
        else {
            return totalOut;

        }
    }

    private void initViews(){
        txtSongName = findViewById(R.id.txtSongName);
        txtSingerName = findViewById(R.id.txtSingerName);
        duration_Total = findViewById(R.id.txtDurationTotal);
        duration_Player = findViewById(R.id.txtDurationPlayer);
        cover_art = findViewById(R.id.cover_art);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnPlayPause = findViewById(R.id.btnPlay_Pause);
        btnPrev = findViewById(R.id.btnPrev);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        seekBar = findViewById(R.id.SeekBar);
    }

    private void getIntentMethod(){

        position = getIntent().getIntExtra("position", -1);
        listSongs = musicFiles;
        if(listSongs != null){
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        else{
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
    }

    private  void metaData (Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_Total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art != null){
            bitmap = BitmapFactory.decodeByteArray(art, 0 , art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if( swatch != null){
                        ImageView grendient = findViewById(R.id.cover_art);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        grendient.setBackgroundResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        grendient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        txtSongName.setTextColor(swatch.getTitleTextColor());
                        txtSingerName.setTextColor(swatch.getBodyTextColor());
                    }
                    else{
                        ImageView grendient = findViewById(R.id.cover_art);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        grendient.setBackgroundResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        grendient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        txtSongName.setTextColor(Color.WHITE);
                        txtSingerName.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.background_new_year_horenito)
                    .into(cover_art);
            ImageView grendient = findViewById(R.id.imageViewGredinet);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            grendient.setBackgroundResource(R.drawable.gredient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            txtSongName.setTextColor(Color.WHITE);
            txtSingerName.setTextColor(Color.DKGRAY);

        }

    }

    @Override
    protected void onResume() {
        playThread();
        nextThread();
        prevThread();
        super.onResume();
    }

    private void playThread() {
        playThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                btnPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPlayClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void btnPlayClicked() {

        if(mediaPlayer.isPlaying()){
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000) ;

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrnetPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }

        else {
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrnetPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });

        }

    }

    private void nextThread(){

        nextThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnNextClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void btnNextClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position = (position + 1) % listSongs.size();
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrnetPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();

        }

        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position = (position + 1) % listSongs.size();
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrnetPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);

        }
    }

    private int getRandom(int i) {
        Random random = new Random();

        return random.nextInt(i + 1);
    }

    private void prevThread(){
        prevThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                btnPrev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPrevClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void btnPrevClicked() {

        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1 ));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrnetPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();

        }

        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1 ));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrnetPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);

        }

    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btnNextClicked();
        if(mediaPlayer != null){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }
}
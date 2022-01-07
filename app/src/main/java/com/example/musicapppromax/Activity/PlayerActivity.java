package com.example.musicapppromax.Activity;

import static com.example.musicapppromax.Activity.MainActivity.musicFiles;
import static com.example.musicapppromax.Activity.MainActivity.repeatBoolean;
import static com.example.musicapppromax.Activity.MainActivity.shuffleBoolean;
import static com.example.musicapppromax.Adapter.AlbumDetailsAdapter.albumFiles;
import static com.example.musicapppromax.Adapter.MusicAdapter.mFiles;
import static com.example.musicapppromax.Application.ApplicationClass.ACTION_NEXT;
import static com.example.musicapppromax.Application.ApplicationClass.ACTION_PLAY;
import static com.example.musicapppromax.Application.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicapppromax.Application.ApplicationClass.CHANNEL_ID_2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.animation.Animator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.musicapppromax.ActionPlaying;
import com.example.musicapppromax.Adapter.MusicAdapter;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.NotificationReceiver;
import com.example.musicapppromax.R;
import com.example.musicapppromax.Service.PlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection {

    TextView txtSongName, txtSingerName, duration_Total, duration_Player;
    ImageView cover_art, btnNext, btnBack, btnShuffle, btnRepeat, btnPrev;
    FloatingActionButton btnPlayPause;
    SeekBar seekBar;
    int position = -1;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    public static Uri uri;
    //public static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    PlayerService playerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullSreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();
        initViews();
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (playerService != null & fromUser) {
                    playerService.seekTo(progress * 1000);
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
                if (playerService != null) {
                    int mCurrentPosition = playerService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_Total.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    btnShuffle.setBackgroundResource(R.drawable.ic_shuffle);
                } else {
                    shuffleBoolean = true;
                    btnShuffle.setBackgroundResource(R.drawable.ic_shuffle_blue);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    btnRepeat.setBackgroundResource(R.drawable.ic_repeat);
                } else {
                    repeatBoolean = true;
                    btnRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
                }
            }
        });
    }

    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String mimites = String.valueOf(mCurrentPosition / 60);
        totalOut = mimites + ":" + seconds;
        totalNew = mimites + ";" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;

        }
    }

    private void initViews() {
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

    private void getIntentMethod() {

        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetails")) {
            listSongs = albumFiles;
        } else {
            listSongs = mFiles;
        }
        if (listSongs != null) {
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_Total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
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
                    } else {
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
        } else {
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
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThread();
        nextThread();
        prevThread();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(this);
    }

    private void playThread() {
        playThread = new Thread() {
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

    public void btnPlayClicked() {

        if (playerService.isPlaying()) {
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);
            playerService.showNotification(R.drawable.ic_play);
            playerService.pause();
            seekBar.setMax(playerService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerService != null) {
                        int mCurrnetPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            playerService.showNotification(R.drawable.ic_pause);
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            playerService.start();
            seekBar.setMax(playerService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerService != null) {
                        int mCurrnetPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });

        }

    }

    private void nextThread() {

        nextThread = new Thread() {
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

    public void btnNextClicked() {
        if (playerService.isPlaying()) {
            playerService.stop();
            playerService.release();
            if (shuffleBoolean && repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = (position + 1) % listSongs.size();
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            playerService.createMediaPlayer(position);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(playerService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerService != null) {
                        int mCurrnetPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            playerService.OnCompleted();
            playerService.showNotification(R.drawable.ic_pause);
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            playerService.start();

        } else {
            playerService.stop();
            playerService.release();
            if (shuffleBoolean && repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = (position + 1) % listSongs.size();
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            playerService.createMediaPlayer(position);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(playerService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerService != null) {
                        int mCurrnetPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            playerService.OnCompleted();
            playerService.showNotification(R.drawable.ic_play);
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);

        }
    }

    private int getRandom(int i) {
        Random random = new Random();

        return random.nextInt(i + 1);
    }

    private void prevThread() {
        prevThread = new Thread() {
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

    public void btnPrevClicked() {

        if (playerService.isPlaying()) {
            playerService.stop();
            playerService.release();
            if (shuffleBoolean && repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            playerService.createMediaPlayer(position);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(playerService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerService != null) {
                        int mCurrnetPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            playerService.OnCompleted();
            playerService.showNotification(R.drawable.ic_pause);
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            playerService.start();

        } else {
            playerService.stop();
            playerService.release();
            if (shuffleBoolean && repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            playerService.createMediaPlayer(position);
            metaData(uri);
            txtSongName.setText(listSongs.get(position).getTitle());
            txtSingerName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(playerService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerService != null) {
                        int mCurrnetPosition = playerService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrnetPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            playerService.OnCompleted();
            playerService.showNotification(R.drawable.ic_play);
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);

        }

    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap) {
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

    private void setFullSreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        PlayerService.MyBinder myBinder = (PlayerService.MyBinder) service;
        playerService = myBinder.getService();
        playerService.setCallBack(this);
        Toast.makeText(this, "Connected" + playerService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(playerService.getDuration() / 1000);
        metaData(uri);
        playerService.OnCompleted();
        txtSongName.setText(listSongs.get(position).getTitle());
        txtSingerName.setText(listSongs.get(position).getArtist());
        playerService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        playerService = null;

    }


}
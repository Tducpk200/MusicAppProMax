package com.example.musicapppromax.Service;

import static com.example.musicapppromax.Activity.PlayerActivity.listSongs;
import static com.example.musicapppromax.Application.ApplicationClass.ACTION_NEXT;
import static com.example.musicapppromax.Application.ApplicationClass.ACTION_PLAY;
import static com.example.musicapppromax.Application.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicapppromax.Application.ApplicationClass.CHANNEL_ID_2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicapppromax.ActionPlaying;
import com.example.musicapppromax.Activity.PlayerActivity;
import com.example.musicapppromax.Files.MusicFiles;
import com.example.musicapppromax.NotificationReceiver;
import com.example.musicapppromax.R;

import java.util.ArrayList;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    public static ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    public static int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    private int btnPlayPause;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mBinder;
    }

    public class MyBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");

        if (myPosition != -1) {
            playMedia(myPosition);
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    btnPlayPauseClicked();
                    break;
                case "next":
                    btnNextClicked();
                    break;
                case "previous":
                    btnPreviousClicked();
                    break;
            }
        }

        return START_STICKY;
    }

    private void playMedia(int StartPosition) {
        musicFiles = listSongs;
        position = StartPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }


    public void start() {
        mediaPlayer.start();
        Intent intentStart = new Intent("START");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentStart);

    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void stop() {
        mediaPlayer.stop();
        Intent intentStop = new Intent("STOP");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentStop);
    }

    public void release() {
        mediaPlayer.release();
        Intent intentRelease = new Intent("RELEASE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentRelease);
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void pause() {
        mediaPlayer.pause();
        Intent intentPause = new Intent("PAUSE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentPause);
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public void createMediaPlayer(int positionInner) {

        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
                .edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.btnNextClicked();
            if (mediaPlayer != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }

    }

    public void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    public void showNotification(int btnPlayPause) {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent prevPending = PendingIntent.getBroadcast(this, 0,
                prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pausePending = PendingIntent.getBroadcast(this, 0,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent nextPending = PendingIntent.getBroadcast(this, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);

        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.background_new_year_horenito);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(btnPlayPause)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_previous, "Previous", prevPending)
                .addAction(btnPlayPause, "Pause", pausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build();
        startForeground(2, notification);
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public void btnNextClicked() {
        if (actionPlaying != null) {
            actionPlaying.btnNextClicked();
        }
    }

    public void btnPreviousClicked() {
        if (actionPlaying != null) {
            actionPlaying.btnPrevClicked();
        }
    }

    public void btnPlayPauseClicked() {
        if (actionPlaying != null) {
            actionPlaying.btnPlayClicked();
        }
    }
}

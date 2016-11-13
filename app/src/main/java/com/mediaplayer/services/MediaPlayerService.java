package com.mediaplayer.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mediaplayer.R;
import com.mediaplayer.activities.MediaPlayerActivity;
import com.mediaplayer.beans.Track;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.SQLConstants;
import com.mediaplayer.utilities.Utilities;

import java.io.IOException;

public class MediaPlayerService extends IntentService {
    private static String LOG_TAG = "MediaPlayerService";
    private static MediaPlayer mp;
    public static boolean isServiceRunning;
    public static boolean isServiceBound;
    private IBinder mBinder = new MyBinder();

    private static TextView timeElapsed;
    private static SeekBar songProgressBar;
    private Handler mHandler = new Handler();

    public MediaPlayerService() {
        super("MediaPlayerService");
    }

    public static MediaPlayer getMp() {
        return mp;
    }

    public static void setMp(MediaPlayer mp) {
        MediaPlayerService.mp = mp;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*timeElapsed = (TextView) findViewById(R.id.timeElapsed);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);*/

        Log.d(LOG_TAG, "Service created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Starting service...");
        isServiceRunning = true;

        while(isServiceRunning) {}

        Log.d("isServiceRunning: ", String.valueOf(isServiceRunning));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Binding activity to service...");

        Track selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        playSong(selectedTrack);
        Notification notification = createNotification(selectedTrack);
        startForeground(1, notification);
        Log.d(LOG_TAG, "Is foreground?: true");
        isServiceBound = true;

        Log.d("isServiceBound: ", String.valueOf(isServiceBound));
        Log.d(LOG_TAG, "Service bound to activity");
        return mBinder;
    }

    // Play the requested track
    public void playSong(Track selectedTrack) {
        Log.d(LOG_TAG, "START: The playSong() event");
        String filePath = selectedTrack.getTrackLocation();

        if(mp == null) {
            mp = new MediaPlayer();
        }

        try {
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            Log.d(LOG_TAG, "Now Playing: " + selectedTrack.getTrackTitle());
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "END: The playSong() event");
    }

    @TargetApi(23)
    public Notification createNotification(Track selectedTrack) {
        Bitmap bm = null;
        byte data[] = selectedTrack.getAlbumArt();

        if(data != null) {
            bm = BitmapFactory.decodeByteArray(data, SQLConstants.ZERO, data.length);
        }

        Notification.Builder builder = new Notification.Builder(this);
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
        MediaSession mMediaSession = new MediaSession(this, MediaPlayerConstants.TAG_MEDIA_SESSION);

        //Creating Icons for actions
        Icon prevIcon = Icon.createWithResource(this, R.drawable.ic_skip_previous_white_24dp);
        Icon playIcon = Icon.createWithResource(this, R.drawable.ic_play_arrow_white_24dp);
        Icon pauseIcon = Icon.createWithResource(this, R.drawable.ic_pause_white_24dp);
        Icon nextIcon = Icon.createWithResource(this, R.drawable.ic_skip_next_white_24dp);

        //Setting mediastyle attributes
        mediaStyle.setShowActionsInCompactView(SQLConstants.ONE);
        mediaStyle.setMediaSession(mMediaSession.getSessionToken());

        //Setting builder attributes
        builder.setStyle(mediaStyle);
        builder.setContentTitle(selectedTrack.getTrackTitle());
        builder.setContentText(selectedTrack.getArtistName());
        builder.setSubText(selectedTrack.getAlbumName());
        builder.setLargeIcon(bm);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.drawable.ic_library_music_white_18dp);
        builder.setShowWhen(false);

        //Creating intents
        Intent prevIntent = new Intent(this, MediaPlayerActivity.class);
        Intent pauseIntent = new Intent(this, MediaPlayerActivity.class);
        Intent playIntent = new Intent(this, MediaPlayerActivity.class);
        Intent nextIntent = new Intent(this, MediaPlayerActivity.class);
        Intent deleteIntent = new Intent(this, MediaPlayerActivity.class);

        //Setting actions for intents
        prevIntent.setAction(MediaPlayerConstants.PREVIOUS).putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        pauseIntent.setAction(MediaPlayerConstants.PAUSE).putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        playIntent.setAction(MediaPlayerConstants.PLAY).putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        nextIntent.setAction(MediaPlayerConstants.NEXT).putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        deleteIntent.setAction(MediaPlayerConstants.STOP);

        //Creating pending intents
        PendingIntent prevPendingIntent = PendingIntent.getActivity(this, SQLConstants.ZERO, prevIntent, SQLConstants.ZERO);
        PendingIntent pausePendingIntent = PendingIntent.getActivity(this, SQLConstants.ZERO, pauseIntent, SQLConstants.ZERO);
        PendingIntent playPendingIntent = PendingIntent.getActivity(this, SQLConstants.ZERO, playIntent, SQLConstants.ZERO);
        PendingIntent nextPendingIntent = PendingIntent.getActivity(this, SQLConstants.ZERO, nextIntent, SQLConstants.ZERO);
        PendingIntent deletePendingIntent = PendingIntent.getActivity(this, SQLConstants.ZERO, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //Creating notification actions
        Notification.Action prevAction = new Notification.Action.Builder(prevIcon, MediaPlayerConstants.PREVIOUS, prevPendingIntent).build();
        Notification.Action pauseAction = new Notification.Action.Builder(pauseIcon, MediaPlayerConstants.PLAY, pausePendingIntent).build();
        Notification.Action playAction = new Notification.Action.Builder(playIcon, MediaPlayerConstants.PAUSE, playPendingIntent).build();
        Notification.Action nextAction = new Notification.Action.Builder(nextIcon, MediaPlayerConstants.NEXT, nextPendingIntent).build();

        //Adding notification actions to the builder
        builder.addAction(prevAction);

        if(mp.isPlaying()) {
            builder.addAction(pauseAction);
        } else {
            builder.addAction(playAction);
        }

        builder.addAction(nextAction);
        builder.setDeleteIntent(deletePendingIntent);

        //Building notification
        Notification notification = builder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(SQLConstants.ONE, notification);

        Log.d(LOG_TAG, "Notification created");
        return notification;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "Service rebound");
        isServiceBound = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Service unbound");
        isServiceBound = false;
        return true;
    }

    @Override
    public void onTaskRemoved (Intent rootIntent) {
        Log.d(LOG_TAG, "Task removed");
    }

    @Override
    public void onDestroy() {
        if(mp != null) {
            mp.release();
        }

        isServiceRunning = false;
        Log.d(LOG_TAG, "Service destroyed");
    }

    public class MyBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}

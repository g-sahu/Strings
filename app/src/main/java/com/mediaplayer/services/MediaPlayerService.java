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
import android.os.IBinder;
import android.util.Log;

import com.mediaplayer.R;
import com.mediaplayer.activities.MediaPlayerActivity;
import com.mediaplayer.beans.Track;
import com.mediaplayer.utilities.MediaPlayerConstants;

import java.io.IOException;

public class MediaPlayerService extends IntentService {
    private static String LOG_TAG = "MediaPlayerService";
    private static MediaPlayer mp;
    public static boolean isServiceRunning;
    public static boolean isServiceBound;
    private IBinder mBinder = new MyBinder();

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
        Log.d(LOG_TAG, "Service created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Starting service...");
        isServiceRunning = true;
        Log.d("isServiceRunning: ", String.valueOf(isServiceRunning));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "Binding service to activity...");

        Track requestedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        playSong(requestedTrack);
        Notification notification = createNotification(requestedTrack);
        startForeground(1, notification);
        isServiceBound = true;

        Log.d("isServiceBound: ", String.valueOf(isServiceBound));
        Log.v(LOG_TAG, "Service bound to activity");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "Service rebound");
        super.onRebind(intent);
        isServiceBound = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "Service unbound");
        isServiceBound = false;
        return true;
    }

    // Play the requested track
    public void playSong(Track requestedTrack) {
        Log.d(LOG_TAG, "START: The playSong() event");
        String filePath = requestedTrack.getTrackLocation();

        if(mp == null) {
            mp = new MediaPlayer();
        }

        try {
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            Log.d(LOG_TAG, "Now Playing: " + requestedTrack.getTrackTitle());
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            //FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "IOException caught");
            //FirebaseCrash.report(e);
        }

        Log.d(LOG_TAG, "END: The playSong() event");
    }

    @TargetApi(23)
    public Notification createNotification(Track requestedTrack) {
        Bitmap bm = null;
        byte data[] = requestedTrack.getAlbumArt();

        if (data != null) {
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        Notification.Builder builder = new Notification.Builder(this);
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
        MediaSession mMediaSession = new MediaSession(this, "Session");

        //Creating Icons for actions
        Icon prevIcon = Icon.createWithResource(this, R.drawable.ic_skip_previous_white_24dp);
        Icon playIcon = Icon.createWithResource(this, R.drawable.ic_play_arrow_white_24dp);
        Icon pauseIcon = Icon.createWithResource(this, R.drawable.ic_pause_white_24dp);
        Icon nextIcon = Icon.createWithResource(this, R.drawable.ic_skip_next_white_24dp);

        //Setting mediastyle attributes
        mediaStyle.setShowActionsInCompactView(1);
        mediaStyle.setMediaSession(mMediaSession.getSessionToken());

        //Setting builder attributes
        builder.setStyle(mediaStyle);
        builder.setContentTitle(requestedTrack.getTrackTitle());
        builder.setContentText(requestedTrack.getArtistName());
        builder.setSubText(requestedTrack.getAlbumName());
        builder.setLargeIcon(bm);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.drawable.ic_library_music_white_18dp);

        //Creating Pending Intents
        Intent prevIntent = new Intent(this, MediaPlayerActivity.class);
        Intent pauseIntent = new Intent(this, MediaPlayerActivity.class);
        Intent playIntent = new Intent(this, MediaPlayerActivity.class);
        Intent nextIntent = new Intent(this, MediaPlayerActivity.class);

        prevIntent.setAction(MediaPlayerConstants.PREVIOUS);
        pauseIntent.setAction(MediaPlayerConstants.PAUSE);
        playIntent.setAction(MediaPlayerConstants.PLAY);
        nextIntent.setAction(MediaPlayerConstants.NEXT);

        prevIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, requestedTrack);
        pauseIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, requestedTrack);
        playIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, requestedTrack);
        nextIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, requestedTrack);

        PendingIntent prevPendingIntent = PendingIntent.getActivity(this, 0, prevIntent, 0);
        PendingIntent pausePendingIntent = PendingIntent.getActivity(this, 0, pauseIntent, 0);
        PendingIntent playPendingIntent = PendingIntent.getActivity(this, 0, playIntent, 0);
        PendingIntent nextPendingIntent = PendingIntent.getActivity(this, 0, nextIntent, 0);

        //Creating notification actions
        Notification.Action prevAction = new Notification.Action.Builder(prevIcon, MediaPlayerConstants.PREVIOUS, prevPendingIntent).build();
        Notification.Action pauseAction = new Notification.Action.Builder(pauseIcon, MediaPlayerConstants.PLAY, pausePendingIntent).build();
        Notification.Action playAction = new Notification.Action.Builder(playIcon, MediaPlayerConstants.PAUSE, playPendingIntent).build();
        Notification.Action nextAction = new Notification.Action.Builder(nextIcon, MediaPlayerConstants.NEXT, nextPendingIntent).build();

        // Adding notification actions to the builder
        builder.addAction(prevAction);

        if(mp.isPlaying()) {
            builder.addAction(pauseAction);
        } else {
            builder.addAction(playAction);
        }

        builder.addAction(nextAction);

        Notification notification = builder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);

        Log.d(LOG_TAG, "Notification created");
        return notification;
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

package com.mediaplayer.strings.services;

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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.mediaplayer.strings.R;
import com.mediaplayer.strings.activities.MediaPlayerActivity;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;
import com.mediaplayer.strings.utilities.MediaPlayerStateManager;
import com.mediaplayer.strings.utilities.SQLConstants;

import java.io.IOException;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class MediaPlayerService extends IntentService {
    private static final String LOG_TAG = "MediaPlayerService";
    private static MediaPlayer mp;
    public static boolean isServiceRunning;
    public static boolean isServiceBound;
    private MediaPlayerStateManager mManager;
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
        mManager = ((MediaPlayerStateManager) getApplicationContext());
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
        String selectedPlaylist = intent.getStringExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        playSong(selectedTrack);
        Notification notification = createNotification(selectedTrack, selectedPlaylist);
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
            mManager.setPaused(false);
            Log.d(LOG_TAG, "Now Playing: " + selectedTrack.getTrackTitle());
        } catch(IOException | IllegalStateException e) {
            e.printStackTrace();
            //Utilities.reportCrash(e);
        }

        Log.d(LOG_TAG, "END: The playSong() event");
    }

    public Notification createNotification(Track selectedTrack, String selectedPlaylist) {
        Notification notification = null;
        Notification.Action prevAction = null, pauseAction = null, playAction = null, nextAction = null;
        Bitmap bm = null;
        int zero = SQLConstants.ZERO;
        int flag = PendingIntent.FLAG_CANCEL_CURRENT;
        String keySelectedTrack = MediaPlayerConstants.KEY_SELECTED_TRACK;
        String keySelectedPlaylist = MediaPlayerConstants.KEY_SELECTED_PLAYLIST;
        String keyTrackOrigin = MediaPlayerConstants.KEY_TRACK_ORIGIN;
        String origin = MediaPlayerConstants.TAG_NOTIFICATION;

        try {
            byte data[] = selectedTrack.getAlbumArt();

            if(data != null) {
                bm = BitmapFactory.decodeByteArray(data, zero, data.length);
            }

            Notification.Builder builder = new Notification.Builder(this);
            Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
            MediaSession mMediaSession = new MediaSession(this, MediaPlayerConstants.TAG_MEDIA_SESSION);

            //Setting mediastyle attributes
            mediaStyle.setShowActionsInCompactView(0, 1, 2);
            mediaStyle.setMediaSession(mMediaSession.getSessionToken());

            //Setting builder attributes
            builder.setStyle(mediaStyle);
            builder.setContentTitle(selectedTrack.getTrackTitle());
            builder.setContentText(selectedTrack.getArtistName());
            builder.setSubText(selectedTrack.getAlbumName());
            builder.setLargeIcon(bm);
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setShowWhen(false);

            //Creating intents
            Intent prevIntent = new Intent(this, MediaPlayerActivity.class);
            Intent pauseIntent = new Intent(this, MediaPlayerActivity.class);
            Intent playIntent = new Intent(this, MediaPlayerActivity.class);
            Intent nextIntent = new Intent(this, MediaPlayerActivity.class);
            //Intent deleteIntent = new Intent(this, MediaPlayerActivity.class);
            Intent deleteIntent = new Intent(this, MediaPlayerService.class);

            //Setting actions and extras for intents
            prevIntent.setAction(MediaPlayerConstants.PREVIOUS)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            pauseIntent.setAction(MediaPlayerConstants.PAUSE)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            playIntent.setAction(MediaPlayerConstants.PLAY)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            nextIntent.setAction(MediaPlayerConstants.NEXT)
                    .putExtra(keySelectedTrack, selectedTrack)
                    .putExtra(keySelectedPlaylist, selectedPlaylist)
                    .putExtra(keyTrackOrigin, origin);
            deleteIntent.setAction(MediaPlayerConstants.STOP);

            //Creating pending intents
            PendingIntent prevPendingIntent = PendingIntent.getActivity(this, zero, prevIntent, flag);
            PendingIntent pausePendingIntent = PendingIntent.getActivity(this, zero, pauseIntent, flag);
            PendingIntent playPendingIntent = PendingIntent.getActivity(this, zero, playIntent, flag);
            PendingIntent nextPendingIntent = PendingIntent.getActivity(this, zero, nextIntent, flag);
            //PendingIntent deletePendingIntent = PendingIntent.getActivity(this, zero, deleteIntent, flag);
            PendingIntent deletePendingIntent = PendingIntent.getService(this, zero, deleteIntent, flag);

            //Checking OS build version for notification compatibility
            if(android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
                    android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                //Creating notification actions
                prevAction = new Notification.Action.Builder(R.drawable.ic_skip_previous_white_24dp, MediaPlayerConstants.PREVIOUS, prevPendingIntent).build();
                pauseAction = new Notification.Action.Builder(R.drawable.ic_pause_white_24dp, MediaPlayerConstants.PAUSE, pausePendingIntent).build();
                playAction = new Notification.Action.Builder(R.drawable.ic_play_arrow_white_24dp, MediaPlayerConstants.PLAY, playPendingIntent).build();
                nextAction = new Notification.Action.Builder(R.drawable.ic_skip_next_white_24dp, MediaPlayerConstants.NEXT, nextPendingIntent).build();
            } else if(android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                //Creating Icons for actions
                Icon prevIcon = Icon.createWithResource(this, R.drawable.ic_skip_previous_white_24dp);
                Icon pauseIcon = Icon.createWithResource(this, R.drawable.ic_pause_white_24dp);
                Icon playIcon = Icon.createWithResource(this, R.drawable.ic_play_arrow_white_24dp);
                Icon nextIcon = Icon.createWithResource(this, R.drawable.ic_skip_next_white_24dp);

                //Creating notification actions
                prevAction = new Notification.Action.Builder(prevIcon, MediaPlayerConstants.PREVIOUS, prevPendingIntent).build();
                pauseAction = new Notification.Action.Builder(pauseIcon, MediaPlayerConstants.PAUSE, pausePendingIntent).build();
                playAction = new Notification.Action.Builder(playIcon, MediaPlayerConstants.PLAY, playPendingIntent).build();
                nextAction = new Notification.Action.Builder(nextIcon, MediaPlayerConstants.NEXT, nextPendingIntent).build();
            }

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
            notification = builder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(SQLConstants.ONE, notification);
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

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
    public void onDestroy() {
        if(mp != null) {
            mp.release();
        }

        isServiceRunning = false;
        Log.d(LOG_TAG, "MediaPlayerService destroyed");
    }

    public class MyBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}

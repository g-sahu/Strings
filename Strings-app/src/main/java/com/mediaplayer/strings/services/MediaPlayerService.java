package com.mediaplayer.strings.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.mediaplayer.strings.activities.MediaPlayerActivity;
import com.mediaplayer.strings.beans.Track;

import java.io.IOException;

import static android.app.Notification.Action;
import static android.app.Notification.Builder;
import static android.app.Notification.MediaStyle;
import static android.app.Notification.VISIBILITY_PUBLIC;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.app.PendingIntent.getService;
import static android.graphics.BitmapFactory.decodeByteArray;
import static android.graphics.BitmapFactory.decodeResource;
import static android.graphics.drawable.Icon.createWithResource;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static com.mediaplayer.strings.R.drawable.*;
import static com.mediaplayer.strings.R.mipmap.ic_launcher;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.*;
import static com.mediaplayer.strings.utilities.SQLConstants.ONE;
import static com.mediaplayer.strings.utilities.SQLConstants.ZERO;
import static java.lang.String.valueOf;

public class MediaPlayerService extends IntentService {
    private static final String LOG_TAG = "MediaPlayerService";
    private static MediaPlayer mp;
    public static boolean isServiceRunning;
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

        while(isServiceRunning) {}

        Log.d("isServiceRunning: ", valueOf(isServiceRunning));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Binding activity to service...");

        Track selectedTrack = (Track) intent.getSerializableExtra(KEY_SELECTED_TRACK);
        String selectedPlaylist = intent.getStringExtra(KEY_SELECTED_PLAYLIST);
        playSong(selectedTrack);
        Notification notification = createNotification(selectedTrack, selectedPlaylist);
        startForeground(1, notification);
        Log.d(LOG_TAG, "Is foreground?: true");
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
        } catch(IOException | IllegalStateException e) {
            e.printStackTrace();
            //Utilities.reportCrash(e);
        }

        Log.d(LOG_TAG, "END: The playSong() event");
    }

    public Notification createNotification(Track selectedTrack, String selectedPlaylist) {
        Notification notification = null;
        Action prevAction = null, pauseAction = null, playAction = null, nextAction = null;
        Bitmap bm;
        int zero = ZERO;
        int flag = FLAG_CANCEL_CURRENT;
        String keySelectedTrack = KEY_SELECTED_TRACK;
        String keySelectedPlaylist = KEY_SELECTED_PLAYLIST;
        String keyTrackOrigin = KEY_TRACK_ORIGIN;
        String origin = TAG_NOTIFICATION;
        byte data[] = selectedTrack.getAlbumArt();

        try {
            Builder builder = new Builder(this);
            MediaStyle mediaStyle = new MediaStyle();
            MediaSession mMediaSession = new MediaSession(this, TAG_MEDIA_SESSION);

            //Setting mediastyle attributes
            mediaStyle.setShowActionsInCompactView(0, 1, 2);
            mediaStyle.setMediaSession(mMediaSession.getSessionToken());

            //Setting builder attributes
            builder.setStyle(mediaStyle);
            builder.setContentTitle(selectedTrack.getTrackTitle());
            builder.setContentText(selectedTrack.getArtistName());
            builder.setSubText(selectedTrack.getAlbumName());
            builder.setVisibility(VISIBILITY_PUBLIC);
            builder.setSmallIcon(ic_launcher);
            builder.setShowWhen(false);

            if(data.length != 0) {
                bm = decodeByteArray(data, ZERO, data.length);

                if(bm != null) {
                    builder.setLargeIcon(bm);
                } else {
                    bm = decodeResource(getResources(), img_default_album_art_thumb);
                    builder.setLargeIcon(bm);
                }
            } else {
                bm = decodeResource(getResources(), img_default_album_art_thumb);
                builder.setLargeIcon(bm);
            }

            //Creating intents
            Intent prevIntent = new Intent(this, MediaPlayerActivity.class);
            Intent pauseIntent = new Intent(this, MediaPlayerActivity.class);
            Intent playIntent = new Intent(this, MediaPlayerActivity.class);
            Intent nextIntent = new Intent(this, MediaPlayerActivity.class);
            //Intent deleteIntent = new Intent(this, MediaPlayerActivity.class);
            Intent deleteIntent = new Intent(this, MediaPlayerService.class);
            Intent openIntent = new Intent(this, MediaPlayerActivity.class);

            //Setting actions and extras for intents
            prevIntent.setAction(PREVIOUS)
                      .putExtra(keySelectedTrack, selectedTrack)
                      .putExtra(keySelectedPlaylist, selectedPlaylist)
                      .putExtra(keyTrackOrigin, origin);
            pauseIntent.setAction(PAUSE)
                       .putExtra(keySelectedTrack, selectedTrack)
                       .putExtra(keySelectedPlaylist, selectedPlaylist)
                       .putExtra(keyTrackOrigin, origin);
            playIntent.setAction(PLAY)
                      .putExtra(keySelectedTrack, selectedTrack)
                      .putExtra(keySelectedPlaylist, selectedPlaylist)
                      .putExtra(keyTrackOrigin, origin);
            nextIntent.setAction(NEXT)
                      .putExtra(keySelectedTrack, selectedTrack)
                      .putExtra(keySelectedPlaylist, selectedPlaylist)
                      .putExtra(keyTrackOrigin, origin);
            deleteIntent.setAction(STOP);
            openIntent.setAction(OPEN)
                      .putExtra(keySelectedTrack, selectedTrack)
                      .putExtra(keySelectedPlaylist, selectedPlaylist)
                      .putExtra(keyTrackOrigin, origin);

            //Creating pending intents
            PendingIntent prevPendingIntent = getActivity(this, zero, prevIntent, flag);
            PendingIntent pausePendingIntent = getActivity(this, zero, pauseIntent, flag);
            PendingIntent playPendingIntent = getActivity(this, zero, playIntent, flag);
            PendingIntent nextPendingIntent = getActivity(this, zero, nextIntent, flag);
            //PendingIntent deletePendingIntent = PendingIntent.getActivity(this, zero, deleteIntent, flag);
            PendingIntent deletePendingIntent = getService(this, zero, deleteIntent, flag);
            PendingIntent openPendingIntent = getActivity(this, zero, openIntent, flag);

            //Checking OS build version for notification compatibility
            if(SDK_INT == LOLLIPOP || SDK_INT == LOLLIPOP_MR1) {
                //Creating notification actions
                prevAction = new Action.Builder(ic_skip_previous_white_36dp, PREVIOUS, prevPendingIntent).build();
                pauseAction = new Action.Builder(ic_pause_white_36dp, PAUSE, pausePendingIntent).build();
                playAction = new Action.Builder(ic_play_arrow_white_36dp, PLAY, playPendingIntent).build();
                nextAction = new Action.Builder(ic_skip_next_white_36dp, NEXT, nextPendingIntent).build();
            } else if(SDK_INT == M) {
                //Creating Icons for actions
                Icon prevIcon = createWithResource(this, ic_skip_previous_white_36dp);
                Icon pauseIcon = createWithResource(this, ic_pause_white_36dp);
                Icon playIcon = createWithResource(this, ic_play_arrow_white_36dp);
                Icon nextIcon = createWithResource(this, ic_skip_next_white_36dp);

                //Creating notification actions
                prevAction = new Action.Builder(prevIcon, PREVIOUS, prevPendingIntent).build();
                pauseAction = new Action.Builder(pauseIcon, PAUSE, pausePendingIntent).build();
                playAction = new Action.Builder(playIcon, PLAY, playPendingIntent).build();
                nextAction = new Action.Builder(nextIcon, NEXT, nextPendingIntent).build();
            }

            //Adding notification actions to the builder
            builder.addAction(prevAction);

            if(mp.isPlaying()) {
                builder.addAction(pauseAction);
                builder.setOngoing(true);
            } else {
                builder.addAction(playAction);
                builder.setOngoing(false);
            }

            builder.addAction(nextAction);
            builder.setDeleteIntent(deletePendingIntent);
            builder.setContentIntent(openPendingIntent);

            //Building notification
            notification = builder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.notify(ONE, notification);
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        Log.d(LOG_TAG, "Notification created");
        return notification;
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

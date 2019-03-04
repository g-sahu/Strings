package com.mediaplayer.strings.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.mediaplayer.strings.activities.MediaPlayerActivity;
import com.mediaplayer.strings.beans.Track;

import java.io.IOException;

import static android.app.Notification.Action;
import static android.app.Notification.Builder;
import static android.app.Notification.MediaStyle;
import static android.app.Notification.VISIBILITY_PUBLIC;
import static android.app.NotificationManager.IMPORTANCE_LOW;
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
import static android.os.Build.VERSION_CODES.O;
import static android.support.v4.app.NotificationCompat.CATEGORY_SERVICE;
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
    private static final String CHANNEL_ID = "1";

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
        String filePath = selectedTrack.getTrackLocation();
        mp = (mp ==  null) ? new MediaPlayer() : mp;

        try {
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            Log.d(LOG_TAG, "Now Playing: " + selectedTrack.getTrackTitle());
        } catch(IOException | IllegalStateException e) {
            e.printStackTrace();
            //Utilities.reportCrash(e);
        }
    }

    public Notification createNotification(Track selectedTrack, String selectedPlaylist) {
        Action prevAction = null, pauseAction = null, playAction = null, nextAction = null;

        try {
            //Setting mediastyle attributes
            MediaSession mMediaSession = new MediaSession(this, TAG_MEDIA_SESSION);
            MediaStyle mediaStyle = new MediaStyle();
            mediaStyle.setShowActionsInCompactView(0, 1, 2);
            mediaStyle.setMediaSession(mMediaSession.getSessionToken());

            //Setting builder attributes
            Builder builder = new Builder(this);
            builder.setStyle(mediaStyle);
            builder.setContentTitle(selectedTrack.getTrackTitle());
            builder.setContentText(selectedTrack.getArtistName());
            builder.setSubText(selectedTrack.getAlbumName());
            builder.setVisibility(VISIBILITY_PUBLIC);
            builder.setSmallIcon(ic_launcher);
            builder.setShowWhen(false);
            byte data[] = selectedTrack.getAlbumArt();
            Bitmap bm = (data.length != 0) ? decodeByteArray(data, ZERO, data.length) : decodeResource(getResources(), img_default_album_art_thumb);
            builder.setLargeIcon(bm);

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
                      .putExtra(KEY_SELECTED_TRACK, selectedTrack)
                      .putExtra(KEY_SELECTED_PLAYLIST, selectedPlaylist)
                      .putExtra(KEY_TRACK_ORIGIN, TAG_NOTIFICATION);
            pauseIntent.setAction(PAUSE)
                       .putExtra(KEY_SELECTED_TRACK, selectedTrack)
                       .putExtra(KEY_SELECTED_PLAYLIST, selectedPlaylist)
                       .putExtra(KEY_TRACK_ORIGIN, TAG_NOTIFICATION);
            playIntent.setAction(PLAY)
                      .putExtra(KEY_SELECTED_TRACK, selectedTrack)
                      .putExtra(KEY_SELECTED_PLAYLIST, selectedPlaylist)
                      .putExtra(KEY_TRACK_ORIGIN, TAG_NOTIFICATION);
            nextIntent.setAction(NEXT)
                      .putExtra(KEY_SELECTED_TRACK, selectedTrack)
                      .putExtra(KEY_SELECTED_PLAYLIST, selectedPlaylist)
                      .putExtra(KEY_TRACK_ORIGIN, TAG_NOTIFICATION);
            deleteIntent.setAction(STOP);
            openIntent.setAction(OPEN)
                      .putExtra(KEY_SELECTED_TRACK, selectedTrack)
                      .putExtra(KEY_SELECTED_PLAYLIST, selectedPlaylist)
                      .putExtra(KEY_TRACK_ORIGIN, TAG_NOTIFICATION);

            //Creating pending intents
            PendingIntent prevPendingIntent = getActivity(this, ZERO, prevIntent, FLAG_CANCEL_CURRENT);
            PendingIntent pausePendingIntent = getActivity(this, ZERO, pauseIntent, FLAG_CANCEL_CURRENT);
            PendingIntent playPendingIntent = getActivity(this, ZERO, playIntent, FLAG_CANCEL_CURRENT);
            PendingIntent nextPendingIntent = getActivity(this, ZERO, nextIntent, FLAG_CANCEL_CURRENT);
            //PendingIntent deletePendingIntent = PendingIntent.getActivity(this, ZERO, deleteIntent, FLAG_CANCEL_CURRENT);
            PendingIntent deletePendingIntent = getService(this, ZERO, deleteIntent, FLAG_CANCEL_CURRENT);
            PendingIntent openPendingIntent = getActivity(this, ZERO, openIntent, FLAG_CANCEL_CURRENT);

            //Checking OS build version for notification compatibility
            if(SDK_INT == LOLLIPOP || SDK_INT == LOLLIPOP_MR1) {
                //Creating notification actions
                prevAction = new Action.Builder(ic_skip_previous_white_36dp, PREVIOUS, prevPendingIntent).build();
                pauseAction = new Action.Builder(ic_pause_white_36dp, PAUSE, pausePendingIntent).build();
                playAction = new Action.Builder(ic_play_arrow_white_36dp, PLAY, playPendingIntent).build();
                nextAction = new Action.Builder(ic_skip_next_white_36dp, NEXT, nextPendingIntent).build();
            } else if(SDK_INT >= M) {
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
            builder.setCategory(CATEGORY_SERVICE);

            //Building notification
            if (SDK_INT >= O) {
                createNotificationChannel();
                builder.setChannelId(CHANNEL_ID);
            }

            Notification notification = builder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.notify(ONE, notification);
            return notification;
        } catch(Exception e) {
            Log.e(LOG_TAG_EXCEPTION, e.getMessage());
            //Utilities.reportCrash(e);
        }

        Log.d(LOG_TAG, "Notification created");
        return null;
    }

    @RequiresApi(api = O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Media Playback", IMPORTANCE_LOW);
        channel.setDescription("Media playback controls");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
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

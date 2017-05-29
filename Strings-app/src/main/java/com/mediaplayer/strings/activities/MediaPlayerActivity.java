package com.mediaplayer.strings.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mediaplayer.strings.R;
import com.mediaplayer.strings.beans.Track;
import com.mediaplayer.strings.services.MediaPlayerService;
import com.mediaplayer.strings.utilities.MediaLibraryManager;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;
import com.mediaplayer.strings.utilities.MediaPlayerStateManager;
import com.mediaplayer.strings.utilities.MessageConstants;
import com.mediaplayer.strings.utilities.SQLConstants;
import com.mediaplayer.strings.utilities.Utilities;

import java.util.ArrayList;
import java.util.Random;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.LOG_TAG_EXCEPTION;

public class MediaPlayerActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String LOG_TAG = "MediaPlayerActivity";

    private MediaPlayer mp;
    private Track selectedTrack;
    private String selectedPlaylist;
    private SeekBar songProgressBar;
    private TextView timeElapsed;
    private Toast toast;
    private MediaPlayerService mpService;
    private MediaPlayerStateManager mpStateManager;
    private int currentIndex, playlistSize, width;
    private String origin, playlistName, mpState = MediaPlayerConstants.STOPPED, repeatMode = MediaPlayerConstants.REPEAT_OFF;
    private ImageButton playButton, nextButton, previousButton, repeatButton, shuffleButton;
    private boolean isBound, isShuffling;

    private final Handler progressHandler = new Handler();
    private ArrayList<Integer> tracksCompleted = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Log.d(LOG_TAG, "MediaPlayerActivity created");

        //Fetching MediaPlayerStateManager instance
        mpStateManager = ((MediaPlayerStateManager) getApplicationContext());

        //Fetching device display width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        //Extracting extra info from intent
        Intent intent = getIntent();
        String action = intent.getAction();
        selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        selectedPlaylist = intent.getStringExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        playlistName = intent.getStringExtra(MediaPlayerConstants.KEY_PLAYLIST_TITLE);
        origin = intent.getStringExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN);

        //Initialising MediaPlayerActivity
        initializePlayer(selectedTrack);

        if(mpStateManager.getMpState() != null) {
            //Restoring media player state from MediaPlayerStateManager
            restoreMediaPlayerState();
        }

        if(action != null) {
            switch(action) {
                case MediaPlayerConstants.PREVIOUS:
                    previousButton.performClick();
                    break;

                case MediaPlayerConstants.PAUSE:
                    playButton.setTag(R.id.playButton, origin);
                    playButton.performClick();
                    break;

                case MediaPlayerConstants.PLAY:
                    playButton.setTag(R.id.playButton, origin);
                    playButton.performClick();
                    break;

                case MediaPlayerConstants.NEXT:
                    nextButton.performClick();
                    break;
            }
        }

        Log.d(LOG_TAG, "END: The onCreate() event");
    }

    public void play(View view) {
        Object tagObject;
        mp = MediaPlayerService.getMp();

        if(view != null) {
            tagObject = view.getTag(R.id.playButton);

            if(tagObject == null) {
                origin = MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY;
            }

            view.setTag(R.id.playButton, null);
        }

        if(mp != null) {
            switch(mpState) {
                case MediaPlayerConstants.PLAYING:
                    switch(origin) {
                        case MediaPlayerConstants.TAG_SONGS_LIST_VIEW:
                            mp.reset();
                            playSong(selectedTrack);
                            break;

                        case MediaPlayerConstants.TAG_PLAYLIST_ACTIVITY:
                            mp.reset();
                            playSong(selectedTrack);
                            break;

                        case MediaPlayerConstants.TAG_NOTIFICATION:

                        case MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY:
                            //If already playing, pause the current track
                            mp.pause();
                            mpState = MediaPlayerConstants.PAUSED;
                            playButton.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);

                            //If MediaPlayerService object does not exists, it means service is not bound. Hence, bind the service
                            if(mpService == null) {
                                Intent serviceIntent = new Intent(this, MediaPlayerService.class);
                                serviceIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
                                serviceIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, selectedPlaylist);
                                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                            } else {
                                mpService.stopForeground(false);
                                Log.d(LOG_TAG, "Is foreground?: false");
                                mpService.createNotification(selectedTrack, selectedPlaylist);
                            }

                            break;
                    }

                    break;

                case MediaPlayerConstants.PAUSED:
                    //Else, if paused, resume current track
                    mp.start();
                    mpState = MediaPlayerConstants.PLAYING;
                    playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);

                    //If MediaPlayerService object does not exists, it means service is not bound. Hence, bind the service
                    if(mpService == null) {
                        Intent serviceIntent = new Intent(this, MediaPlayerService.class);
                        serviceIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
                        serviceIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, selectedPlaylist);
                        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                    } else {
                        mpService.startForeground(SQLConstants.ONE, mpService.createNotification(selectedTrack, selectedPlaylist));
                    }

                    break;

                case MediaPlayerConstants.STOPPED:
                    //Else, if stopped, start playback
                    mp = MediaPlayerService.getMp();
                    mp.reset();
                    MediaPlayerService.setMp(mp);
                    playSong(selectedTrack);
                    songProgressBar.setProgress(SQLConstants.ZERO);
                    songProgressBar.setMax(SQLConstants.HUNDRED);
                    break;
            }
        }
    }

    public void next(View view) {
        mp = MediaPlayerService.getMp();
        mp.reset();
        MediaPlayerService.setMp(mp);

        if(isShuffling) {
            //If shuffling is on, play a random song
            selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, getNextIndex());
        } else if(repeatMode.equals(MediaPlayerConstants.REPEAT_CURRENT)) {
            //Else, if repeating current is on, restart the same song

        } else if(MediaLibraryManager.isLastTrack(selectedPlaylist, currentIndex)) {
            if(repeatMode.equals(MediaPlayerConstants.REPEAT_PLAYLIST)) {
                // Else, if repeating all is on and is currently playing the last song,
                // play next song in the playlist
                selectedTrack = MediaLibraryManager.getFirstTrack(selectedPlaylist);
            } else {
                //Else, stop playback
                playButton.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);

                //Showing message to the user
                showToastMessage(MessageConstants.END_OF_PLAYLIST);

                //Setting mediaplayer state in MediaPlayerStateManager
                mpState = MediaPlayerConstants.STOPPED;

                //Stopping foreground service and progress bar and updating notification
                mpService.stopForeground(false);
                mpService.createNotification(selectedTrack, selectedPlaylist);
                stopProgressBar();
                return;
            }
        } else {
            //Else, play the next song in the playlist
            selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, ++currentIndex);
        }

        initializePlayer(selectedTrack);
        playSong(selectedTrack);
    }

    public void previous(View view) {
        mp = MediaPlayerService.getMp();
        mp.reset();
        MediaPlayerService.setMp(mp);

        if(isShuffling) {
            //If shuffling is on, play a random song
            selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, getNextIndex());
        } else if(repeatMode.equals(MediaPlayerConstants.REPEAT_CURRENT)) {
            //Else, if repeating current is on, restart the same song

        } else if(MediaLibraryManager.isFirstTrack(currentIndex)) {
            if(repeatMode.equals(MediaPlayerConstants.REPEAT_PLAYLIST)) {
                // Else, if repeating all is on and is currently playing the first song,
                // play last song in the playlist
                selectedTrack = MediaLibraryManager.getLastTrack(selectedPlaylist);
            } else {
                //Else, stop playback
                playButton.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                mpState = MediaPlayerConstants.STOPPED;

                //Showing message to the user
                showToastMessage(MessageConstants.BEG_OF_PLAYLIST);

                //Stopping foreground service and updating notification
                mpService.stopForeground(false);
                mpService.createNotification(selectedTrack, selectedPlaylist);

                stopProgressBar();
                return;
            }
        } else {
            //Else, play the previous song
            selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, --currentIndex);
        }

        initializePlayer(selectedTrack);
        playSong(selectedTrack);
    }

    public void shuffle(View view) {
        if(!isShuffling) {
            isShuffling = true;
            shuffleButton.setImageResource(R.drawable.ic_shuffle_on_red_24dp);

            //Showing message to the user
            showToastMessage(MessageConstants.SHUFFLING_ON);
        } else {
            isShuffling = false;
            shuffleButton.setImageResource(R.drawable.ic_shuffle_off_black_24dp);

            //Showing message to the user
            showToastMessage(MessageConstants.SHUFFLING_OFF);
        }
    }

    public void repeat(View view) {
        switch(repeatMode) {
            case MediaPlayerConstants.REPEAT_OFF:
                repeatMode = MediaPlayerConstants.REPEAT_CURRENT;
                repeatButton.setImageResource(R.drawable.ic_repeat_one_red_24dp);

                //Showing message to the user
                showToastMessage(MessageConstants.LOOPING_TRACK);
                break;

            case MediaPlayerConstants.REPEAT_CURRENT:
                repeatMode = MediaPlayerConstants.REPEAT_PLAYLIST;
                repeatButton.setImageResource(R.drawable.ic_repeat_all_red_24dp);

                //Showing message to the user
                showToastMessage(MessageConstants.LOOPING_PLAYLIST);
                break;

            case MediaPlayerConstants.REPEAT_PLAYLIST:
                repeatMode = MediaPlayerConstants.REPEAT_OFF;
                repeatButton.setImageResource(R.drawable.ic_repeat_off_black_24dp);

                //Showing message to the user
                showToastMessage(MessageConstants.LOOPING_OFF);
                break;
        }
    }

    private void initializePlayer(Track requestedTrack) {
        Log.d(LOG_TAG, "Initializing Media Player...");

        playButton = (ImageButton) findViewById(R.id.playButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        previousButton = (ImageButton) findViewById(R.id.previousButton);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        TextView titleBar = (TextView) findViewById(R.id.titleBar);
        TextView artistBar = (TextView) findViewById(R.id.artistBar);
        TextView albumBar = (TextView) findViewById(R.id.albumBar);
        TextView playingFrom = (TextView) findViewById(R.id.playingFrom);
        timeElapsed = (TextView) findViewById(R.id.timeElapsed);
        TextView trackDuration = (TextView) findViewById(R.id.trackDuration);
        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        ImageView albumArtThumbnail = (ImageView) findViewById(R.id.albumArtThumbnail);
        String songTitle = requestedTrack.getTrackTitle();
        String albumName = requestedTrack.getAlbumName();
        String artistName = requestedTrack.getArtistName();
        String songDuration = String.valueOf(requestedTrack.getTrackDuration());
        byte[] data = requestedTrack.getAlbumArt();

        switch(selectedPlaylist) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                currentIndex = requestedTrack.getTrackIndex();
                playlistName = MediaPlayerConstants.TITLE_LIBRARY;
                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                currentIndex = requestedTrack.getCurrentTrackIndex();
                break;
        }

        //Setting SeekBar listener
        songProgressBar.setOnSeekBarChangeListener(this);
        songProgressBar.setProgress(SQLConstants.ZERO);
        songProgressBar.setMax(SQLConstants.HUNDRED);

        mp = MediaPlayerService.getMp();

        if(mp == null) {
            mp = new MediaPlayer();
            MediaPlayerService.setMp(mp);
        }

        //Setting listener for track completion
        mp.setOnCompletionListener(this);

        //Setting error listener
        mp.setOnErrorListener(this);

        titleBar.setText(songTitle);
        artistBar.setText(artistName);
        albumBar.setText(albumName);
        playingFrom.setText(playlistName);
        trackDuration.setText(Utilities.milliSecondsToTimer(Long.parseLong(songDuration)));

        if(data.length != 0) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, SQLConstants.ZERO, data.length);

            if(bm != null) {
                int size = width / 2;
                //albumArt.setImageBitmap(Bitmap.createScaledBitmap(bm, size, size, false));
                albumArt.setImageBitmap(bm);
                albumArtThumbnail.setImageBitmap(bm);
            } else {
                albumArt.setImageResource(R.drawable.img_default_album_art);
                albumArtThumbnail.setImageResource(R.drawable.img_default_album_art_thumb);
            }
        } else {
            albumArt.setImageResource(R.drawable.img_default_album_art);
            albumArtThumbnail.setImageResource(R.drawable.img_default_album_art_thumb);
        }

        Log.d(LOG_TAG, "Media Player initialized");
    }

    private void playSong(Track selectedTrack) {
        Log.d(LOG_TAG, "START: The playSong() event");

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, selectedPlaylist);

        //Checking if service is running
        if(MediaPlayerService.isServiceRunning) {
            //Checking if MediaplayerActivity is bound to service
            if(!isBound) {
                //Binding to MediaPlayerService
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

            //Else, if service is running and bound and track is paused, resume playback
            } else if(mpState.equals(MediaPlayerConstants.PAUSED)){
                if(mpService != null) {
                    mpService.playSong(selectedTrack);
                    mpService.startForeground(SQLConstants.ONE, mpService.createNotification(selectedTrack, selectedPlaylist));
                }

            //Else, if track is stoppped, play current track
            } else {
                if(mpService != null) {
                    mpService.playSong(selectedTrack);
                    mpService.startForeground(SQLConstants.ONE, mpService.createNotification(selectedTrack, selectedPlaylist));
                }
            }
        } else {
            Log.d(LOG_TAG, "Service not running");
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        mpState = MediaPlayerConstants.PLAYING;
        playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
        Log.d(LOG_TAG, "END: The playSong() event");
    }

    //Update timer on seekbar
    private void updateProgressBar() {
        progressHandler.postDelayed(mUpdateTimeTask, 5);
    }

    //Background Runnable thread for updating progress bar
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                mp = MediaPlayerService.getMp();
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying time completed playing
                timeElapsed.setText(Utilities.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = Utilities.getProgressPercentage(currentDuration, totalDuration);
                songProgressBar.setProgress(progress);

                // Running this thread after 5 milliseconds
                progressHandler.postDelayed(this, 5);
            } catch(Exception e) {
                Log.e(LOG_TAG_EXCEPTION, e.getMessage());
                //Utilities.reportCrash(e);
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        progressHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        progressHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = Utilities.progressToTimer(seekBar.getProgress(), totalDuration);
        mp.seekTo(currentPosition);
        updateProgressBar();
    }

    private int getNextIndex() {
        boolean isPlayed;
        tracksCompleted.add(currentIndex);
        int nextIndex = 0;

        switch(selectedPlaylist) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                playlistSize = MediaLibraryManager.getTrackInfoListSize();
                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                playlistSize = MediaLibraryManager.getSelectedPlaylist().size();
                break;
        }

        if(tracksCompleted.size() != playlistSize) {
            nextIndex = new Random().nextInt(playlistSize);
            isPlayed = tracksCompleted.contains(nextIndex);

            while(isPlayed) {
                nextIndex = new Random().nextInt(playlistSize);
                isPlayed = tracksCompleted.contains(nextIndex);
            }
        } else {
            tracksCompleted.clear();
        }

        return nextIndex;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Mediaplayer activity paused");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        origin = intent.getStringExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN);

        if(action != null) {
            switch(action) {
                case MediaPlayerConstants.PREVIOUS:
                    previousButton.performClick();
                    break;

                case MediaPlayerConstants.PAUSE:
                    playButton.performClick();
                    break;

                case MediaPlayerConstants.PLAY:
                    playButton.performClick();
                    break;

                case MediaPlayerConstants.NEXT:
                    nextButton.performClick();
                    break;

                /*case MediaPlayerConstants.STOP:
                    Log.d(LOG_TAG, "Delete intent received");
                    mpService.stopSelf();
                    break;*/
            }
        }

        setIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onStart();
        Log.d(LOG_TAG, "Mediaplayer activity started");

        //Updating progress bar
        updateProgressBar();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stopping progress bar and saving media player state
        stopProgressBar();
        saveMediaPlayerState();

        Log.d(LOG_TAG, "Mediaplayer activity stopped");
    }

    private void stopProgressBar() {
        progressHandler.removeCallbacks(mUpdateTimeTask);
        timeElapsed.setText(Utilities.milliSecondsToTimer(SQLConstants.ZERO));
        songProgressBar.setProgress(SQLConstants.ZERO);
    }

    //Defines callbacks for service binding, passed to bindService()
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.MyBinder binder = (MediaPlayerService.MyBinder) service;
            mpService = binder.getService();
            Log.d(LOG_TAG, "Service connected: " + mpService);
            isBound = true;

            if(MediaPlayerService.isServiceRunning) {
                switch(origin) {
                    case MediaPlayerConstants.TAG_SONGS_LIST_VIEW:
                        mpService.playSong(selectedTrack);
                        mpService.startForeground(SQLConstants.ONE, mpService.createNotification(selectedTrack, selectedPlaylist));
                        break;

                    case MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY:
                        mpService.stopForeground(false);
                        Log.d(LOG_TAG, "Is foreground?: false");
                        mpService.createNotification(selectedTrack, selectedPlaylist);
                        break;

                    case MediaPlayerConstants.TAG_NOTIFICATION:
                        mpService.playSong(selectedTrack);
                        mpService.startForeground(SQLConstants.ONE, mpService.createNotification(selectedTrack, selectedPlaylist));
                        break;

                    case MediaPlayerConstants.TAG_PLAYLIST_ACTIVITY:
                        mpService.playSong(selectedTrack);
                        mpService.startForeground(SQLConstants.ONE, mpService.createNotification(selectedTrack, selectedPlaylist));
                        break;

                    default:
                        break;
                }
            }

            updateProgressBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp = MediaPlayerService.getMp();

        if(!mp.isPlaying()) {
            //Checking if track is on loop
            if(repeatMode.equals(MediaPlayerConstants.REPEAT_CURRENT)) {
                mp.reset();
                playSong(selectedTrack);

                //Checking if it is not the last track in the playlist
            } else if(!MediaLibraryManager.isLastTrack(selectedPlaylist, currentIndex)) {
                if(isShuffling) {
                    // Play next random song in the playlist
                    currentIndex = getNextIndex();
                } else {
                    // Play next song in the playlist
                    ++currentIndex;
                }

                mp.reset();
                selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, currentIndex);
                initializePlayer(selectedTrack);
                playSong(selectedTrack);

                //Checking if playlist is on loop
            } else if(repeatMode.equals(MediaPlayerConstants.REPEAT_PLAYLIST)) {
                if(isShuffling) {
                    // Play next random song in the playlist
                    currentIndex = getNextIndex();
                } else {
                    // Play first song in the playlist
                    currentIndex = 0;
                }

                mp.reset();
                selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, currentIndex);
                initializePlayer(selectedTrack);
                playSong(selectedTrack);

                //Else, if looping is off and it is the last track in the playlist
            } else {
                if(isShuffling) {
                    currentIndex = getNextIndex();
                    mp.reset();
                    selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, currentIndex);
                    initializePlayer(selectedTrack);
                    playSong(selectedTrack);
                } else {
                    //Else, stop playback
                    mp.reset();
                    playButton.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                    stopProgressBar();
                    mpState = MediaPlayerConstants.STOPPED;
                    mpService.stopForeground(false);
                    mpService.createNotification(selectedTrack, selectedPlaylist);
                }
            }
        }
    }

    private void showToastMessage(String toastText) {
        if(toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void saveMediaPlayerState() {
        mpStateManager.setMpState(mpState);
        mpStateManager.setRepeatMode(repeatMode);
        mpStateManager.setShuffling(isShuffling);
    }

    private void restoreMediaPlayerState() {
        mpState = mpStateManager.getMpState();
        repeatMode = mpStateManager.getRepeatMode();
        isShuffling = mpStateManager.isShuffling();
        mp = MediaPlayerService.getMp();

        //Updating play button
        switch (mpState) {
            case MediaPlayerConstants.PLAYING:
                playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
                break;

            case MediaPlayerConstants.PAUSED:

            case MediaPlayerConstants.STOPPED:
                playButton.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                break;
        }

        //Updating repeat button
        switch (repeatMode) {
            case MediaPlayerConstants.REPEAT_CURRENT:
                repeatButton.setImageResource(R.drawable.ic_repeat_one_red_24dp);
                break;

            case MediaPlayerConstants.REPEAT_PLAYLIST:
                repeatButton.setImageResource(R.drawable.ic_repeat_all_red_24dp);
                break;

            case MediaPlayerConstants.REPEAT_OFF:
                repeatButton.setImageResource(R.drawable.ic_repeat_off_black_24dp);
                break;
        }

        //Updating shuffle button
        if (isShuffling) {
            shuffleButton.setImageResource(R.drawable.ic_shuffle_on_red_24dp);
        } else {
            shuffleButton.setImageResource(R.drawable.ic_shuffle_off_black_24dp);
        }

        //Updating progress bar
        updateProgressBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unbinding from the service
        if(isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }

        Log.d(LOG_TAG, "Mediaplayer activity destroyed");
    }
}
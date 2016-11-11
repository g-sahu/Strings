package com.mediaplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mediaplayer.R;
import com.mediaplayer.beans.Track;
import com.mediaplayer.services.MediaPlayerService;
import com.mediaplayer.utilities.MediaLibraryManager;
import com.mediaplayer.utilities.MediaPlayerConstants;
import com.mediaplayer.utilities.MessageConstants;
import com.mediaplayer.utilities.Utilities;

import java.util.ArrayList;
import java.util.Random;

public class MediaPlayerActivity extends AppCompatActivity { //implements SeekBar.OnSeekBarChangeListener {
    private static String LOG_TAG = "MediaPlayerActivity";
    private static MediaPlayer mp;
    private static ImageButton playButton, nextButton, previousButton, repeatButton, shuffleButton;
    private static Track selectedTrack;
    private static String selectedPlaylist;
    private static SeekBar songProgressBar;
    private static TextView titleBar, artistBar, albumBar, timeElapsed, trackDuration;
    private static ImageView albumArt, albumArtThumbnail;
    private static String songTitle, albumName, artistName, songDuration;

    private Handler mHandler = new Handler();
    private ArrayList<Integer> tracksCompleted = new ArrayList<Integer>();
    private boolean isPaused = false, isIdle = true, isRepeatingAll = false, isRepeatingCurrent = false, isShuffling = false;
    private int currentIndex;
    private int playlistSize;
    private byte data[];
    private Bitmap bm;
    //private LinearLayout albumArtLayout;
    private Toast toast;
    private Context context;
    private String toastText;
    private String origin;

    private MediaPlayerService mService;
    private boolean mBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Log.d(LOG_TAG, "MediaPlayerActivity created");

        context = getApplicationContext();
        Intent intent = getIntent();
        String action = intent.getAction();

        selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        selectedPlaylist = intent.getStringExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        origin = intent.getStringExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN);
        initializePlayer(selectedTrack);

        if(action != null) {
            switch(action) {
                case MediaPlayerConstants.PREVIOUS:
                    previousButton.performClick();
                    break;

                case MediaPlayerConstants.PAUSE:
                    playButton.setTag(R.id.playButton, MediaPlayerConstants.TAG_SONGS_LIST_VIEW);
                    playButton.performClick();
                    break;

                case MediaPlayerConstants.PLAY:
                    playButton.setTag(R.id.playButton, MediaPlayerConstants.TAG_SONGS_LIST_VIEW);
                    playButton.performClick();
                    break;

                case MediaPlayerConstants.NEXT:
                    nextButton.performClick();
                    break;
            }

            return;
        }

        playSong(selectedTrack);

        // Listeners
        //songProgressBar.setOnSeekBarChangeListener(this);

        /*if(mp != null) {
            // Set listener for playback completion
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (isRepeatingCurrent) {
                        mp.reset();
                        isIdle = true;
                        //playSong(currentIndex);
                        playSong(selectedTrack);

                    } else if (currentIndex < playlistSize - 1) {
                        if (isShuffling) {
                            currentIndex = getNextIndex();
                        } else {
                            currentIndex = currentIndex + 1;
                        }

                        // Play next song in the playlist
                        mp.reset();
                        isIdle = true;
                        playSong(currentIndex);
                    } else if (isRepeatingAll) {
                        if (isShuffling) {
                            currentIndex = getNextIndex();
                        } else {
                            currentIndex = 0;
                        }

                        // Play first song in the playlist
                        mp.reset();
                        isIdle = true;
                        playSong(currentIndex);
                    } else {
                        if (isShuffling) {
                            currentIndex = getNextIndex();
                            mp.reset();
                            isIdle = true;
                            playSong(currentIndex);
                        } else {
                            // Stop playback
                            mp.stop();
                            playButton.setImageResource(R.drawable.play_button);
                            songProgressBar.setProgress(0);
                            isPaused = false;
                        }
                    }
                }
            });
        }*/

        Log.d(LOG_TAG, "END: The onCreate() event");
    }

    public void play(View view) {
        Object tag;
        mp = MediaPlayerService.getMp();

        if(view != null) {
            tag = view.getTag(R.id.playButton);

            if(tag != null) {
                origin = tag.toString();
                view.setTag(R.id.playButton, null);
            } else {
                origin = MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY;
            }
        }

        if(mp != null) {
            if(mp.isPlaying()) {
                switch(origin) {
                    case MediaPlayerConstants.TAG_SONGS_LIST_VIEW:
                        mp.reset();
                        playSong(selectedTrack);
                        break;

                    case MediaPlayerConstants.TAG_PLAYLIST_ACTIVITY:
                        mp.reset();
                        playSong(selectedTrack);
                        break;

                    case MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY:
                        //If already playing, pause the current track
                        mp.pause();
                        isPaused = true;
                        playButton.setImageResource(R.drawable.play_button);
                        mService.stopForeground(false);
                        Log.d(LOG_TAG, "Is foreground?: false");
                        mService.createNotification(selectedTrack);
                        break;
                }
            } else if(!isPaused) {
                //Else, if stopped, start playback
                playSong(selectedTrack);
                isPaused = false;
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                //updateProgressBar();
            } else {
                //Else, if paused, resume current track
                mp.start();
                isPaused = false;
                playButton.setImageResource(R.drawable.pause_button);
                mService.startForeground(1, mService.createNotification(selectedTrack));
            }
        }
    }

    public void next(View view) {
        mp = MediaPlayerService.getMp();
        mp.reset();
        MediaPlayerService.setMp(mp);
        isIdle = true;

        if(isShuffling) {
            //If shuffling is on, play a random song
            selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, getNextIndex());
        } else if(isRepeatingCurrent) {
            //Else, if repeating current is on, restart the same song

        } else if(MediaLibraryManager.isLastTrack(selectedPlaylist, currentIndex)) {
            if(isRepeatingAll) {
                // Else, if repeating all is on and is currently playing the last song,
                // play next song in the playlist
                selectedTrack = MediaLibraryManager.getFirstTrack(selectedPlaylist);
            } else {
                //Stop playback
                playButton.setImageResource(R.drawable.play_button);
                toastText = MessageConstants.END_OF_PLAYLIST;
                toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
                toast.show();
                mService.createNotification(selectedTrack);
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
        isIdle = true;

        if(isShuffling) {
            //If shuffling is on, play a random song
            selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, getNextIndex());
        } else if(isRepeatingCurrent) {
            //Else, if repeating current is on, restart the same song

        } else if(MediaLibraryManager.isFirstTrack(currentIndex)) {
            if(isRepeatingAll) {
                // Else, if repeating all is on and is currently playing the first song,
                // play last song in the playlist
                selectedTrack = MediaLibraryManager.getLastTrack(selectedPlaylist);
            } else {
                //Stop playback
                playButton.setImageResource(R.drawable.play_button);
                toastText = MessageConstants.BEG_OF_PLAYLIST;
                toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
                toast.show();
                mService.createNotification(selectedTrack);
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
            shuffleButton.setImageResource(R.drawable.ic_shuffle_red_18dp);
            toastText = MessageConstants.SHUFFLING_ON;
        } else {
            isShuffling = false;
            shuffleButton.setImageResource(R.drawable.ic_shuffle_black_18dp);
            toastText = MessageConstants.SHUFFLING_OFF;
        }

        toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void repeat(View view) {
        if(!isRepeatingCurrent && !isRepeatingAll) {
            isRepeatingCurrent = true;
            repeatButton.setImageResource(R.drawable.ic_repeat_one_red_18dp);
            toastText = MessageConstants.LOOPING_TRACK;
        } else if (isRepeatingCurrent) {
            isRepeatingAll = true;
            isRepeatingCurrent = false;
            repeatButton.setImageResource(R.drawable.ic_repeat_red_18dp);
            toastText = MessageConstants.LOOPING_PLAYLIST;
        } else {
            isRepeatingCurrent = false;
            isRepeatingAll = false;
            repeatButton.setImageResource(R.drawable.ic_repeat_black_18dp);
            toastText = MessageConstants.LOOPING_OFF;
        }

        toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void initializePlayer(Track requestedTrack) {
        Log.d(LOG_TAG, "Initializing Media Player...");

        playButton = (ImageButton) findViewById(R.id.playButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        previousButton = (ImageButton) findViewById(R.id.previousButton);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        titleBar = (TextView) findViewById(R.id.titleBar);
        artistBar = (TextView) findViewById(R.id.artistBar);
        albumBar = (TextView) findViewById(R.id.albumBar);
        timeElapsed = (TextView) findViewById(R.id.timeElapsed);
        trackDuration = (TextView) findViewById(R.id.trackDuration);
        albumArt = (ImageView) findViewById(R.id.albumArt);
        albumArtThumbnail = (ImageView) findViewById(R.id.albumArtThumbnail);
        //albumArtLayout = (LinearLayout) findViewById(R.id.albumArtLayout);
        songTitle = requestedTrack.getTrackTitle();
        albumName = requestedTrack.getAlbumName();
        artistName = requestedTrack.getArtistName();
        songDuration = String.valueOf(requestedTrack.getTrackDuration());

        switch(selectedPlaylist) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                currentIndex = requestedTrack.getTrackIndex();
                break;

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
                currentIndex = requestedTrack.getCurrentTrackIndex();
                break;
        }

        data = requestedTrack.getAlbumArt();
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);

        if(data != null) {
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        if(mp == null) {
            mp = new MediaPlayer();
            MediaPlayerService.setMp(mp);
        }

        titleBar.setText(songTitle);
        artistBar.setText(artistName);
        albumBar.setText(albumName);
        trackDuration.setText(Utilities.milliSecondsToTimer(Long.parseLong(songDuration)));
        albumArt.setImageBitmap(bm);
        albumArtThumbnail.setImageBitmap(bm);

        Log.d(LOG_TAG, "Media Player initialized");
    }

    public void playSong(Track selectedTrack) {
        Log.d(LOG_TAG, "START: The playSong() event");

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);

        if(MediaPlayerService.isServiceRunning) {
            if(!MediaPlayerService.isServiceBound) {
                //Binding to MediaPlayerService
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            } else if(isPaused){
                mService.playSong(selectedTrack);
                mService.startForeground(1, mService.createNotification(selectedTrack));
            } else {
                mService.playSong(selectedTrack);
                mService.createNotification(selectedTrack);
            }
        } else {
            Log.d(LOG_TAG, "Service not running");
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        playButton.setImageResource(R.drawable.pause_button);
        //updateProgressBar();

        Log.d(LOG_TAG, "END: The playSong() event");
    }

    //Update timer on seekbar
    /*public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 10);
    }*/

    //Background Runnable thread for updating progress bar
    /*private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying time completed playing
            timeElapsed.setText(Utilities.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = Utilities.getProgressPercentage(currentDuration, totalDuration);
            songProgressBar.setProgress(progress);

            // Running this thread after 5 milliseconds
            mHandler.postDelayed(this, 5);
        }
    };*/

    /*@Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);

        int totalDuration = mp.getDuration();
        int currentPosition = Utilities.progressToTimer(seekBar.getProgress(), totalDuration);

        mp.seekTo(currentPosition);
        updateProgressBar();
    }*/

    public int getNextIndex() {
        boolean isPlayed;
        tracksCompleted.add(currentIndex);
        int nextIndex = 0;

        switch(selectedPlaylist) {
            case MediaPlayerConstants.KEY_PLAYLIST_LIBRARY:
                playlistSize = MediaLibraryManager.getTrackInfoListSize();
                break;

            case MediaPlayerConstants.KEY_PLAYLIST_OTHER:
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
        setIntent(intent);
        Log.d(LOG_TAG, action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Mediaplayer activity resumed");

        Intent intent = getIntent();
        String action = intent.getAction();

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

                case MediaPlayerConstants.STOP:
                    Log.d(LOG_TAG, "Delete intent received");
                    mService.stopSelf();
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "Mediaplayer activity stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unbind from the service
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        Log.d(LOG_TAG, "Mediaplayer activity destroyed");
    }

    //Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.MyBinder binder = (MediaPlayerService.MyBinder) service;
            mService = binder.getService();
            Log.d(LOG_TAG, "Service connected: " + mService);
            mBound = true;

            if(MediaPlayerService.isServiceRunning) {
                mService.playSong(selectedTrack);
                mService.createNotification(selectedTrack);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
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
    private static SeekBar songProgressBar;
    private static TextView titleBar, artistBar, timeElapsed, trackDuration;
    private static ImageView albumArt;
    private static String songTitle, albumName, artistName, songDuration;

    private Handler mHandler = new Handler();
    private ArrayList<Integer> tracksCompleted = new ArrayList<Integer>();
    private boolean isPaused = false, isIdle = true, isRepeatingAll = false, isRepeatingCurrent = false, isShuffling = false;
    private int currentIndex;
    private int playlistSize = MediaLibraryManager.getTrackInfoListSize();
    byte data[];
    private Bitmap bm;
    private LinearLayout albumArtLayout;
    private Toast toast;
    private Context context;
    private String toastText;

    MediaPlayerService mService;
    boolean mBound = false;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Log.d(LOG_TAG, "START: The onCreate() event");

        context = getApplicationContext();
        Intent intent = getIntent();
        String action = intent.getAction();

        selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        initializePlayer(selectedTrack);

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
            }

            return;
        }

        /*selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        initializePlayer(selectedTrack);*/
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

        //playSong(selectedTrack);
        Log.d(LOG_TAG, "END: The onCreate() event");
    }

    public void play(View view) {
        mp = MediaPlayerService.getMp();

        if(mp != null) {
            if(mp.isPlaying()) {
                // If already playing, pause the current track
                mp.pause();
                isPaused = true;
                playButton.setImageResource(R.drawable.play_button);
            } else if(!isPaused) {
                //Else, if paused, resume current track
                playSong(selectedTrack);
                isPaused = false;
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                //updateProgressBar();
            } else {
                //Else, if stopped, start playback
                mp.start();
                isPaused = false;
                playButton.setImageResource(R.drawable.pause_button);
            }
        }

        mService.createNotification(selectedTrack);
    }

    public void next(View view) {
        mp = MediaPlayerService.getMp();
        mp.reset();
        MediaPlayerService.setMp(mp);
        isIdle = true;

        if(isShuffling) {
            //If shuffling is on, play a random song
            selectedTrack = MediaLibraryManager.getTrackByIndex(getNextIndex());
        } else if(isRepeatingCurrent) {
            //Else, if repeating current is on, restart the same song

        } else if(MediaLibraryManager.isLastTrack(currentIndex)) {
            if(isRepeatingAll) {
                // Else, if repeating all is on and is currently playing the last song,
                // play next song in the playlist
                selectedTrack = MediaLibraryManager.getFirstTrack();
            } else {
                //Stop playback
                playButton.setImageResource(R.drawable.play_button);
                return;
            }
        } else {
            //Else, play the previous song
            selectedTrack = MediaLibraryManager.getTrackByIndex(++currentIndex);
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
            selectedTrack = MediaLibraryManager.getTrackByIndex(getNextIndex());
        } else if(isRepeatingCurrent) {
            //Else, if repeating current is on, restart the same song
        } else if(MediaLibraryManager.isFirstTrack(currentIndex)) {
            if(isRepeatingAll) {
                // Else, if repeating all is on and is currently playing the first song,
                // play last song in the playlist
                selectedTrack = MediaLibraryManager.getLastTrack();
            } else {
                //Stop playback
                playButton.setImageResource(R.drawable.play_button);
                return;
            }
        } else {
            //Else, play the previous song
            selectedTrack = MediaLibraryManager.getTrackByIndex(--currentIndex);
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
        if (!isRepeatingCurrent && !isRepeatingAll) {
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
        timeElapsed = (TextView) findViewById(R.id.timeElapsed);
        trackDuration = (TextView) findViewById(R.id.trackDuration);
        albumArt = (ImageView) findViewById(R.id.albumArt);
        albumArtLayout = (LinearLayout) findViewById(R.id.albumArtLayout);
        songTitle = requestedTrack.getTrackTitle();
        albumName = requestedTrack.getAlbumName();
        artistName = requestedTrack.getArtistName();
        songDuration = String.valueOf(requestedTrack.getTrackDuration());
        currentIndex = requestedTrack.getTrackIndex();
        data = requestedTrack.getAlbumArt();
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);

        if (data != null) {
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        if(mp == null) {
            mp = new MediaPlayer();
        }

        titleBar.setText(songTitle);
        artistBar.setText(artistName);
        trackDuration.setText(Utilities.milliSecondsToTimer(Long.parseLong(songDuration)));
        albumArt.setImageBitmap(bm);

        Log.d(LOG_TAG, "Media Player initialized");
    }

    public void playSong(Track requestedTrack) {
        Log.d(LOG_TAG, "START: The playSong() event");

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, requestedTrack);

        //Bind to the MediaPlayerService
        if(MediaPlayerService.isServiceRunning) {
            if(!MediaPlayerService.isServiceBound) {
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            } else {
                mService.playSong(requestedTrack);
                mService.createNotification(requestedTrack);
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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.MyBinder binder = (MediaPlayerService.MyBinder) service;
            mService = binder.getService();
            Log.d(LOG_TAG, "Service connected: " + mService);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
        selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        setIntent(intent);
        Log.d(LOG_TAG, action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Mediaplayer activity resumed");

        Intent intent = getIntent();
        String action = intent.getAction();

        selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        initializePlayer(selectedTrack);

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
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        Log.d(LOG_TAG, "Mediaplayer activity stopped");
    }

    // Called just before the activity is destroyed.
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "Mediaplayer activity destroyed");
    }
}
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
import com.mediaplayer.strings.utilities.MessageConstants;
import com.mediaplayer.strings.utilities.SQLConstants;
import com.mediaplayer.strings.utilities.Utilities;

import java.util.ArrayList;
import java.util.Random;

public class MediaPlayerActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = "MediaPlayerActivity";
    private static MediaPlayer mp;
    private static Track selectedTrack;
    private static String selectedPlaylist;
    private static SeekBar songProgressBar;
    private static TextView timeElapsed;
    private static final Handler mHandler = new Handler();

    private ImageButton playButton, nextButton, previousButton, repeatButton, shuffleButton;
    private ArrayList<Integer> tracksCompleted = new ArrayList<Integer>();
    private boolean isPaused = false, isRepeatingAll = false, isRepeatingCurrent = false, isShuffling = false, mBound = false;
    private int currentIndex, playlistSize, width;
    private Bitmap bm;
    private Toast toast;
    private Context context;
    private String toastText, origin, playlistName;
    private MediaPlayerService mService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        Log.d(LOG_TAG, "MediaPlayerActivity created");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        context = getApplicationContext();
        Intent intent = getIntent();
        String action = intent.getAction();

        selectedTrack = (Track) intent.getSerializableExtra(MediaPlayerConstants.KEY_SELECTED_TRACK);
        selectedPlaylist = intent.getStringExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST);
        playlistName = intent.getStringExtra(MediaPlayerConstants.KEY_PLAYLIST_TITLE);
        origin = intent.getStringExtra(MediaPlayerConstants.KEY_TRACK_ORIGIN);
        initializePlayer(selectedTrack);

        //Setting SeekBar listener
        songProgressBar.setOnSeekBarChangeListener(this);

        if(action != null) {
            switch(action) {
                case MediaPlayerConstants.PREVIOUS:
                    previousButton.performClick();
                    break;

                case MediaPlayerConstants.PAUSE:
                    playButton.setTag(R.id.playButton, MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY);
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
        }

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

                        if(mService == null) {
                            Intent serviceIntent = new Intent(this, MediaPlayerService.class);
                            serviceIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_TRACK, selectedTrack);
                            serviceIntent.putExtra(MediaPlayerConstants.KEY_SELECTED_PLAYLIST, selectedPlaylist);
                            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
                        } else {
                            mService.stopForeground(false);
                            Log.d(LOG_TAG, "Is foreground?: false");
                            mService.createNotification(selectedTrack, selectedPlaylist);
                        }

                        break;
                }
            } else if(!isPaused) {
                //Else, if stopped, start playback
                playSong(selectedTrack);
                isPaused = false;
                songProgressBar.setProgress(SQLConstants.ZERO);
                songProgressBar.setMax(SQLConstants.HUNDRED);
            } else {
                //Else, if paused, resume current track
                mp.start();
                isPaused = false;
                playButton.setImageResource(R.drawable.pause_button);
                mService.startForeground(SQLConstants.ONE, mService.createNotification(selectedTrack, selectedPlaylist));
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
                mService.stopForeground(false);
                mService.createNotification(selectedTrack, selectedPlaylist);
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
                mService.stopForeground(false);
                mService.createNotification(selectedTrack, selectedPlaylist);
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

        switch(selectedPlaylist) {
            case MediaPlayerConstants.TAG_PLAYLIST_LIBRARY:
                currentIndex = requestedTrack.getTrackIndex();
                playlistName = MediaPlayerConstants.TITLE_LIBRARY;
                break;

            case MediaPlayerConstants.TAG_PLAYLIST_OTHER:
                currentIndex = requestedTrack.getCurrentTrackIndex();
                break;
        }

        byte[] data = requestedTrack.getAlbumArt();
        songProgressBar.setProgress(SQLConstants.ZERO);
        songProgressBar.setMax(SQLConstants.HUNDRED);

        if(mp == null) {
            mp = new MediaPlayer();
            MediaPlayerService.setMp(mp);
        }

        mp.setOnCompletionListener(this);

        titleBar.setText(songTitle);
        artistBar.setText(artistName);
        albumBar.setText(albumName);
        playingFrom.setText(playlistName);
        trackDuration.setText(Utilities.milliSecondsToTimer(Long.parseLong(songDuration)));

        if(data != null) {
            bm = BitmapFactory.decodeByteArray(data, SQLConstants.ZERO, data.length);

            if(bm != null) {
                int size = width / 2;
                albumArt.setImageBitmap(Bitmap.createScaledBitmap(bm, size, size, false));
            } else {
                albumArt.setImageBitmap(bm);
            }
        }

        albumArtThumbnail.setImageBitmap(bm);
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
            if(!MediaPlayerService.isServiceBound) {
                //Binding to MediaPlayerService
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            //Else, if service is running and bound and track is paused, resume playback
            } else if(isPaused){
                mService.playSong(selectedTrack);
                mService.startForeground(SQLConstants.ONE, mService.createNotification(selectedTrack, selectedPlaylist));

            //Else, if track is stoppped, play current track
            } else {
                mService.playSong(selectedTrack);
                mService.createNotification(selectedTrack, selectedPlaylist);
            }
        } else {
            Log.d(LOG_TAG, "Service not running");
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        playButton.setImageResource(R.drawable.pause_button);
        Log.d(LOG_TAG, "END: The playSong() event");
    }

    //Update timer on seekbar
    private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 10);
    }

    //Background Runnable thread for updating progress bar
    private static Runnable mUpdateTimeTask = new Runnable() {
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
    };

    @Override
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

        setIntent(intent);
        Log.d(LOG_TAG, action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Mediaplayer activity resumed");
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

    public static void stopProgressBar() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        timeElapsed.setText(Utilities.milliSecondsToTimer(SQLConstants.ZERO));
        songProgressBar.setProgress(SQLConstants.ZERO);

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
                switch(origin) {
                    case MediaPlayerConstants.TAG_SONGS_LIST_VIEW:
                        mService.playSong(selectedTrack);
                        mService.createNotification(selectedTrack, selectedPlaylist);
                        break;

                    case MediaPlayerConstants.TAG_MEDIAPLAYER_ACTIVITY:
                        mService.stopForeground(false);
                        Log.d(LOG_TAG, "Is foreground?: false");
                        mService.createNotification(selectedTrack, selectedPlaylist);
                        break;

                    case MediaPlayerConstants.TAG_NOTIFICATION:
                        mService.playSong(selectedTrack);
                        mService.createNotification(selectedTrack, selectedPlaylist);
                        break;

                    default:
                        break;
                }
            }

            updateProgressBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp = MediaPlayerService.getMp();

        if(!mp.isPlaying()) {
            //Checking if track is on loop
            if (isRepeatingCurrent) {
                mp.reset();
                playSong(selectedTrack);

                //Checking if it is not the last track in the playlist
            } else if (!MediaLibraryManager.isLastTrack(selectedPlaylist, currentIndex)) {
                if (isShuffling) {
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
            } else if (isRepeatingAll) {
                if (isShuffling) {
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
                if (isShuffling) {
                    currentIndex = getNextIndex();
                    mp.reset();
                    selectedTrack = MediaLibraryManager.getTrackByIndex(selectedPlaylist, currentIndex);
                    initializePlayer(selectedTrack);
                    playSong(selectedTrack);
                } else {
                    //Stop playback
                    mp.reset();
                    playButton.setImageResource(R.drawable.play_button);
                    stopProgressBar();
                    isPaused = false;
                    mService.stopForeground(false);
                    mService.createNotification(selectedTrack, selectedPlaylist);
                }
            }
        }
    }
}
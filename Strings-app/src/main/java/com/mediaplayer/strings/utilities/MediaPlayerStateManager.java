package com.mediaplayer.strings.utilities;

import android.app.Application;

public class MediaPlayerStateManager extends Application {
    private boolean isPaused;

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}

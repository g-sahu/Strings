package com.mediaplayer.strings.utilities;

import android.app.Application;

public class MediaPlayerStateManager extends Application {
    private String mpState;
    private boolean isShuffling;
    private String repeatMode;

    public String getMpState() {
        return mpState;
    }

    public void setMpState(String mpState) {
        this.mpState = mpState;
    }

    public boolean isShuffling() {
        return isShuffling;
    }

    public void setShuffling(boolean shuffling) {
        isShuffling = shuffling;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(String repeatMode) {
        this.repeatMode = repeatMode;
    }
}

package com.mediaplayer.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mediaplayer.fragments.SongsFragment;
import com.mediaplayer.fragments.PlaylistsFragment;
import com.mediaplayer.utilities.MediaPlayerConstants;

public class HomePagerAdapter extends FragmentPagerAdapter {

    public HomePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return MediaPlayerConstants.PAGE_COUNT;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SongsFragment();

            case 1:
                return new PlaylistsFragment();

            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return MediaPlayerConstants.TITLE_SONGS;

            case 1:
                return MediaPlayerConstants.TITLE_PLAYLISTS;

            default:
                return null;
        }
    }
}

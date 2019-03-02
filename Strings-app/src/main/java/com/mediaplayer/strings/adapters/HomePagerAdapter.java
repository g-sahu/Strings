package com.mediaplayer.strings.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.mediaplayer.strings.fragments.PlaylistsFragment;
import com.mediaplayer.strings.fragments.SongsFragment;

import static com.mediaplayer.strings.utilities.MediaPlayerConstants.PAGE_COUNT;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.TITLE_PLAYLISTS;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.TITLE_SONGS;

public class HomePagerAdapter extends FragmentPagerAdapter {

    public HomePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return PAGE_COUNT;
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
                return TITLE_SONGS;

            case 1:
                return TITLE_PLAYLISTS;

            default:
                return null;
        }
    }
}

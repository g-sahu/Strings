package com.mediaplayer.strings.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import static android.support.v7.app.AlertDialog.Builder;
import static com.mediaplayer.strings.BuildConfig.VERSION_NAME;
import static com.mediaplayer.strings.R.id;
import static com.mediaplayer.strings.R.layout.dialog_about_us;
import static com.mediaplayer.strings.utilities.MediaPlayerConstants.VERSION;

public class AboutUsDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String versionName = VERSION + " " + VERSION_NAME;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(dialog_about_us, null);
        TextView versionNumber = dialogView.findViewById(id.versionNumber);
        versionNumber.setText(versionName);

        Builder builder = new Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }
}

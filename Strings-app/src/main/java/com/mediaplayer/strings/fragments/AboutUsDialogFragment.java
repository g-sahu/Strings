package com.mediaplayer.strings.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediaplayer.strings.BuildConfig;
import com.mediaplayer.strings.R;
import com.mediaplayer.strings.utilities.MediaPlayerConstants;


public class AboutUsDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String versionName = MediaPlayerConstants.VERSION + " " + BuildConfig.VERSION_NAME;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about_us, null);
        TextView versionNumber = dialogView.findViewById(R.id.versionNumber);
        versionNumber.setText(versionName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }
}

package com.mediaplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.mediaplayer.R;

public class SplashScreenActivity extends AppCompatActivity {
    Button launchButton;
    Intent intent;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        launchButton = (Button) findViewById(R.id.launch_button);
        intent = new Intent(this, PlaylistActivity.class);

        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(intent);
            }
        });
    }
}

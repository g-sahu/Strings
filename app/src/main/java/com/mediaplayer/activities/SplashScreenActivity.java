package com.mediaplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mediaplayer.R;
import com.mediaplayer.services.MediaManagerService;

public class SplashScreenActivity extends AppCompatActivity {
    private Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Button launchButton = (Button) findViewById(R.id.launch_button);
        //intent = new Intent(this, HomeActivity.class);
        intent = new Intent(this, MediaManagerService.class);

        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //startActivity(intent);
                Log.d("SplashScreenActivity", "Starting MediaManagerService...");
                startService(intent);
                Log.d("SplashScreenActivity", "Exiting SplashScreenActivity");
            }
        });
    }
}

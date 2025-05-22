package com.example.climap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setVideoURI(videoUri);
        videoView.setZOrderOnTop(true);  // keeps it in front

        videoView.setOnCompletionListener(mp -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        });

        videoView.start();
    }
}

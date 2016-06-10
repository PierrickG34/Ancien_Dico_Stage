package com.antoine_charlotte_romain.dictionary.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import com.antoine_charlotte_romain.dictionary.R;

public class SplashActivity extends Activity {


    private final int SPLASH_DISPLAY_LENGTH = 2000;

    private MediaPlayer splashSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashSound = MediaPlayer.create(SplashActivity.this,R.raw.splash);
        splashSound.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                splashSound.release();
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
package ie.csis.app.dicosaure.views.activities

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler

import ie.csis.app.dicosaure.views.R

class SplashActivity : Activity() {


    private val SPLASH_DISPLAY_LENGTH = 2000

    private var splashSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashSound = MediaPlayer.create(this@SplashActivity, R.raw.splash)
        splashSound!!.start()
        Handler().postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
            splashSound!!.release()
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            this@SplashActivity.startActivity(mainIntent)
            this@SplashActivity.finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}
package com.example.moderato

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var logoWave: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoWave = findViewById(R.id.logoWave)

        startWaveAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

    private fun startWaveAnimation() {
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000
            repeatCount = Animation.INFINITE
        }
        logoWave.startAnimation(rotateAnimation)
    }
}
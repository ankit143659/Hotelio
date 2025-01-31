package com.example.hotelio

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

class MainActivity : AppCompatActivity() {
    private lateinit var lottieAnimationView : LottieAnimationView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        lottieAnimationView = findViewById(R.id.progressBar)
        lottieAnimationView.playAnimation()

        lottieAnimationView = findViewById(R.id.welcome)
        lottieAnimationView.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this,LoginPage::class.java)
            startActivity(intent)
            finish()
        },3000)

    }
}
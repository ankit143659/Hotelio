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
    private lateinit var share : SharePrefrence
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        share = SharePrefrence(this)

        lottieAnimationView = findViewById(R.id.progressBar)
        lottieAnimationView.playAnimation()

        lottieAnimationView = findViewById(R.id.welcome)
        lottieAnimationView.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({

            if(share.checkLoginState()){
                val intent = Intent(this,HomeMainPage::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this,LoginPage::class.java)
                startActivity(intent)
                finish()
            }

        },3000)

    }
}
package com.example.hotelio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn : Button

    private lateinit var lottieAnimationView: LottieAnimationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.Password)

        loginBtn = findViewById(R.id.btnLogin)

        loginBtn.setOnClickListener{
            addDataToFirebase()
        }

     lottieAnimationView = findViewById(R.id.loginAnimation)
     lottieAnimationView.playAnimation()

        val signIn : TextView = findViewById(R.id.tvSignUp)
        signIn.setOnClickListener{
            val intent = Intent(this,ResgistrationPage::class.java)
            startActivity(intent)
        }
    }

    private fun addDataToFirebase() {
        val Email = email.text.toString().trim()
        val Pass = password.text.toString().trim()

        if (Email.isNotBlank() && Pass.isNotBlank()){
            auth.signInWithEmailAndPassword(Email,Pass)
                .addOnCompleteListener(this){task->
                    if (task.isSuccessful){
                        Toast.makeText(this,"Login Successfull",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show()
                    }
                }
        }else{
            Toast.makeText(this,"Please Fill all details",Toast.LENGTH_SHORT).show()
        }

    }
}
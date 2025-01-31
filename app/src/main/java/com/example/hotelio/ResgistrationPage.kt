package com.example.hotelio

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ResgistrationPage : AppCompatActivity() {
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var username: EditText
    private lateinit var mobile: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirimPassword: EditText
    private lateinit var registrationBtn: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var datbase :DatabaseReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resgistration_page)

        auth = FirebaseAuth.getInstance()
        datbase = FirebaseDatabase.getInstance().reference

        lottieAnimationView = findViewById(R.id.registerAnimation)
        lottieAnimationView.playAnimation()

        username = findViewById(R.id.Username)
        mobile = findViewById(R.id.moblie)
        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.Password)
        confirimPassword = findViewById(R.id.cPassword)
        registrationBtn = findViewById(R.id.btnregister)

        registrationBtn.setOnClickListener {
            checkDetailsAndLogin()
        }

        val signIn: TextView = findViewById(R.id.tvLogin)
        signIn.setOnClickListener {
            NavigatetoLogin()
        }
    }

    private fun checkDetailsAndLogin() {
        val Username = username.text.toString().trim()
        val Mobile = mobile.text.toString().trim()
        val Email = email.text.toString().trim()
        val Password = password.text.toString().trim()
        val CPassword = confirimPassword.text.toString().trim()

        // Reset errors
        username.error = null
        mobile.error = null
        email.error = null
        password.error = null
        confirimPassword.error = null

        // Validate Username
        if (Username.isEmpty()) {
            username.error = "Username is required"
            return
        } else if (Username.length < 2 || Username.length > 16) {
            username.error = "Username must be between 2 and 16 characters"
            return
        }

        // Validate Mobile
        if (Mobile.isEmpty()) {
            mobile.error = "Mobile number is required"
            return
        } else if (Mobile.length != 10 || !Mobile.matches(Regex("^[0-9]{10}$"))) {
            mobile.error = "Invalid mobile number (10 digits required)"
            return
        }

        // Validate Email
        if (Email.isEmpty()) {
            email.error = "Email is required"
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            email.error = "Invalid email format"
            return
        }

        // Validate Password
        if (Password.isEmpty()) {
            password.error = "Password is required"
            return
        } else if (Password.length < 8) {
            password.error = "Password must be at least 8 characters long"
            return
        } else if (!Password.matches(Regex(".*[A-Z].*"))) {
            password.error = "Password must contain at least one uppercase letter"
            return
        } else if (!Password.matches(Regex(".*[a-z].*"))) {
            password.error = "Password must contain at least one lowercase letter"
            return
        } else if (!Password.matches(Regex(".*[0-9].*"))) {
            password.error = "Password must contain at least one digit"
            return
        } else if (!Password.matches(Regex(".*[!@#$%^&*()_+=|<>{}\\[\\]~-].*"))) {
            password.error = "Password must contain at least one special character"
            return
        }

        // Validate Confirm Password
        if (CPassword.isEmpty()) {
            confirimPassword.error = "Confirm Password is required"
            return
        } else if (CPassword != Password) {
            confirimPassword.error = "Passwords do not match"
            return
        }

        // Firebase Registration
        auth.createUserWithEmailAndPassword(Email, Password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userData = HashMap<String, String>()
                        userData["Username"] = Username
                        userData["Mobile no"] = Mobile
                        userData["Email"] = Email
                        userData["Password"] = Password

                        datbase.child("Users").child(userId).setValue(userData)
                            .addOnCompleteListener {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                movetoLoginPage()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Handle Firebase Authentication errors
                    val exception = task.exception
                    if (exception != null) {
                        when (exception) {
                            is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
    }

    private fun movetoLoginPage() {
        val intent = Intent(this, LoginPage::class.java)
        startActivity(intent)
    }

    private fun NavigatetoLogin() {
        val intent = Intent(this, LoginPage::class.java)
        startActivity(intent)
    }
}
package com.example.hotelio

import android.content.Context
import android.content.SharedPreferences

class SharePrefrence (context: Context){

    private val prefs : SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun loginState(State : Boolean){
        val editor = prefs.edit()
        editor.putBoolean("Login",State)
        editor.apply()
    }

    fun checkLoginState():Boolean{
        return prefs.getBoolean("Login",false)
    }

}
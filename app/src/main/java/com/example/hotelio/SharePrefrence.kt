package com.example.hotelio

import android.content.Context
import android.content.SharedPreferences

class SharePrefrence (context: Context){

    private val prefs : SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)


}
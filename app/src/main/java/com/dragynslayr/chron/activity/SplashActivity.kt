package com.dragynslayr.chron.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragynslayr.chron.R
import com.dragynslayr.chron.data.User
import com.dragynslayr.chron.helper.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableNightMode()

        database = Firebase.database.reference

        createNotificationChannel()

        if (savedInstanceState == null) {
            checkForToken()
        }
    }

    private fun checkForToken() {
        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userToken = sharedPreferences.getString(getString(R.string.user_token_key), null)
        if (userToken != null) {
            val split = userToken.split(",")
            verifyToken(split[0], split[1])
        } else {
            startLogin()
        }
    }

    private fun verifyToken(username: String, password: String) {
        database.child(DB_USERS).child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user = p0.getValue<User>()
                        if (user != null) {
                            if (user.password == password) {
                                startMain(user)
                                return
                            }
                        }
                    }
                    startLogin()
                }
            })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
package com.dragynslayr.chron.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.postDelayed
import com.dragynslayr.chron.R
import com.dragynslayr.chron.data.Birthday
import com.dragynslayr.chron.helper.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var calendar: Calendar
    private lateinit var context: Context
    private lateinit var database: DatabaseReference
    private lateinit var user: String


    override fun onReceive(c: Context?, i: Intent?) {
        calendar = Calendar.getInstance()
        calendar.time = Date()

        if (calendar.get(Calendar.HOUR_OF_DAY) != 0) {
            return
        }

        context = c!!

        FirebaseApp.initializeApp(context)
        database = Firebase.database.reference
        getUser()

        database.child(DB_USERS).child(user).child("WeirdVal").setValue("Weird")

        getBirthdays()
    }

    private fun getUser() {
        val sharedPreferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val userToken =
            sharedPreferences.getString(context.getString(R.string.user_token_key), null)
        if (userToken != null) {
            val split = userToken.split(",")
            user = split[0]
        }
    }

    private fun getBirthdays() {
        database.child(DB_BIRTHDAYS).child(user)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val birthdays = arrayListOf<Birthday>()
                        snapshot.children.forEach {
                            birthdays.add(it.getValue<Birthday>()!!)
                        }
                        checkBirthdays(birthdays)
                    }
                }
            })
    }

    private fun checkBirthdays(allBirthdays: ArrayList<Birthday>) {
        val today = getCurrentDate()

        val birthdays = arrayListOf<Birthday>()
        var birthdayMsg = StringBuilder()

        allBirthdays.forEach {
            if (it.month == today.month && it.day == today.day) {
                birthdays.add(it)
                birthdayMsg.append(it.name).append("\n")
            }
        }

        birthdayMsg = if (birthdayMsg.isEmpty()) {
            StringBuilder("None")
        } else {
            StringBuilder(birthdayMsg.toString().trim())
        }

        val todayString = getDateString(today.month, today.day)
        makeNotification("Birthdays on $todayString", birthdayMsg.toString())
        sendMessages(birthdays)
    }

    private fun makeNotification(title: String, msg: String) {
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_chron_notif)
                .setContentTitle(title).setContentText(msg)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        val manager = NotificationManagerCompat.from(context)
        manager.notify(NOTIFICATION_ID, builder.build())
        NOTIFICATION_ID++
    }

    private fun sendMessages(birthdays: ArrayList<Birthday>) {
        val sms = SmsManager.getDefault()
        val currentYear = calendar.get(Calendar.YEAR)
        birthdays.forEachIndexed { i, birthday ->
            if (birthday.lastSentYear!! < currentYear) {
                Handler().postDelayed(i * 500L) {
                    sms.sendTextMessage(birthday.phone!!, null, birthday.message!!, null, null)
                    birthday.lastSentYear = currentYear
                    birthday.upload(database, user)
                }
            }
        }
    }

    companion object {
        private var NOTIFICATION_ID = 0
    }
}
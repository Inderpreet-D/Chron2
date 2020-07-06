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
import com.dragynslayr.chron.helper.CHANNEL_ID
import com.dragynslayr.chron.helper.getCurrentDate
import com.dragynslayr.chron.helper.getDateString
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
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
        user = Firebase.auth.currentUser!!.uid
        database = Firebase.database.reference

        getBirthdays()
    }

    private fun getBirthdays() {
        database.child(user).addListenerForSingleValueEvent(object : ValueEventListener {
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
        birthdays.forEachIndexed { i, birthday ->
            Handler().postDelayed(i * 500L) {
                sms.sendTextMessage(birthday.phone!!, null, birthday.message!!, null, null)
            }
        }
    }

    companion object {
        private var NOTIFICATION_ID = 0
    }
}